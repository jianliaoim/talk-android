package com.teambition.talk.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;
import com.talk.dialog.TalkDialog;
import com.teambition.talk.BizLogic;
import com.teambition.talk.BusProvider;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.adapter.NotificationAdapter;
import com.teambition.talk.entity.Draft;
import com.teambition.talk.entity.Notification;
import com.teambition.talk.entity.Room;
import com.teambition.talk.event.ClearNotificationUnreadEvent;
import com.teambition.talk.event.NetworkEvent;
import com.teambition.talk.event.DraftEvent;
import com.teambition.talk.event.RemoveNotificationEvent;
import com.teambition.talk.event.UpdateNotificationEvent;
import com.teambition.talk.presenter.NotificationPresenter;
import com.teambition.talk.realm.NotificationRealm;
import com.teambition.talk.realm.DraftRealm;
import com.teambition.talk.rx.EmptyAction;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.ui.RowFactory;
import com.teambition.talk.ui.activity.ChatActivity;
import com.teambition.talk.ui.row.NotificationRow;
import com.teambition.talk.ui.widget.WrapContentLinearLayoutManager;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.util.TransactionUtil;
import com.teambition.talk.view.NotificationView;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.parceler.Parcels;

import java.util.Date;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by zeatual on 15/10/9.
 */
public class NotificationFragment extends BaseFragment implements NotificationView,
        SwipeRefreshLayout.OnRefreshListener, NotificationRow.OnClickListener, NotificationAdapter.OnHeaderClickListener {

    @InjectView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @InjectView(R.id.recyclerView)
    RecyclerView listView;
    @InjectView(R.id.placeholder_view)
    View placeHolder;
    @InjectView(R.id.progress_bar)
    View progressbar;

    private Date maxDate;
    private boolean canLoadMore;
    private NotificationPresenter presenter;
    private LinearLayoutManager layoutManager;
    private NotificationAdapter adapter;
    private Subscription spPollWebState;

    public static NotificationFragment getInstance() {
        return new NotificationFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        BusProvider.getInstance().register(this);
        presenter = new NotificationPresenter(this);
        layoutManager = new WrapContentLinearLayoutManager(activity);
        adapter = new NotificationAdapter(layoutManager);
        adapter.setOnHeaderClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        ButterKnife.inject(this, view);
        listView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity())
                .marginResId(R.dimen.contacts_text_left_margin, R.dimen.zero_dp)
                .build());
        listView.setLayoutManager(layoutManager);
        listView.setAdapter(adapter);
        listView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (canLoadMore && layoutManager.findLastVisibleItemPosition() > adapter.getItemCount() - 5) {
                    canLoadMore = false;
                    adapter.setIsLoading(true);
                    presenter.getMoreNotifications(maxDate);
                }
            }
        });
        swipeRefreshLayout.setOnRefreshListener(this);
        presenter.initNotifications(false);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        spPollWebState = presenter.getWebOnlineState();
    }

    @Override
    public void onPause() {
        if (spPollWebState != null && !spPollWebState.isUnsubscribed()) {
            spPollWebState.unsubscribe();
        }
        super.onPause();
    }

    @OnClick(R.id.go_to_general_button)
    public void onClick(View view) {
        if (view.getId() == R.id.go_to_general_button) {
            for (Room r : MainApp.globalRooms.values()) {
                if (r.get_teamId().equals(BizLogic.getTeamId()) && r.getIsGeneral()) {
                    Bundle data = new Bundle();
                    data.putParcelable(ChatActivity.EXTRA_ROOM, Parcels.wrap(r));
                    TransactionUtil.goTo(NotificationFragment.this, ChatActivity.class, data);
                    break;
                }
            }
        }
    }

    @Override
    public void onDetach() {
        BusProvider.getInstance().unregister(this);
        super.onDetach();
    }

    @Override
    public void onInitNotifications(List<Notification> notifications, int pinNum) {
        swipeRefreshLayout.setRefreshing(false);
        progressbar.setVisibility(View.GONE);
        if (notifications.isEmpty()) {
            placeHolder.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        } else {
            placeHolder.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            adapter.updateData(RowFactory.getInstance()
                    .makeNotificationRows(notifications, this), pinNum);
            if (notifications.size() > 0) {
                maxDate = notifications.get(notifications.size() - 1).getUpdatedAt();
            }
            canLoadMore = notifications.size() > 0;
        }
    }

    @Override
    public void onInitNotificationsFailed() {
        swipeRefreshLayout.setRefreshing(false);
        progressbar.setVisibility(View.GONE);
    }

    @Override
    public void onLoadMoreNotifications(List<Notification> notifications) {
        adapter.setIsLoading(false);
        adapter.addToEnd(RowFactory.getInstance().makeNotificationRows(notifications, this));
        maxDate = notifications.get(notifications.size() - 1).getUpdatedAt();
        canLoadMore = notifications.size() >= 20;
    }

    @Override
    public void onLoadMoreNotificationsFailed() {
        adapter.setIsLoading(false);
    }

    @Override
    public void onPinSucceed(Notification notification) {
        adapter.updateOne(RowFactory.getInstance().makeNotificationRow(notification, this));
    }

    @Override
    public void onMuteSucceed(Notification notification) {
        adapter.updateNotification(RowFactory.getInstance().makeNotificationRow(notification, this));
    }

    @Override
    public void onClearUnreadSucceed(Notification notification) {
        adapter.clearUnread(RowFactory.getInstance().makeNotificationRow(notification, this));
        String targetId = "";
        if (notification.getMember() != null) {
            targetId = notification.getMember().get_id();
        } else if (notification.getRoom() != null) {
            targetId = notification.getRoom().get_id();
        } else if (notification.getStory() !=  null) {
            targetId = notification.getStory().get_id();
        }
        NotificationRealm.getInstance().clearUnreadWithTargetId(targetId)
                .subscribe(new EmptyAction<Void>(), new RealmErrorAction());
    }

    @Override
    public void onGetWebState(boolean webOnline) {
        boolean previousState = adapter.isShowWebOnline();
        adapter.showWebOnlineHeader(webOnline);
        if (!previousState && webOnline) {
            try {
                listView.smoothScrollToPosition(0);
            } catch (Exception e) {

            }
        }
    }

    @Override
    public void onUpdatePreference() {
        adapter.changeMutePreference();
    }

    @Override
    public void onClick(Notification notification) {
        Bundle bundle = new Bundle();
        if (notification.getMember() != null) {
            bundle.putParcelable(ChatActivity.EXTRA_MEMBER, Parcels.wrap(notification.getMember()));
        } else if (notification.getRoom() != null) {
            bundle.putParcelable(ChatActivity.EXTRA_ROOM, Parcels.wrap(notification.getRoom()));
        } else if (notification.getStory() != null) {
            bundle.putParcelable(ChatActivity.EXTRA_STORY, Parcels.wrap(notification.getStory()));
        }
        TransactionUtil.goTo(this, ChatActivity.class, bundle);

    }

    @Override
    public void onLongClick(final Notification notification) {
        new TalkDialog.Builder(getActivity())
                .items(new CharSequence[]{notification.getIsPinned() ? getString(R.string.unpin_recent) :
                        getString(R.string.pin_recent), notification.getIsMute() ? getString(R.string.un_mute_recent) :
                        getString(R.string.mute_recent), getString(R.string.hide_recent)})
                .itemsCallback(new TalkDialog.ListCallback() {
                    @Override
                    public void onSelection(TalkDialog materialDialog, View view, int i,
                                            CharSequence charSequence) {
                        switch (i) {
                            case 0:
                                presenter.pinNotification(notification.get_id(), !notification.getIsPinned());
                                break;
                            case 1:
                                presenter.muteNotification(notification.get_id(), !notification.getIsMute());
                                break;
                            case 2:
                                presenter.removeNotification(notification);
                                break;
                        }
                    }
                }).show();
    }

    @Override
    public void onRefresh() {
        presenter.initNotifications(true);
    }

    @Subscribe
    public void onDraftEvent(DraftEvent event) {
        if (StringUtil.isNotBlank(event.content)) {
            final Draft draft = new Draft();
            draft.setTeamId(BizLogic.getTeamId());
            draft.set_id(BizLogic.getTeamId() + event.id);
            draft.setContent(event.content);
            draft.setUpdatedAt(new Date());
            DraftRealm.getInstance()
                    .addOrUpdate(draft)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Object>() {
                        @Override
                        public void call(Object o) {
                            if (o == null) return;
                            adapter.updateNotificationDraft(draft);
                        }
                    }, new RealmErrorAction());
        } else {
            final Draft draft = new Draft();
            draft.set_id(BizLogic.getTeamId() + event.id);
            DraftRealm.getInstance()
                    .remove(BizLogic.getTeamId() + event.id)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Object>() {
                        @Override
                        public void call(Object o) {
                            if (o == null) return;
                            adapter.updateNotificationDraft(draft);
                        }
                    }, new RealmErrorAction());
        }
    }

    @Subscribe
    public void onUpdateNotificationEvent(UpdateNotificationEvent event) {
        if (BizLogic.isCurrentTeam(event.notification.get_teamId())) {
            adapter.updateOne(RowFactory.getInstance().makeNotificationRow(event.notification, this));
            listView.scrollToPosition(0);
        }
    }

    @Subscribe
    public void onRemoveNotificationEvent(RemoveNotificationEvent event) {
        adapter.removeOne(RowFactory.getInstance().makeNotificationRow(event.notification, this));
    }

    @Subscribe
    public void oneNetworkEvent(NetworkEvent event) {
        switch (event.state) {
            case NetworkEvent.STATE_CONNECTED:
//                presenter.initNotifications(false);
                break;
        }
    }

    @Subscribe
    public void onClearUnreadEvent(ClearNotificationUnreadEvent event) {
        if (StringUtil.isBlank(event.targetId)) return;
        for (NotificationRow notificationRow : adapter.getRows()) {
            if (event.targetId.equals(notificationRow.getNotification().get_targetId())) {
                if (notificationRow.getNotification().getUnreadNum() > 0) {
                    presenter.clearUnread(notificationRow.getNotification());
                }
                break;
            }
        }
    }

    @Override
    public void onHeaderClick() {
        new TalkDialog.Builder(getContext())
                .title(R.string.tips)
                .titleColorRes(R.color.white)
                .titleBackgroundColorRes(R.color.talk_red)
                .content(R.string.talk_web_online)
                .negativeText(adapter.isMute() ? R.string.resume_notification_on_device
                        : R.string.stop_notification_on_device)
                .callback(new TalkDialog.ButtonCallback() {
                    @Override
                    public void onNegative(TalkDialog dialog) {
                        presenter.updateMuteConfig(!adapter.isMute());
                    }
                })
                .positiveText(R.string.cancel)
                .build()
                .show();
    }
}
