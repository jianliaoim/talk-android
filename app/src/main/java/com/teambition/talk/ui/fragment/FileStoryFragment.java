package com.teambition.talk.ui.fragment;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.transition.ChangeBounds;
import android.transition.ChangeTransform;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.joooonho.SelectableRoundedImageView;
import com.squareup.otto.Subscribe;
import com.teambition.talk.BizLogic;
import com.teambition.talk.BusProvider;
import com.teambition.talk.GsonProvider;
import com.teambition.talk.event.StoryDetailExpandEvent;
import com.teambition.talk.imageloader.ImageLoaderConfig;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.entity.File;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Story;
import com.teambition.talk.event.UpdateStoryEvent;
import com.teambition.talk.realm.MemberDataProcess;
import com.teambition.talk.realm.StoryDataProcess;
import com.teambition.talk.ui.MessageFormatter;
import com.teambition.talk.ui.activity.ChatActivity;
import com.teambition.talk.ui.activity.FilePreViewActivity;
import com.teambition.talk.util.AnalyticsHelper;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.util.TransactionUtil;

import org.parceler.Parcels;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by zeatual on 15/11/24.
 */
public class FileStoryFragment extends BaseFragment {

    @InjectView(R.id.background)
    RelativeLayout background;
    @InjectView(R.id.view_shadow)
    View shadow;
    @InjectView(R.id.image)
    SelectableRoundedImageView imageView;
    @InjectView(R.id.tv_title)
    TextView tvTitle;
    @InjectView(R.id.tv_creator)
    TextView tvCreator;
    @InjectView(R.id.tv_date)
    TextView tvDate;
    @InjectView(R.id.tv_description)
    TextView tvDescription;
    @InjectView(R.id.file_scheme)
    TextView tvFileScheme;

    private View overlay;

    private Story story;
    private File file;
    private Member creator;

    public boolean isExpanded;

    static public FileStoryFragment getInstance(Story story, boolean isExpanded) {
        FileStoryFragment f = new FileStoryFragment();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                f.setSharedElementEnterTransition(new ChangeTransform());
            }
            f.setSharedElementEnterTransition(new ChangeBounds());
        }
        Bundle bundle = new Bundle();
        bundle.putBoolean("isExpanded", isExpanded);
        bundle.putParcelable("story", Parcels.wrap(story));
        f.setArguments(bundle);
        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        overlay = activity.findViewById(R.id.view_overlay);
        this.isExpanded = getArguments().getBoolean("isExpanded", false);
        this.story = Parcels.unwrap(getArguments().getParcelable("story"));
        file = GsonProvider.getGson().fromJson(story.getData(), File.class);
        if (MainApp.globalMembers.containsKey(story.get_creatorId())) {
            creator = MainApp.globalMembers.get(story.get_creatorId());
        } else {
            creator = MemberDataProcess.getAnonymousInstance();
        }
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        BusProvider.getInstance().unregister(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(isExpanded ? R.layout.fragment_expanded_file_story :
                (StringUtil.isNotBlank(file.getText()) ?
                        R.layout.fragment_collapsed_file_story_with_description :
                        R.layout.fragment_collapsed_file_story_without_description), null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        setStoryContent();
    }

    private void setStoryContent() {
        if (BizLogic.isImg(file)) {
            imageView.setVisibility(View.VISIBLE);
            tvFileScheme.setVisibility(View.GONE);
            MainApp.IMAGE_LOADER.displayImage(file.getDownloadUrl(), imageView,
                    ImageLoaderConfig.EMPTY_OPTIONS);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(FilePreViewActivity.FILE, Parcels.wrap(file));
                    TransactionUtil.goTo(getActivity(), FilePreViewActivity.class, bundle);
                }
            });
        } else {
            tvFileScheme.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            GradientDrawable drawable = new GradientDrawable();
            final float radius = MainApp.CONTEXT.getResources().getDimension(R.dimen.story_chat_file_radius);
            drawable.setCornerRadii(new float[]{radius, radius, radius, radius, radius, radius, radius, radius});
            drawable.setColor(Color.parseColor(file.getSchemeColor(file.getFileType())));
            tvFileScheme.setBackgroundDrawable(drawable);
            tvFileScheme.setText(file.getFileType());
            tvFileScheme.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(FilePreViewActivity.FILE, Parcels.wrap(file));
                    TransactionUtil.goTo(getActivity(), FilePreViewActivity.class, bundle);
                }
            });
        }
        tvTitle.setText(file.getFileName());
        tvCreator.setText(creator.getAlias() == null ? creator.getName() : creator.getAlias());
        if (StringUtil.isNotBlank(file.getText())) {
            tvDescription.setText(file.getText());
        }
        tvDate.setText(String.format(getString(R.string.create_at),
                MessageFormatter.formatCreateTime(story.getCreatedAt())));
    }

    @Subscribe
    public void onUpdateStory(UpdateStoryEvent event) {
        if (event.story == null || StoryDataProcess.Category.getEnum(story.getCategory()) != StoryDataProcess.Category.FILE) return;
        this.story = event.story;
        file = GsonProvider.getGson().fromJson(story.getData(), File.class);
        if (MainApp.globalMembers.containsKey(story.get_creatorId())) {
            creator = MainApp.globalMembers.get(story.get_creatorId());
        } else {
            creator = MemberDataProcess.getAnonymousInstance();
        }
        setStoryContent();
    }

    @OnClick({R.id.background})
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.background:
                if (!isExpanded) {
                    AnalyticsHelper.getInstance().sendEvent(AnalyticsHelper.Category.page_elements,
                            "show story detail", null);
                            ((ChatActivity) getActivity()).resetKeyBoardButton();
                    getFragmentManager().beginTransaction()
                            .addSharedElement(background, "background")
                            .addSharedElement(shadow, "shadow")
                            .setCustomAnimations(R.anim.anim_fade_in, R.anim.anim_fade_out)
                            .replace(R.id.container, FileStoryFragment.getInstance(story, !isExpanded))
                            .commit();
                    overlay.animate()
                            .alpha(0.8F)
                            .setDuration(200L)
                            .setInterpolator(new FastOutSlowInInterpolator())
                            .start();
                    overlay.setClickable(true);
                    BusProvider.getInstance().post(new StoryDetailExpandEvent(true));
                } else {
                    ((ChatActivity) getActivity()).resetStory();
                }
                break;
        }
    }

}
