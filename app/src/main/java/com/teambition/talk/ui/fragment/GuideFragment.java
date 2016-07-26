package com.teambition.talk.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by zeatual on 15/5/19.
 */
public class GuideFragment extends BaseFragment {

    public static GuideFragment getInstance(int index) {
        GuideFragment f = new GuideFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("index", index);
        f.setArguments(bundle);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = new View(getActivity());
        view.setTag(getArguments().getInt("index", 0));
        return view;
    }
}
