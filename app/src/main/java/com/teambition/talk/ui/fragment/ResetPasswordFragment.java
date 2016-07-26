package com.teambition.talk.ui.fragment;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.teambition.talk.R;
import com.teambition.talk.rx.ApiErrorAction;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.entity.User;
import com.teambition.talk.ui.activity.ChooseTeamActivity;
import com.teambition.talk.ui.activity.RegisterActivity;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.util.TransactionUtil;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class ResetPasswordFragment extends Fragment {
    public static final String TAG = "ResetPasswordFragment";

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.et_password)
    EditText etPassword;
    @InjectView(R.id.et_password2)
    EditText etEtPassword2;

    private OnFragmentInteractionListener mListener;

    public static ResetPasswordFragment newInstance() {
        ResetPasswordFragment fragment = new ResetPasswordFragment();
        return fragment;
    }

    public ResetPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reset_password, container, false);
        ButterKnife.inject(this, view);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.reset_password);
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @OnClick({R.id.btn_reset})
    public void onClick(View view) {
        if (view.getId() == R.id.btn_reset) {
            String passwordStr = etPassword.getText().toString();
            String passwordStr2 = etEtPassword2.getText().toString();
            if (StringUtil.isBlank(passwordStr)) {
                Toast.makeText(getActivity(), R.string.password_required, Toast.LENGTH_SHORT).show();
                return;
            }
            if (!passwordStr.equals(passwordStr2)) {
                Toast.makeText(getActivity(), R.string.password_not_equal, Toast.LENGTH_SHORT).show();
                return;
            }

            TalkClient.getInstance().getAccountApi()
                    .resetPassword(passwordStr)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<User>() {
                        @Override
                        public void call(User user) {
                            RegisterActivity.initUserData(getActivity(), user);
                            TransactionUtil.goTo(getActivity(), ChooseTeamActivity.class, true);
                        }
                    }, new ApiErrorAction());
        }
    }

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

}
