package com.teambition.talk.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
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
import android.widget.TextView;

import com.teambition.talk.BizLogic;
import com.teambition.talk.R;
import com.teambition.talk.adapter.SearchFavoritesAdapter;
import com.teambition.talk.client.data.SearchRequestData;
import com.teambition.talk.entity.Message;
import com.teambition.talk.presenter.SearchPresenter;
import com.teambition.talk.realm.MessageDataProcess;
import com.teambition.talk.ui.OnMessageClickExecutor;
import com.teambition.talk.ui.activity.FavoritesPhotoViewActivity;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.util.TransactionUtil;
import com.teambition.talk.view.SearchView;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by nlmartian on 6/1/15.
 */
public class SearchFavoritesFragment extends BaseFragment implements SearchView {

    @InjectView(R.id.et_keyword)
    EditText etKeyword;
    @InjectView(R.id.recyclerView)
    RecyclerView recyclerView;
    @InjectView(R.id.btn_clear)
    View vClear;
    @InjectView(R.id.empty_view)
    View emptyView;
    @InjectView(R.id.tv_empty_msg)
    TextView tvEmptyMsg;

    private SearchFavoritesAdapter adapter;
    private SearchPresenter presenter;
    private InputMethodManager imm;
    private SearchRequestData requestData = new SearchRequestData(BizLogic.getTeamId(), null);


    public static SearchFavoritesFragment getInstance() {
        SearchFavoritesFragment fragment = new SearchFavoritesFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new SearchFavoritesAdapter(getActivity(), new SearchFavoritesAdapter.SearchFavoritesListener() {
            @Override
            public void onFavoriteClick(Message message) {
                final Message msg = message;
                if (msg != null) {
                    new OnMessageClickExecutor(getActivity(), msg) {
                        @Override
                        public void onImageClick(Context context, Message message) {
                            SearchRequestData d = requestData.copy();
                            int imgCount = 0;
                            int page = 1;
                            for (int i = 0; i < adapter.getItemCount(); i++) {
                                if (MessageDataProcess.getInstance().getFile(message) != null && "image".equals(MessageDataProcess.getInstance().getFile(message)
                                        .getFileCategory())) {
                                    imgCount++;
                                    if (msg.get_id().equals(message.get_id())) {
                                        break;
                                    }
                                }
                            }
                            if (imgCount != 0) {
                                page = imgCount / d.limit + (imgCount % d.limit == 0 ? 0 : 1);
                            }
                            d.fileCategory = "image";
                            d.page = page;
                            Bundle bundle = new Bundle();
                            bundle.putString("msgId", msg.get_id());
                            bundle.putParcelable("data", Parcels.wrap(d));
                            TransactionUtil.goTo(getActivity(), FavoritesPhotoViewActivity.class,
                                    bundle);
                        }
                    }.execute();
                }
            }
        });
        presenter = new SearchPresenter(this);
        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.inject(this, view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                hideKeyboard();
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        etKeyword.addTextChangedListener(new TextWatcher() {
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
                    emptyView.setVisibility(View.GONE);
                } else {
                    vClear.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        });

        etKeyword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_ENTER) &&
                        StringUtil.isNotBlank(etKeyword.getText().toString())) {
                    requestData.setKeyword(etKeyword.getText().toString());
                    presenter.searchFavorites(requestData);
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
                break;
            case R.id.btn_clear:
                etKeyword.setText("");
                adapter.updateSearchResult(new ArrayList<Message>());
                break;
        }
    }

    @Override
    public void onSearchFinish(List<Message> messages) {
        if (messages.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            tvEmptyMsg.setText(getString(R.string.search_result_no_favorite, etKeyword.getText().toString()));
        } else {
            emptyView.setVisibility(View.GONE);
        }
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
