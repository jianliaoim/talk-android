package com.teambition.talk.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.teambition.talk.R;
import com.teambition.talk.ui.activity.FavoritesActivity;
import com.teambition.talk.ui.activity.ItemsActivity;
import com.teambition.talk.ui.activity.MentionedMeActivity;
import com.teambition.talk.ui.activity.TeamSettingActivity;
import com.teambition.talk.ui.activity.TeamTagActivity;
import com.teambition.talk.util.TransactionUtil;
import com.umeng.analytics.MobclickAgent;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by zeatual on 15/5/4.
 */
public class MoreFragment extends BaseFragment {

    public static MoreFragment getInstance() {
        return new MoreFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_more, container, false);
        ButterKnife.inject(this, view);
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

    @OnClick({R.id.tv_favorite, R.id.tv_item, R.id.tv_tag, R.id.tv_team_setting, R.id.tv_at_me})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_favorite:
                TransactionUtil.goTo(this, FavoritesActivity.class);
                break;
            case R.id.tv_item:
                TransactionUtil.goTo(this, ItemsActivity.class);
                break;
            case R.id.tv_tag:
                TransactionUtil.goTo(this, TeamTagActivity.class);
                break;
            case R.id.tv_at_me:
                TransactionUtil.goTo(this, MentionedMeActivity.class);
                break;
            case R.id.tv_team_setting:
                TransactionUtil.goTo(this, TeamSettingActivity.class);
                break;
        }
    }
}