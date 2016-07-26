package com.teambition.talk.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.teambition.talk.R;
import com.teambition.talk.adapter.TeamActivitiesAdapter;
import com.teambition.talk.entity.TeamActivity;
import com.teambition.talk.event.NewTeamActivityEvent;
import com.teambition.talk.event.RemoveTeamActivityEvent;
import com.teambition.talk.event.UpdateTeamActivityEvent;
import com.teambition.talk.imageloader.RecyclerViewPauseOnScrollListener;
import com.teambition.talk.presenter.TeamActivitiesPresenter;
import com.teambition.talk.ui.activity.ChatActivity;
import com.teambition.talk.ui.widget.WrapContentLinearLayoutManager;
import com.teambition.talk.util.TransactionUtil;
import com.teambition.talk.view.TeamActivitiesView;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.parceler.Parcels;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by nlmartian on 3/8/16.
 */
public class TeamActivityFragment extends BaseFragment implements TeamActivitiesView,
        TeamActivitiesAdapter.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    @InjectView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @InjectView(R.id.listView)
    RecyclerView listView;
    @InjectView(R.id.layout_placeholder)
    View placeholder;
    @InjectView(R.id.progress_bar)
    View loadingView;

    private TeamActivitiesPresenter presenter;
    private LinearLayoutManager layoutManager;
    private TeamActivitiesAdapter adapter;
    private String maxId;
    private boolean canLoadMore;

    public static TeamActivityFragment newInstance() {
        return new TeamActivityFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onDetach() {
        BusProvider.getInstance().unregister(this);
        super.onDetach();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_team_activities, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        initView();
        presenter = new TeamActivitiesPresenter(this);
    }

    @Override
    protected void lazyLoad() {
        if (maxId == null) {
            swipeRefreshLayout.setRefreshing(true);
            presenter.getTeamActivities(BizLogic.getTeamId(), null);
        }
    }

    private void initView() {
        layoutManager = new WrapContentLinearLayoutManager(getActivity());
        adapter = new TeamActivitiesAdapter(this);
        listView.setLayoutManager(layoutManager);
        listView.setAdapter(adapter);
        listView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity())
                .marginResId(R.dimen.contacts_text_left_margin, R.dimen.zero_dp)
                .build());
        listView.addOnScrollListener(new RecyclerViewPauseOnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (canLoadMore && BizLogic.isNetworkConnected()
                        && layoutManager.findLastVisibleItemPosition() > adapter.getItemCount() - 5) {
                    canLoadMore = false;
                    adapter.setIsLoading(true);
                    presenter.getTeamActivities(BizLogic.getTeamId(), maxId);
                }
            }
        });
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void showActivities(List<TeamActivity> activities, boolean refresh) {
        swipeRefreshLayout.setRefreshing(false);
        loadingView.setVisibility(View.GONE);
        if (activities.isEmpty()) {
            placeholder.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.VISIBLE);
            if (refresh) {
                adapter.initData(activities);
            } else {
                adapter.addToEnd(activities);
            }
            maxId = activities.get(activities.size() - 1).get_id();
            canLoadMore = activities.size() >= 30;
            adapter.setIsLoading(false);
        }
    }

    @Override
    public void showActivitiesFailed() {
        swipeRefreshLayout.setRefreshing(false);
        adapter.setIsLoading(false);
        loadingView.setVisibility(View.GONE);
        placeholder.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRefresh() {
        maxId = null;
        presenter.getTeamActivities(BizLogic.getTeamId(), null);
    }

    @Override
    public void removeActivity(String id) {
        adapter.removeActivity(id);
    }

    @Override
    public void onClick(TeamActivity activity) {
        Bundle bundle = new Bundle();
        if ("room".equals(activity.getType())) {
            if (activity.getRoom() != null) {
                bundle.putParcelable(ChatActivity.EXTRA_ROOM, Parcels.wrap(activity.getRoom()));
                TransactionUtil.goTo(this, ChatActivity.class, bundle);
            }
        } else if ("story".equals(activity.getType())) {
            if (activity.getStory() != null) {
                bundle.putParcelable(ChatActivity.EXTRA_STORY, Parcels.wrap(activity.getStory()));
                TransactionUtil.goTo(this, ChatActivity.class, bundle);
            }
        }
    }

    @Override
    public void onLongClick(final TeamActivity activity) {
        if (BizLogic.isAdmin() || BizLogic.isMe(activity.get_creatorId())) {
            new TalkDialog.Builder(getActivity())
                    .items(new CharSequence[]{getString(R.string.delete)})
                    .itemsCallback(new TalkDialog.ListCallback() {
                        @Override
                        public void onSelection(TalkDialog dialog, View itemView, int which, CharSequence text) {
                            presenter.removeTeamActivity(activity.get_id());
                        }
                    }).show();
        }
    }

    @Subscribe
    public void onNewActivityEvent(NewTeamActivityEvent event) {
        adapter.addToTop(event.activity);
    }

    @Subscribe
    public void onUpdateActivityEvent(UpdateTeamActivityEvent event) {
        adapter.updateItem(event.activity);
    }

    @Subscribe
    public void onRemoveActivityEvent(RemoveTeamActivityEvent event) {
        if (event.activity != null) {
            adapter.removeActivity(event.activity.get_id());
        }
    }
}
