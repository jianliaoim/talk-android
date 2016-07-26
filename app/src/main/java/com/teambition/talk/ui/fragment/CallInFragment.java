package com.teambition.talk.ui.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.otto.Subscribe;
import com.teambition.talk.BizLogic;
import com.teambition.talk.BusProvider;
import com.teambition.talk.client.data.call.SaveCallUsageData;
import com.teambition.talk.entity.call.CallResultRes;
import com.teambition.talk.imageloader.ImageLoaderConfig;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.client.ApiConfig;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.client.data.call.CallCancelData;
import com.teambition.talk.client.data.call.CreateCallMeetingConf;
import com.teambition.talk.client.data.call.CreateCallMeetingData;
import com.teambition.talk.client.data.call.DialBackData;
import com.teambition.talk.client.data.call.DismissConf;
import com.teambition.talk.client.data.call.InviteJoinCallMeetingConf;
import com.teambition.talk.client.data.call.InviteJoinCallMeetingData;
import com.teambition.talk.client.data.call.QuitCallMeeting;
import com.teambition.talk.entity.call.CreateCallMeeting;
import com.teambition.talk.entity.call.DialBack;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.call.InviteJoinCallMeeting;
import com.teambition.talk.event.PhoneEvent;
import com.teambition.talk.rx.ApiErrorAction;
import com.teambition.talk.rx.EmptyAction;
import com.teambition.talk.util.CallUtil;
import com.teambition.talk.util.Logger;
import com.teambition.talk.util.StringUtil;

import org.parceler.Parcels;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Notification;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by wlanjie on 15/9/12.
 */
public class CallInFragment extends BaseFragment {

    @InjectView(R.id.img)
    RoundedImageView mAvatarImage;

    @InjectView(R.id.name)
    TextView mNameText;

    @InjectView(R.id.meet_listen_call)
    TextView mMeetListenCallText;

    @InjectView(R.id.hang_up_call)
    ImageView mHangUpCallImage;

    @InjectView(R.id.hide_window)
    View mHideWindowView;

    private String mCallSid;

    private String mConfid;

    public static CallInFragment getInstance(Member member, String multiPhoneNumber) {
        CallInFragment fragment = new CallInFragment();
        Bundle args = new Bundle();
        args.putParcelable("member", Parcels.wrap(member));
        args.putString("multiPhoneNumber", multiPhoneNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_call_in, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void onPhoneListener(PhoneEvent event) {
        if (getActivity() != null) {
            try {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                Field stateSaved = fm.getClass().getDeclaredField("mStateSaved");
                stateSaved.setAccessible(true);
                stateSaved.set(fm, false);
                //getActivity().getSupportFragmentManager().popBackStack();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.inject(this, view);

        view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        mHideWindowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        if (getArguments() != null) {
            Bundle args = getArguments();
            Member member = Parcels.unwrap(args.getParcelable("member"));
            final String multiPhoneNumber = args.getString("multiPhoneNumber", "");
            if (member != null) {
                mNameText.setText(member.getAlias());
                if (!TextUtils.isEmpty(member.getAvatarUrl())) {
                    MainApp.IMAGE_LOADER.displayImage(member.getAvatarUrl(), mAvatarImage, ImageLoaderConfig.AVATAR_OPTIONS);
                }
                callSingle(member);
            }
            if (!TextUtils.isEmpty(multiPhoneNumber)) {
                createCallMeeting();
                mAvatarImage.setImageResource(R.drawable.ic_multi_call_avatar);
                int multiLength = multiPhoneNumber.split("#").length;
                mNameText.setText(String.format(getResources().getString(R.string.conference_call_number), multiLength));
            }
            mMeetListenCallText.setText(String.format(getResources().getString(R.string.meet_listen_call), MainApp.TALK_BUSINESS_CALL));

            mHangUpCallImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!TextUtils.isEmpty(multiPhoneNumber)) {
                        quitCallMeeting();
                    }
                    cancelCall();
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }
    }

    private void cancelCall() {
        try {
            if (!TextUtils.isEmpty(mCallSid)) {
                try {
                    String signature = CallUtil.getCallSignature(BizLogic.getUserInfo().getVoip().getSubAccountSid(), BizLogic.getUserInfo().getVoip().getSubToken());
                    String authorization = CallUtil.getAuthorization(BizLogic.getUserInfo().getVoip().getSubAccountSid());
                    TalkClient.getInstance().getCallApi()
                            .cancelDialBackCall(authorization, BizLogic.getUserInfo().getVoip().getSubAccountSid(), signature, new CallCancelData(ApiConfig.CALL_APP_ID, mCallSid, 0))
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<Object>() {
                                @Override
                                public void call(Object o) {

                                }
                            }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {

                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callSingle(Member member) {
        try {
            if (BizLogic.getUserInfo() != null && BizLogic.getUserInfo().getVoip() != null) {
                String signature = CallUtil.getCallSignature(BizLogic.getUserInfo().getVoip().getSubAccountSid(), BizLogic.getUserInfo().getVoip().getSubToken());
                String authorization = CallUtil.getAuthorization(BizLogic.getUserInfo().getVoip().getSubAccountSid());
                DialBackData data = new DialBackData();
                data.setFrom(BizLogic.getUserInfo().getPhoneForLogin());
                data.setTo(member.getPhoneForLogin());
                data.setCustomerSerNum(MainApp.TALK_BUSINESS_CALL);
                data.setFromSerNum(MainApp.TALK_BUSINESS_CALL);
                TalkClient.getInstance().getCallApi()
                        .createDialBackCall(authorization, BizLogic.getUserInfo().getVoip().getSubAccountSid(), signature, data)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<DialBack>() {
                            @Override
                            public void call(DialBack dialBack) {
                                if (dialBack != null && dialBack.getCallback() != null) {
                                    mCallSid = dialBack.getCallback().getCallSid();
                                    saveCallUsage(mCallSid);
                                }
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                try {
                                    getActivity().getSupportFragmentManager().popBackStack();
                                } catch (Exception e) {

                                }
                            }
                        });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void createCallMeeting() {
        try {
            String signature = CallUtil.getCallSignature(ApiConfig.CALL_ACCOUNT_SID, ApiConfig.CALL_ACCOUNT_TOKEN);
            String authorization = CallUtil.getAuthorization(ApiConfig.CALL_ACCOUNT_SID);
            TalkClient.getInstance()
                    .getCallApi()
                    .createCallMeeting(authorization, ApiConfig.CALL_ACCOUNT_SID, signature, new CreateCallMeetingData(ApiConfig.CALL_APP_ID, new CreateCallMeetingConf("300")))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<CreateCallMeeting>() {
                        @Override
                        public void call(CreateCallMeeting createCallMeeting) {
                            if (createCallMeeting != null && "000000".equals(createCallMeeting.getStatusCode())) {
                                inviteJoinCallMeeting(createCallMeeting);
                                mConfid = createCallMeeting.getConfid();
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {

                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void inviteJoinCallMeeting(CreateCallMeeting meeting) {
        try {
            String signature = CallUtil.getCallSignature(ApiConfig.CALL_ACCOUNT_SID, ApiConfig.CALL_ACCOUNT_TOKEN);
            String authorization = CallUtil.getAuthorization(ApiConfig.CALL_ACCOUNT_SID);
            InviteJoinCallMeetingConf conf = new InviteJoinCallMeetingConf(getArguments().getString("multiPhoneNumber"), meeting.getConfid());
            TalkClient.getInstance()
                    .getCallApi()
                    .inviteJoinCallMeeting(authorization, ApiConfig.CALL_ACCOUNT_SID, signature, meeting.getConfid(), new InviteJoinCallMeetingData(ApiConfig.CALL_APP_ID, conf))
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<InviteJoinCallMeeting>() {
                        @Override
                        public void call(InviteJoinCallMeeting inviteJoinCallMeeting) {
                            if (inviteJoinCallMeeting != null) {
                                mCallSid = inviteJoinCallMeeting.getCallSid();
                                saveCallUsage(mCallSid);
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Logger.e("CallInFragment", "invite join meeting", throwable);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void quitCallMeeting() {
        try {
            if (TextUtils.isEmpty(mCallSid) || TextUtils.isEmpty(mConfid)) return;
            String signature = CallUtil.getCallSignature(ApiConfig.CALL_ACCOUNT_SID, ApiConfig.CALL_ACCOUNT_TOKEN);
            String authorization = CallUtil.getAuthorization(ApiConfig.CALL_ACCOUNT_SID);
            DismissConf conf = new DismissConf();
            conf.setConfid(mConfid);
            QuitCallMeeting quit = new QuitCallMeeting(ApiConfig.CALL_APP_ID, conf);
            TalkClient.getInstance()
                    .getCallApi()
                    .quitCallMeeting(authorization, ApiConfig.CALL_ACCOUNT_SID, signature, mConfid, quit)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Object>() {
                        @Override
                        public void call(Object o) {

                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {

                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void saveCallUsage(String callSid) {
        if (StringUtil.isBlank(callSid)) {
            return;
        }
        List<String> callSids = new ArrayList<>();
        String[] arrSid = callSid.split("#");
        callSids.addAll(Arrays.asList(arrSid));
        TalkClient.getInstance().getTalkApi()
                .saveCallUsage(new SaveCallUsageData(BizLogic.getTeamId(), callSids))
                .subscribe(new EmptyAction(), new ApiErrorAction());
    }

    void getCallResult(String callSid) {
        String signature = CallUtil.getCallSignature(ApiConfig.CALL_ACCOUNT_SID, ApiConfig.CALL_ACCOUNT_TOKEN);
        String authorization = CallUtil.getAuthorization(ApiConfig.CALL_ACCOUNT_SID);
        TalkClient.getInstance()
                .getCallApi()
                .getCallResult(authorization, ApiConfig.CALL_ACCOUNT_SID, signature, callSid)
                .repeatWhen(new Func1<Observable<? extends Notification<?>>, Observable<?>>() {
                    @Override
                    public Observable<?> call(Observable<? extends Notification<?>> observable) {
                        return observable.delay(5, TimeUnit.SECONDS);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<CallResultRes>() {
                    @Override
                    public void call(CallResultRes callResult) {
                        Toast.makeText(getContext(), callResult.getStatusMsg(), Toast.LENGTH_SHORT).show();
                        if (callResult.getCallResult() != null) {
                            Toast.makeText(getContext(), callResult.getCallResult().getState() + "  ->"
                                    + callResult.getCallResult().getCallTime(), Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }
}