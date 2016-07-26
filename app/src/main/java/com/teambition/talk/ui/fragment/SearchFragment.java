package com.teambition.talk.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.teambition.talk.R;
import com.teambition.talk.imageloader.RecyclerViewPauseOnScrollListener;
import com.teambition.talk.adapter.SearchResultAdapter;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Message;
import com.teambition.talk.entity.Room;
import com.teambition.talk.presenter.SearchPresenter;
import com.teambition.talk.ui.activity.ChatActivity;
import com.teambition.talk.ui.activity.MessageSearchActivity;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.view.SearchView;
import com.umeng.analytics.MobclickAgent;

import org.parceler.Parcels;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by ZZQ on 3/26/15.
 */
public class SearchFragment extends BaseFragment implements SearchView, TextWatcher,
        SearchResultAdapter.SearchListener {

    @InjectView(R.id.et_keyword)
    EditText etKeyword;
    @InjectView(R.id.recyclerView)
    RecyclerView recyclerView;
    @InjectView(R.id.btn_clear)
    View vClear;

    private SearchPresenter presenter;
    private SearchResultAdapter adapter;
    private SearchListener listener;
    private InputMethodManager imm;

    public interface SearchListener {
        void onDismiss();
    }

    public void setListener(SearchListener listener) {
        this.listener = listener;
    }

    public static SearchFragment getInstance(SearchListener listener) {
        SearchFragment f = new SearchFragment();
        f.setListener(listener);
        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        adapter = new SearchResultAdapter(activity, this);
        presenter = new SearchPresenter(this);
        imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.inject(this, view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(mOnScrollListener);

        etKeyword.addTextChangedListener(this);
        etKeyword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_ENTER) &&
                        StringUtil.isNotBlank(etKeyword.getText().toString())) {
                    adapter.search();
                    return true;
                }
                return false;
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showKeyboard();
            }
        }, 200);
        return view;
    }

    final RecyclerViewPauseOnScrollListener mOnScrollListener = new RecyclerViewPauseOnScrollListener(new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            hideKeyboard();
        }
    });

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        recyclerView.removeOnScrollListener(mOnScrollListener);
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

    @OnClick({R.id.background, R.id.btn_back, R.id.btn_clear})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_back:
            case R.id.background:
                hideKeyboard();
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(R.anim.anim_fade_in, R.anim.anim_fade_out)
                        .remove(this)
                        .commit();
                getActivity().getSupportFragmentManager().popBackStack();
                listener.onDismiss();
                break;
            case R.id.btn_clear:
                etKeyword.setText("");
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (StringUtil.isBlank(s.toString())) {
            vClear.setVisibility(View.GONE);
            recyclerView.setVisibility(View.INVISIBLE);
        } else {
            vClear.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.filter(s.toString());
        }
    }

    @Override
    public void search(String keyword) {
        hideKeyboard();
        presenter.searchMessages(keyword);
    }

    @Override
    public void onMemberClick(Member member) {
        hideKeyboard();
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_MEMBER, Parcels.wrap(member));
        getActivity().startActivity(intent);
        getActivity().getSupportFragmentManager().popBackStack();
        listener.onDismiss();

    }

    @Override
    public void onRoomClick(Room room) {
        hideKeyboard();
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        if (room.getIsQuit()) {
            intent.putExtra(ChatActivity.IS_PREVIEW, true);
        }
        intent.putExtra(ChatActivity.EXTRA_ROOM, Parcels.wrap(room));
        getActivity().startActivity(intent);
        getActivity().getSupportFragmentManager().popBackStack();
        listener.onDismiss();

    }

    @Override
    public void onMessageClick(Message message) {
        hideKeyboard();
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_MESSAGE, Parcels.wrap(message));
        if (message.getStory() != null) {
            intent.putExtra(ChatActivity.EXTRA_STORY, Parcels.wrap(message.getStory()));
        }
        getActivity().startActivity(intent);
        getActivity().getSupportFragmentManager().popBackStack();
        listener.onDismiss();

    }

    @Override
    public void onMoreClick() {
        Intent intent = new Intent(getActivity(), MessageSearchActivity.class);
        intent.putExtra(MessageSearchActivity.KEY, etKeyword.getText().toString());
        startActivity(intent);
    }

    @Override
    public void onSearchFinish(List<Message> messages) {
        adapter.updateSearchResult(messages);
    }

    @Override
    public void onDeleteMessageSuccess(String messageId) {

    }

    @Override
    public void onDownloadFinish(String path) {

    }

    @Override
    public void onDownloadProgress(Integer progress) {

    }

    private void showKeyboard() {
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public void hideKeyboard() {
        imm.hideSoftInputFromWindow(etKeyword.getWindowToken(), 0);
    }
}
