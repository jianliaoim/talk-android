package com.teambition.talk.rx;

import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.client.data.ErrorResponseData;

import retrofit.RetrofitError;
import rx.functions.Action1;

/**
 * Created by zeatual on 15/9/7.
 */
public class ApiErrorAction implements Action1<Throwable> {
    @Override
    public void call(Throwable throwable) {
        if (throwable instanceof RetrofitError) {
            try {
                ErrorResponseData error = (ErrorResponseData) ((RetrofitError) throwable)
                        .getBodyAs(ErrorResponseData.class);
                MainApp.showToastMsg(error.message);
            } catch (Exception e) {
                MainApp.showToastMsg(R.string.network_failed);
            }
        }
        call();
    }

    protected void call() {
    }
}
