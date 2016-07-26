package com.teambition.talk.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.teambition.talk.BizLogic;
import com.teambition.talk.GsonProvider;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.client.data.InfoType;
import com.teambition.talk.entity.File;
import com.teambition.talk.entity.Link;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.TeamActivity;
import com.teambition.talk.entity.Topic;
import com.teambition.talk.imageloader.ImageLoaderConfig;
import com.teambition.talk.ui.MessageFormatter;
import com.teambition.talk.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by nlmartian on 2/16/16.
 */
public class TeamActivitiesAdapter extends RecyclerView.Adapter {
    public static final int VIEW_TYPE_LOADING = 0;
    public static final int VIEW_TYPE_ACTIVITY = 1;

    private List<TeamActivity> teamActivities = new ArrayList<>();

    private boolean isLoadingMore;

    private OnClickListener onClickListener;

    public interface OnClickListener {
        void onClick(TeamActivity activity);

        void onLongClick(TeamActivity activity);
    }

    public TeamActivitiesAdapter(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_LOADING) {
            return new LoadingHolder(inflater.inflate(R.layout.item_loading_white, parent, false));
        } else {
            View itemView = inflater.inflate(R.layout.row_team_activity, parent, false);
            return new ActivityViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ActivityViewHolder) {
            final TeamActivity activity = teamActivities.get(position);
            ActivityViewHolder viewHolder = (ActivityViewHolder) holder;
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.onClick(activity);
                }
            });
            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    onClickListener.onLongClick(activity);
                    return true;
                }
            });

            Member creator = activity.getCreator();
            viewHolder.icon.setOval(true);
            viewHolder.icon.setScaleType(ImageView.ScaleType.CENTER);
            viewHolder.tvContent.setVisibility(View.GONE);
            viewHolder.tvDetail.setVisibility(View.GONE);
            viewHolder.preview.setVisibility(View.GONE);
            if (activity.getType() == null) {
                viewHolder.icon.setImageResource(R.drawable.ic_activity_system);
            } else if ("room".equals(activity.getType())) {
                viewHolder.icon.setImageResource(R.drawable.ic_type_topic);
                viewHolder.tvContent.setVisibility(View.VISIBLE);
                if (activity.getRoom() != null) {
                    viewHolder.tvContent.setText(activity.getRoom().getTopic());
                    if (StringUtil.isNotBlank(activity.getRoom().getPurpose())) {
                        viewHolder.tvDetail.setVisibility(View.VISIBLE);
                        viewHolder.tvDetail.setText(activity.getRoom().getPurpose());
                    }
                }
            } else if ("story".equals(activity.getType())) {
                if (activity.getStory() != null) {
                    viewHolder.tvContent.setVisibility(View.VISIBLE);
                    viewHolder.tvContent.setText(activity.getStory().getTitle());
                }
                if (TextUtils.equals("file", activity.getStory().getCategory())) {
                    viewHolder.icon.setImageResource(R.drawable.ic_story_type_file);
                    if (activity.getStory() != null && StringUtil.isNotBlank(activity.getStory().getData())) {
                        File file = GsonProvider.getGson().fromJson(activity.getStory().getData(), File.class);
                        if ("image".equals(file.getFileCategory()) && StringUtil.isNotBlank(file.getDownloadUrl())) {
                            viewHolder.preview.setVisibility(View.VISIBLE);
                            MainApp.IMAGE_LOADER.displayImage(file.getThumbnailUrl(), viewHolder.preview, ImageLoaderConfig.DEFAULT_OPTIONS);
                        }
                    }
                } else if (TextUtils.equals("topic", activity.getStory().getCategory())) {
                    viewHolder.icon.setImageResource(R.drawable.ic_story_type_idea);
                    if (activity.getStory() != null &&  StringUtil.isNotBlank(activity.getStory().getData())) {
                        Topic topic = GsonProvider.getGson().fromJson(activity.getStory().getData(), Topic.class);
                        viewHolder.tvDetail.setVisibility(View.VISIBLE);
                        viewHolder.tvDetail.setText(topic.getText());
                    }
                } else if (TextUtils.equals("link", activity.getStory().getCategory())) {
                    viewHolder.icon.setImageResource(R.drawable.ic_story_type_link);
                    if (activity.getStory() != null &&  StringUtil.isNotBlank(activity.getStory().getData())) {
                        Link link = GsonProvider.getGson().fromJson(activity.getStory().getData(), Link.class);
                        viewHolder.tvDetail.setVisibility(View.VISIBLE);
                        viewHolder.tvDetail.setText(link.getUrl());
                        if (StringUtil.isNotBlank(link.getImageUrl())) {
                            viewHolder.preview.setVisibility(View.VISIBLE);
                            MainApp.IMAGE_LOADER.displayImage(link.getImageUrl(), viewHolder.preview, ImageLoaderConfig.DEFAULT_OPTIONS);
                        }
                    }
                }
            }

            String authorName;
            if (creator != null && BizLogic.isMe(creator.get_id())) {
                authorName = MainApp.CONTEXT.getString(R.string.me);
            } else if (creator != null) {
                authorName = BizLogic.isXiaoai(creator) ? MainApp.CONTEXT.getString(R.string.talk_ai) : creator.getAlias();
            } else {
                authorName = MainApp.CONTEXT.getString(R.string.anonymous_user);
            }
            viewHolder.tvTitle.setText(MessageFormatter.formatNotification(activity.getText(), authorName));
            viewHolder.tvTime.setText(MessageFormatter.formatCreateTime(activity.getUpdatedAt()));
        }
    }

    @Override
    public int getItemCount() {
        return teamActivities.size() + (isLoadingMore ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoadingMore && position == teamActivities.size()) {
            return VIEW_TYPE_LOADING;
        } else {
            return VIEW_TYPE_ACTIVITY;
        }
    }

    public void initData(List<TeamActivity> activities) {
        this.teamActivities.clear();
        teamActivities.addAll(activities);
        notifyDataSetChanged();
    }

    public void addToTop(TeamActivity activity) {
        this.teamActivities.add(0, activity);
        notifyItemInserted(0);
    }

    public void updateItem(TeamActivity teamActivity) {
        for (int i = 0; i < teamActivities.size(); i++) {
            TeamActivity activity = teamActivities.get(i);
            if (TextUtils.equals(activity.get_id(), teamActivity.get_id())) {
                teamActivities.add(i, teamActivity);
                teamActivities.remove(i);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void addToEnd(List<TeamActivity> activities) {
        int position = teamActivities.size();
        this.teamActivities.addAll(activities);
        notifyItemRangeInserted(position + (isLoadingMore ? 1 : 0),
                activities.size() + (isLoadingMore ? 1 : 0));
    }

    public void setIsLoading(boolean isLoading) {
        this.isLoadingMore = isLoading;
        if (isLoadingMore) {
            notifyItemInserted(teamActivities.size());
        } else {
            notifyItemRemoved(teamActivities.size());
        }
    }

    public void removeActivity(String id) {
        for (int i = 0; i < teamActivities.size(); i++) {
            TeamActivity activity = teamActivities.get(i);
            if (TextUtils.equals(activity.get_id(), id)) {
                teamActivities.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.icon)
        RoundedImageView icon;
        @InjectView(R.id.image_preview)
        RoundedImageView preview;
        @InjectView(R.id.tv_time)
        TextView tvTime;
        @InjectView(R.id.tv_title)
        TextView tvTitle;
        @InjectView(R.id.tv_content)
        TextView tvContent;
        @InjectView(R.id.tv_detail)
        TextView tvDetail;
        @InjectView(R.id.background)
        RelativeLayout background;

        public ActivityViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }

    static class LoadingHolder extends RecyclerView.ViewHolder {
        public LoadingHolder(View itemView) {
            super(itemView);
        }
    }

}
