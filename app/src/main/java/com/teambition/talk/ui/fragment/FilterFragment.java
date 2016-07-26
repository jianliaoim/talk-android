package com.teambition.talk.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.teambition.talk.BizLogic;
import com.teambition.talk.R;
import com.teambition.talk.adapter.ChatSelectAdapter;
import com.teambition.talk.entity.ChatItem;
import com.teambition.talk.entity.FilterItem;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Room;
import com.teambition.talk.realm.MemberRealm;
import com.teambition.talk.realm.RoomRealm;
import com.teambition.talk.ui.activity.ItemsActivity;
import com.teambition.talk.util.Logger;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by ZZQ on 5/5/15.
 */
public class FilterFragment extends BaseFragment implements ChatSelectAdapter.OnItemClickListener {

    @InjectView(R.id.recyclerView)
    RecyclerView recyclerView;

    private InputMethodManager imm;
    private FilterListener listener;
    private ChatSelectAdapter adapter;

    public interface FilterListener {
        void onFilter(FilterItem filterItem);
    }

    public void setListener(FilterListener listener) {
        this.listener = listener;
    }

    public static FilterFragment getInstance(FilterListener listener) {
        FilterFragment f = new FilterFragment();
        f.setListener(listener);
        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean isShowAllDoneIcon = true;
        String showItemIconId = "";
        if (activity instanceof ItemsActivity) {
            isShowAllDoneIcon = ((ItemsActivity) activity).isShowAllDoneIcon;
            showItemIconId = ((ItemsActivity) activity).showItemIconId;
        }
        adapter = new ChatSelectAdapter(activity, isShowAllDoneIcon, showItemIconId);
        adapter.setShowHeader(true);
        adapter.setOnItemClickListener(this);
        adapter.setFilterListener(listener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filter, container, false);
        ButterKnife.inject(this, view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                hideKeyboard();
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        prepareData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(getClass().getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(getClass().getName());
    }

    @Override
    public void onItemClick(int position) {
        FilterItem item = adapter.getItem(position).convertToFilterItem();
        if (item != null) {
            listener.onFilter(item);
        }
    }

    private void showKeyboard() {
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public void hideKeyboard() {
        imm.hideSoftInputFromWindow(recyclerView.getWindowToken(), 0);
    }

    private void prepareData() {
        Observable.create(new Observable.OnSubscribe<List<ChatItem>>() {
            @Override
            public void call(Subscriber<? super List<ChatItem>> subscriber) {
                List<ChatItem> items = new ArrayList<>();
                try {
                    List<Room> rooms = RoomRealm.getInstance().getRoomOnNotQuitOnNotArchivedWithCurrentThread();
                    for (Room room : rooms) {
                        items.add(new ChatItem(room));
                    }
                    List<Member> members = MemberRealm.getInstance().getMemberWithCurrentThread();
                    Iterator<Member> iterator = members.iterator();
                    while (iterator.hasNext()) {
                        Member member = iterator.next();
                        if (!BizLogic.isXiaoai(member) && member.getIsRobot()) {
                            iterator.remove();
                        }
                    }
                    Collections.sort(members, new Comparator<Member>() {
                        @Override
                        public int compare(Member lhs, Member rhs) {
                            return lhs.getIsQuit().compareTo(rhs.getIsQuit());
                        }
                    });
                    for (Member member : members) {
                        items.add(new ChatItem(member));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                subscriber.onNext(items);
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<List<ChatItem>>() {
                    @Override
                    public void call(List<ChatItem> chatItems) {
                        adapter.updateData(chatItems);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Logger.d("Share Data", throwable.toString());
                    }
                });
    }
}
