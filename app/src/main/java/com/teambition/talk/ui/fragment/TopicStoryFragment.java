package com.teambition.talk.ui.fragment;

import android.app.Activity;
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

import com.squareup.otto.Subscribe;
import com.teambition.talk.BusProvider;
import com.teambition.talk.GsonProvider;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.entity.File;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Story;
import com.teambition.talk.entity.Topic;
import com.teambition.talk.event.RemoveStoryEvent;
import com.teambition.talk.event.StoryDetailExpandEvent;
import com.teambition.talk.event.UpdateStoryEvent;
import com.teambition.talk.realm.MemberDataProcess;
import com.teambition.talk.realm.StoryDataProcess;
import com.teambition.talk.ui.MessageFormatter;
import com.teambition.talk.ui.activity.ChatActivity;
import com.teambition.talk.util.AnalyticsHelper;
import com.teambition.talk.util.StringUtil;

import org.parceler.Parcels;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by zeatual on 15/11/24.
 */
public class TopicStoryFragment extends BaseFragment {

    @InjectView(R.id.background)
    RelativeLayout background;
    @InjectView(R.id.view_shadow)
    View shadow;
    @InjectView(R.id.tv_title)
    TextView tvTitle;
    @InjectView(R.id.tv_creator)
    TextView tvCreator;
    @InjectView(R.id.tv_date)
    TextView tvDate;
    @InjectView(R.id.tv_description)
    TextView tvDescription;

    private View overlay;

    private Story story;
    private Topic topic;
    private Member creator;

    public boolean isExpanded;

    static public TopicStoryFragment getInstance(Story story, boolean isExpanded) {
        TopicStoryFragment f = new TopicStoryFragment();
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
        topic = GsonProvider.getGson().fromJson(story.getData(), Topic.class);
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
        return inflater.inflate(isExpanded ? R.layout.fragment_expanded_topic_story :
                (StringUtil.isNotBlank(topic.getText()) ?
                        R.layout.fragment_collapsed_file_topic_with_description :
                        R.layout.fragment_collapsed_file_topic_without_description), null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        setStoryContent();
    }

    private void setStoryContent() {
        tvTitle.setText(topic.getTitle());
        tvCreator.setText(creator.getAlias() == null ? creator.getName() : creator.getAlias());
        if (StringUtil.isNotBlank(topic.getText())) {
            tvDescription.setText(topic.getText());
        }
        tvDate.setText(String.format(getString(R.string.create_at),
                MessageFormatter.formatCreateTime(story.getCreatedAt())));
    }

    @Subscribe
    public void onUpdateStory(UpdateStoryEvent event) {
        if (event.story == null || StoryDataProcess.Category.getEnum(story.getCategory()) != StoryDataProcess.Category.TOPIC) return;
        this.story = event.story;
        topic = GsonProvider.getGson().fromJson(story.getData(), Topic.class);
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
                            .setCustomAnimations(R.anim.anim_fade_in, R.anim.anim_fade_out)
                            .addSharedElement(background, "background")
                            .addSharedElement(shadow, "shadow")
                            .replace(R.id.container, TopicStoryFragment.getInstance(story, !isExpanded))
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
