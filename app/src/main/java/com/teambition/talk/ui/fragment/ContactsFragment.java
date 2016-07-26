package com.teambition.talk.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;
import com.talk.dialog.TalkDialog;
import com.teambition.talk.BizLogic;
import com.teambition.talk.BusProvider;
import com.teambition.talk.Constant;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.imageloader.RecyclerViewPauseOnScrollListener;
import com.teambition.talk.adapter.MemberAdapter;
import com.teambition.talk.entity.Member;
import com.teambition.talk.event.SyncLeaveMemberFinisEvent;
import com.teambition.talk.event.UpdateMemberEvent;
import com.teambition.talk.presenter.MemberPresenter;
import com.teambition.talk.ui.MemberInfoDialog;
import com.teambition.talk.util.ThemeUtil;
import com.teambition.talk.view.MemberView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by wlanjie on 15/10/27.
 */
public class ContactsFragment extends BaseFragment implements MemberView, MemberAdapter.OnItemClickListener {

    @InjectView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    MemberPresenter mPresenter;

    final MemberAdapter adapter = new MemberAdapter();

    private final RecyclerViewPauseOnScrollListener mListener = new RecyclerViewPauseOnScrollListener(MainApp.IMAGE_LOADER, true, true, null);

    public static ContactsFragment getInstance() {
        return new ContactsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);
        mPresenter = new MemberPresenter(this);
        adapter.setListener(this);
        mRecyclerView.setAdapter(adapter);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        manager.setSmoothScrollbarEnabled(true);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addOnScrollListener(mListener);
        /*mRecyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(getActivity())
                .marginResId(R.dimen.contacts_text_left_margin, R.dimen.zero_dp)
                .visibilityProvider(new FlexibleDividerDecoration.VisibilityProvider() {
                    @Override
                    public boolean shouldHideDivider(int position, RecyclerView parent) {
                        if (position == 0 || position == 1) {
                            return false;
                        } else if (position == adapter.getItemCount() - 1) {
                            return true;
                        } else if (!adapter.getLeaveMembers().isEmpty() && position == adapter.getItemCount() - 2) {
                            return true;
                        } else {
                            Member before = adapter.getItems().get(position - 1);
                            Member after = adapter.getItems().get(position);
                            if (before.getAliasPinyin() == null && after.getAliasPinyin() == null) {
                                return true;
                            } else if (before.getAliasPinyin() == null && after.getAliasPinyin() != null) {
                                return false;
                            } else {
                                return adapter.getItems().get(position - 1).getAliasPinyin()
                                        .equals(adapter.getItems().get(position).getAliasPinyin());
                            }
                        }
                    }
                })
                .build());*/
        mPresenter.getMembers();
        mPresenter.getLeaveMembers();
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRecyclerView.removeOnScrollListener(mListener);
        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void onUpdateMemberEvent(UpdateMemberEvent event) {
        if (mPresenter != null) {
            mPresenter.getMembers();
            mPresenter.getLeaveMembers();
        }
    }

    @Subscribe
    public void onLeaveMemberFinish(SyncLeaveMemberFinisEvent event) {
        if (mPresenter != null) {
            mPresenter.getLeaveMembers();
        }
    }

    @Override
    public void onLoadMembersFinish(List<Member> members) {
        adapter.addItems(members);
    }

    @Override
    public void onLoadLeaveMembersFinish(List<Member> members) {
        adapter.addLeaveMembers(members);
    }

    @Override
    public void onClick(final Member member) {
        if (!member.isInvite()) {
            new MemberInfoDialog
                    .Builder(getActivity(), R.style.Talk_Dialog)
                    .setMember(member)
                    .show();
        } else {
            TalkDialog dialog = new TalkDialog.Builder(getActivity())
                    .title(R.string.not_visit_title)
                    .titleColorRes(R.color.white)
                    .titleBackgroundColorRes(ThemeUtil.getThemeColorRes(BizLogic.getTeamColor()))
                    .content(R.string.not_visit_content)
                    .positiveText(R.string.confirm)
                    .build();
            if (!BizLogic.isAdmin()) {
                if (MainApp.PREF_UTIL.getBoolean(Constant.IS_FIRST_CLICK_NOT_VISIT, true)) {
                    dialog.getBuilder()
                            .negativeColorRes(R.color.material_grey_700)
                            .negativeText(R.string.not_mention)
                            .callback(new TalkDialog.ButtonCallback() {
                                @Override
                                public void onNegative(TalkDialog dialog) {
                                    MainApp.PREF_UTIL.putBoolean(Constant.IS_FIRST_CLICK_NOT_VISIT, false);
                                }
                            })
                            .show();
                }
            } else {
                dialog.getBuilder()
                        .negativeColorRes(R.color.talk_red)
                        .negativeText(R.string.title_remove_member)
                        .callback(new TalkDialog.ButtonCallback() {
                            @Override
                            public void onNegative(TalkDialog dialog) {
                                mPresenter.deleteInvitation(member.get_id());
                            }
                        })
                        .show();
            }
        }
    }
}
