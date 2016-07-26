package com.teambition.talk.ui.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.ui.activity.LocalContactsActivity;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by nlmartian on 1/27/16.
 * 添加手机联系人的提醒页面
 */
public class ContactsCoverFragment extends BaseFragment {
    public static final String BACK_STACK_TAG = "ContactsCoverFragment";

    private static final int READ_CONTACTS_PERMISSION = 0;

    @InjectView(R.id.toolbar)
    Toolbar toolbar;

    public ContactsCoverFragment() {
    }

    public static ContactsCoverFragment newInstance() {
        ContactsCoverFragment fragment = new ContactsCoverFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts_cover, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupToolbar();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().getSupportFragmentManager().popBackStack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.btn_add)
    public void onClick(View view) {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                MainApp.showToastMsg(R.string.record_contacts_permission_denied);
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_PERMISSION);
            }
        } else {
            try {
                getActivity().getSupportFragmentManager().popBackStack();
            } catch (Exception e) {
            }
            getActivity().startActivity(new Intent(getContext(), LocalContactsActivity.class));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case READ_CONTACTS_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                getActivity().getSupportFragmentManager().popBackStack();
                            } catch (Exception e) {
                            }
                        }
                    }, 3000);

                    getActivity().startActivity(new Intent(getContext(), LocalContactsActivity.class));
                }
                break;
        }
    }

    private void setupToolbar() {
        toolbar.setBackgroundColor(0x00ffffff);
        toolbar.setNavigationIcon(R.drawable.ic_action_close_contact);
        toolbar.setTitleTextColor(0xde0f3666);
        toolbar.setTitle(R.string.local_contacts);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
}
