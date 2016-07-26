package com.teambition.talk.adapter;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.teambition.talk.BizLogic;
import com.teambition.talk.R;
import com.teambition.talk.entity.Draft;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Preference;
import com.teambition.talk.entity.Room;
import com.teambition.talk.entity.Story;
import com.teambition.talk.entity.User;
import com.teambition.talk.realm.DraftRealm;
import com.teambition.talk.realm.MessageDataProcess;
import com.teambition.talk.rx.EmptyAction;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.ui.row.NotificationRow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by zeatual on 14/11/7.
 */
public class NotificationAdapter extends RecyclerView.Adapter {

    private final static int VIEW_TYPE_NOTIFICATION = 0;
    private final static int VIEW_TYPE_LOADING = 1;
    private final static int VIEW_TYPE_WEBONLINE = 2;

    private LinearLayoutManager layoutManager;
    private List<NotificationRow> rows;
    private boolean isLoading;
    private int pinNum;
    private boolean showWebOnline;
    private OnHeaderClickListener onHeaderClickListener;
    private Preference preference;

    public interface OnHeaderClickListener {
        public void onHeaderClick();
    }

    public NotificationAdapter(LinearLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
        rows = new ArrayList<>();
        isLoading = false;
        pinNum = 0;
        getPreference();
    }

    private void getPreference() {
        User user = BizLogic.getUserInfo();
        preference = user != null ? user.getPreference() : null;
    }

    public List<NotificationRow> getRows() {
        return rows;
    }

    public void clearUnread(NotificationRow row) {
        for (int i = 0; i < rows.size(); i++) {
            NotificationRow notificationRow = rows.get(i);
            if (notificationRow.getNotification().get_id().equals(row.getNotification().get_id())) {
                notificationRow.getNotification().setUnreadNum(0);
                notifyItemChanged(rows.indexOf(notificationRow) + (showWebOnline ? 1 : 0));
                break;
            }
        }
    }

    public void updateNotification(NotificationRow row) {
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).getNotification().get_targetId().equals(row.getNotification().get_targetId())) {
                rows.get(i).getNotification().setIsMute(row.getNotification().getIsMute());
                notifyItemChanged(i + (showWebOnline ? 1 : 0));
                break;
            }
        }
    }

    public void updateNotificationDraft(Draft draft) {
        final List<NotificationRow> pinneds = new ArrayList<>();
        Iterator<NotificationRow> iterator = this.rows.iterator();
        NotificationRow draftNotificationRow = null;
        while (iterator.hasNext()) {
            NotificationRow row = iterator.next();
            if (draft.get_id().equals(BizLogic.getTeamId() + row.getNotification().get_targetId())) {
                row.getNotification().setOutlineText(draft.getContent() == null ? null : draft.getContent());
                row.getNotification().setDraftTempUpdateAt(row.getNotification().getDraftTempUpdateAt() == null ? row.getNotification().getUpdatedAt() : row.getNotification().getDraftTempUpdateAt());
                if (draft.getUpdatedAt() == null) {
                    row.getNotification().setUpdatedAt(row.getNotification().getDraftTempUpdateAt());
                } else {
                    row.getNotification().setUpdatedAt(draft.getUpdatedAt());
                }
                draftNotificationRow = row;
                if (!row.getNotification().getIsPinned()) {
                    iterator.remove();
                }
            }
            if (row.getNotification().getUpdatedAt() == null) {
                row.getNotification().setUpdatedAt(row.getNotification().getDraftTempUpdateAt());
            }
            if (row.getNotification().getIsPinned()) {
                pinneds.add(row);
                iterator.remove();
            }
        }
        if (draftNotificationRow != null && !draftNotificationRow.getNotification().getIsPinned()) {
            this.rows.add(draftNotificationRow);
        }
        Collections.sort(NotificationAdapter.this.rows, new Comparator<NotificationRow>() {
            @Override
            public int compare(NotificationRow lhs, NotificationRow rhs) {
                return rhs.getNotification().getUpdatedAt().compareTo(lhs.getNotification().getUpdatedAt());
            }
        });
        Collections.sort(pinneds, new Comparator<NotificationRow>() {
            @Override
            public int compare(NotificationRow lhs, NotificationRow rhs) {
                return rhs.getNotification().getUpdatedAt().compareTo(lhs.getNotification().getUpdatedAt());
            }
        });
        NotificationAdapter.this.rows.addAll(0, pinneds);
        notifyDataSetChanged();
    }

    public void updateOne(NotificationRow row) {
        int position = -1;
        boolean hasPinChanged = false;
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).getNotification().get_targetId().equals(row.getNotification().get_targetId())) {
                position = i;
                if (row.getNotification().getStatus() != MessageDataProcess.Status.NONE.ordinal()){
                    row.getNotification().setIsPinned(rows.get(i).getNotification().getIsPinned());
                }
                row.getNotification().setOutlineText(rows.get(i).getNotification().getOutlineText());
                hasPinChanged = rows.get(i).getNotification().getIsPinned() !=
                        row.getNotification().getIsPinned();
                break;
            }
        }

        if (position != -1) { // found one
            rows.remove(position);
            if (row.getNotification().getIsPinned()) {
                rows.add(0, row);
                layoutManager.scrollToPosition(0);
                notifyItemMoved(position + (showWebOnline ? 1 : 0), 0 + (showWebOnline ? 1 : 0));
                if (hasPinChanged) {
                    pinNum++;
                }
                notifyItemRangeChanged(0  + (showWebOnline ? 1 : 0), position + 1  + (showWebOnline ? 1 : 0));
            } else {
                if (hasPinChanged) {
                    pinNum--;
                }
                rows.add(pinNum, row);
                notifyItemMoved(position  + (showWebOnline ? 1 : 0), pinNum + (showWebOnline ? 1 : 0));
                if (position < pinNum) {
                    notifyItemRangeChanged(position  + (showWebOnline ? 1 : 0), pinNum - position + 1 + (showWebOnline ? 1 : 0));
                } else {
                    notifyItemRangeChanged(pinNum  + (showWebOnline ? 1 : 0), position - pinNum + 1 + (showWebOnline ? 1 : 0));
                }
            }
        } else { // new one
            if (row.getNotification().getStatus() != MessageDataProcess.Status.NONE.ordinal()){
                row.getNotification().setIsPinned(false);
            }
            if (row.getNotification().getIsPinned()) {
                pinNum++;
                layoutManager.scrollToPosition(0);
                rows.add(0, row);
                notifyItemInserted(0 + (showWebOnline ? 1 : 0));
            } else {
                rows.add(pinNum, row);
                layoutManager.scrollToPosition(pinNum);
                notifyItemInserted(pinNum + (showWebOnline ? 1 : 0));
            }
        }
    }

    public void removeOne(NotificationRow row) {
        int position = -1;
        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).getNotification().get_id().equals(row.getNotification().get_id())) {
                position = i;
                break;
            }
        }
        if (position != -1) {
            if (row.getNotification().getIsPinned()) {
                pinNum--;
            }
            rows.remove(position);
            notifyItemRemoved(position + (showWebOnline ? 1 : 0));
        }
        String draftId = row.getNotification().get_targetId();
        DraftRealm.getInstance().remove(BizLogic.getTeamId() + draftId)
                .subscribe(new EmptyAction<>(), new RealmErrorAction());
    }

    public void removeAll() {
        rows.clear();
        notifyDataSetChanged();
    }

    public void addToEnd(List<NotificationRow> rows) {
        int position = this.rows.size();
        this.rows.addAll(position, rows);
        notifyItemRangeInserted(position  + (showWebOnline ? 1 : 0), rows.size() + (showWebOnline ? 1 : 0));
    }

    public void updateData(final List<NotificationRow> rows, final int pinNum) {
        this.pinNum = pinNum;
        final List<NotificationRow> pinneds = new ArrayList<>();
        DraftRealm.getInstance().getDrafts()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Draft>>() {
                    @Override
                    public void call(List<Draft> drafts) {
                        NotificationAdapter.this.rows.clear();
                        for (int i = 0; i < rows.size(); i++) {
                            NotificationRow row = rows.get(i);
                            for (Draft draft : drafts) {
                                if (draft.get_id().equals(BizLogic.getTeamId() + row.getNotification().get_targetId())) {
                                    row.getNotification().setOutlineText(draft.getContent());
                                    row.getNotification().setDraftTempUpdateAt(row.getNotification().getUpdatedAt());
                                    row.getNotification().setUpdatedAt(draft.getUpdatedAt());
                                    break;
                                }
                            }
                            if (row.getNotification().getIsPinned()) {
                                pinneds.add(row);
                            } else {
                                NotificationAdapter.this.rows.add(row);
                            }
                        }
                        Collections.sort(NotificationAdapter.this.rows, new Comparator<NotificationRow>() {
                            @Override
                            public int compare(NotificationRow lhs, NotificationRow rhs) {
                                return rhs.getNotification().getUpdatedAt().compareTo(lhs.getNotification().getUpdatedAt());
                            }
                        });
                        Collections.sort(pinneds, new Comparator<NotificationRow>() {
                            @Override
                            public int compare(NotificationRow lhs, NotificationRow rhs) {
                                return rhs.getNotification().getUpdatedAt().compareTo(lhs.getNotification().getUpdatedAt());
                            }
                        });
                        NotificationAdapter.this.rows.addAll(0, pinneds);
                        notifyDataSetChanged();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        NotificationAdapter.this.rows.clear();
                        NotificationAdapter.this.rows.addAll(rows);
                        notifyDataSetChanged();
                    }
                });
    }

    public void setIsLoading(boolean isLoading) {
        if (this.isLoading != isLoading) {
            this.isLoading = isLoading;
            if (isLoading) {
                notifyItemInserted(rows.size());
            } else {
                notifyItemRemoved(rows.size());
            }
        }
    }

    public void changeMutePreference() {
        getPreference();
        notifyItemChanged(0);
    }

    public boolean isMute() {
        if (preference == null) {
            return false;
        } else {
            return preference.isMuteWhenWebOnline();
        }
    }

    public void setOnHeaderClickListener(OnHeaderClickListener onHeaderClickListener) {
        this.onHeaderClickListener = onHeaderClickListener;
    }

    public void showWebOnlineHeader(boolean showHeader) {
        if (showHeader != this.showWebOnline) {
            this.showWebOnline = showHeader;
            if (showHeader) {
                notifyItemInserted(0);
            } else {
                notifyItemRemoved(0);
            }
        }
    }

    public boolean isShowWebOnline() {
        return showWebOnline;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_LOADING:
                return new LoadingHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_loading_white, null));
            case VIEW_TYPE_WEBONLINE:
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_webonline, null);
                return new WebonlineHeaderHolder(view);
            case VIEW_TYPE_NOTIFICATION:
            default:
            return NotificationRow.createViewHolder(parent);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_NOTIFICATION) {
            rows.get(position - (showWebOnline ? 1 : 0)).bindViewHolder((NotificationRow.NotificationHolder) holder);
        } else if (getItemViewType(position) == VIEW_TYPE_WEBONLINE) {
            if (preference != null) {
                ((WebonlineHeaderHolder) holder).mute.setImageResource(preference.isMuteWhenWebOnline()
                        ? R.drawable.ic_inbox_tab_unremind : R.drawable.ic_inbox_tab_ring);
            }
            ((WebonlineHeaderHolder) holder).background.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onHeaderClickListener != null) {
                        onHeaderClickListener.onHeaderClick();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return rows.size() + (isLoading ? 1 : 0) + (showWebOnline ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        if (!showWebOnline && isLoading && position == rows.size() || showWebOnline && isLoading && position == rows.size() + 1) {
            return VIEW_TYPE_LOADING;
        } else if (showWebOnline && position == 0) {
            return VIEW_TYPE_WEBONLINE;
        } else {
            return VIEW_TYPE_NOTIFICATION;
        }
    }

    static class LoadingHolder extends RecyclerView.ViewHolder {
        public LoadingHolder(View itemView) {
            super(itemView);
        }
    }

    static class WebonlineHeaderHolder extends RecyclerView.ViewHolder {
        @InjectView(R.id.background)
        View background;
        @InjectView(R.id.mute)
        ImageView mute;

        public WebonlineHeaderHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }
}
