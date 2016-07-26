package com.teambition.talk.ui.fragment;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.teambition.talk.R;
import com.teambition.talk.ui.widget.CodeInputView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class VerificationCodeFragment extends Fragment {
    public static final String TAG = "VerificationCodeFragment";

    private static final String ARG_RANDOM_CODE = "arg_random_code";
    private static final String ARG_IS_MOBILE = "arg_is_mobile";

    private final static int RESTORATION_TIME = 1000;
    private static int TIMER = 60;

    static TimerRunnable timerRunnable = new TimerRunnable();
    static Handler timerHandler = new Handler();


    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.code_input)
    CodeInputView codeInputView;
    @InjectView(R.id.btn_next)
    Button bntNext;
    @InjectView(R.id.tv_resend_vcode)
    TextView tvResend;

    private String randomCode;
    private boolean isMobile;

    private OnFragmentInteractionListener mListener;

    public static VerificationCodeFragment newInstance(String randomCode, boolean isMobile) {
        VerificationCodeFragment fragment = new VerificationCodeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_RANDOM_CODE, randomCode);
        args.putBoolean(ARG_IS_MOBILE, isMobile);
        fragment.setArguments(args);
        return fragment;
    }

    public static VerificationCodeFragment newInstance(String randomCode) {
        return VerificationCodeFragment.newInstance(randomCode, true);
    }

    public VerificationCodeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            randomCode = getArguments().getString(ARG_RANDOM_CODE);
            isMobile = getArguments().getBoolean(ARG_IS_MOBILE, true);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_verification_code, container, false);
        ButterKnife.inject(this, view);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.input_v_code_title);
        setTextChangeListener();

        timerRunnable.setTextView(tvResend);
        timerHandler.postDelayed(timerRunnable, RESTORATION_TIME);
        return view;
    }



    @OnClick({R.id.tv_resend_vcode, R.id.btn_next})
    public void onClick(View view) {
        if (view.getId() == R.id.tv_resend_vcode) {
            if (mListener != null) {
                mListener.resendVerifyCode(isMobile);
            }
        } else if (view.getId() == R.id.btn_next) {
            if (mListener != null) {
                mListener.onGetVerifyCode(isMobile, randomCode, codeInputView.getText().toString());
            }
        }
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
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
        timerHandler.removeCallbacks(timerRunnable);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
        return super.onOptionsItemSelected(item);
    }

    private void setTextChangeListener() {
        codeInputView.setTextChangeListener(new CodeInputView.TextChangeListener() {
            @Override
            public void onTextChange(String s) {
                bntNext.setEnabled(s.length() == codeInputView.getCodeSize());
            }
        });
        codeInputView.setOnDeleteKeyListener(new CodeInputView.OnDeleteKeyListener() {
            @Override
            public void onDeleteKey(int length) {
                bntNext.setEnabled(length == codeInputView.getCodeSize());
            }
        });
    }

    public interface OnFragmentInteractionListener {
        public void onGetVerifyCode(boolean isMobile, String randomCode, String vCode);
        public void resendVerifyCode(boolean isMobile);
    }


    static class TimerRunnable implements Runnable {

        TextView textView;

        @Override
        public void run() {
            if (textView == null) return;
            TIMER--;
            if (TIMER == 0) {
                TIMER = 60;
                textView.setEnabled(true);
                textView.setText(R.string.resend);
                textView.setTextColor(textView.getResources().getColor(R.color.colorPrimary));
                timerHandler.removeCallbacks(this);
            } else {
                textView.setEnabled(false);
                textView.setText(String.format(textView.getResources().getString(R.string.resend_countdown), TIMER));
                textView.setTextColor(textView.getResources().getColor(R.color.material_grey_500));
                timerHandler.postDelayed(this, RESTORATION_TIME);
            }
        }

        public void setTextView(TextView textView) {
            this.textView = textView;
        }
    }
}
