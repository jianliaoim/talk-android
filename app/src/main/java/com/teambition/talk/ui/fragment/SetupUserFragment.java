package com.teambition.talk.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.makeramen.roundedimageview.RoundedImageView;
import com.teambition.talk.Constant;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.rx.ApiErrorAction;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.client.apis.TalkApi;
import com.teambition.talk.client.data.FileUploadResponseData;
import com.teambition.talk.client.data.StrikerTokenResponseData;
import com.teambition.talk.client.data.UserUpdateData;
import com.teambition.talk.entity.User;
import com.teambition.talk.ui.activity.ChooseTeamActivity;
import com.teambition.talk.ui.activity.SelectImageActivity;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.util.TransactionUtil;

import org.parceler.Parcels;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.mime.TypedFile;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class SetupUserFragment extends Fragment {

    public static final String TAG = "SetupUserFragment";

    private static final String ARG_USER = "arg_user";

    private User user;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.avatar)
    RoundedImageView ivAvatar;
    @InjectView(R.id.et_login)
    EditText etUserName;

    private OnFragmentInteractionListener mListener;

    public static SetupUserFragment newInstance(User user) {
        SetupUserFragment fragment = new SetupUserFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_USER, Parcels.wrap(user));
        fragment.setArguments(args);
        return fragment;
    }

    public SetupUserFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = Parcels.unwrap(getArguments().getParcelable(ARG_USER));
        }
        getStrikeToken();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setup_user, container, false);
        ButterKnife.inject(this, view);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.action_edit_profile);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SelectImageActivity.SELECT_IMAGES) {
                String path = data.getStringExtra(SelectImageActivity.IMAGE_PATH);
                uploadAvatar(path);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_done, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_done) {
            if (StringUtil.isNotBlank(etUserName.getText().toString())) {
                user.setName(etUserName.getText().toString());
                updateUserInfo();
                TransactionUtil.goTo(this, ChooseTeamActivity.class, true);
            }
        } else if (item.getItemId() == android.R.id.home) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getStrikeToken() {
        TalkClient.getInstance().getTalkApi().getStrikerToken().observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<StrikerTokenResponseData>() {
                    @Override
                    public void call(StrikerTokenResponseData responseData) {
                        if (responseData != null) {
                            MainApp.PREF_UTIL.putString(Constant.STRIKER_TOKEN, responseData.getToken());
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }

    private void uploadAvatar(final String localPath) {
        File file = new File(localPath);
        TalkClient.getInstance().getUploadApi()
                .uploadFile(file.getName(), "image/*", file.length(), new TypedFile("image/*", file))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<FileUploadResponseData>() {
                    @Override
                    public void call(FileUploadResponseData fileUploadResponseData) {
                        MainApp.IMAGE_LOADER.displayImage("file://" + localPath, ivAvatar);
                        user.setAvatarUrl(fileUploadResponseData.getThumbnailUrl());
                        updateUserInfo();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }

    private void updateUserInfo() {
        TalkApi api = TalkClient.getInstance().getTalkApi();
        api.updateUser(user.get_id(),
                new UserUpdateData(user.getName(), user.getEmail(), user.getAvatarUrl()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        // TODO: 11/10/15 save user data 
                    }
                }, new ApiErrorAction());
    }

    @OnClick({R.id.avatar})
    public void onClick(View view) {
        if (view.getId() == R.id.avatar) {
            Intent intent = new Intent(getActivity(), SelectImageActivity.class);
            startActivityForResult(intent, SelectImageActivity.SELECT_IMAGES);
        }
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

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

}
