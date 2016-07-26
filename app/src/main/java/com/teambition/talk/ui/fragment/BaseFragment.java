package com.teambition.talk.ui.fragment;

import android.app.ProgressDialog;
import android.support.v4.app.Fragment;
import android.view.View;

import com.teambition.talk.view.BaseView;

/**
 * Created by zeatual on 14/10/27.
 */
public class BaseFragment extends Fragment implements BaseView {

    protected ProgressDialog progressDialog;

    protected View progressBar;

    protected boolean isVisible;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(getUserVisibleHint()) {
            isVisible = true;
            onVisible();
        } else {
            isVisible = false;
            onInvisible();
        }
    }

    protected void onVisible() {
        lazyLoad();
    }

    protected void lazyLoad() {

    }

    protected void onInvisible() {}

    @Override
    public void showProgressDialog(int message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this.getActivity());
        }
        progressDialog.setMessage(getString(message));
        progressDialog.show();
    }

    @Override
    public void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }


    @Override
    public void showProgressBar() {
        if (progressBar == null)
            return;
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void dismissProgressBar() {
        if (progressBar == null)
            return;
        progressBar.setVisibility(View.GONE);
    }

}
