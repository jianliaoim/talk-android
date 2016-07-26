package com.teambition.talk.client.apis;

import com.teambition.talk.client.data.ValidCaptchaResponseData;
import com.teambition.talk.entity.Captcha;

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by nlmartian on 1/8/16.
 */
public interface TbAuthApi {

    @GET("/captcha/setup")
    public Observable<Captcha> setUpCaptcha(@Query("num") int num, @Query("lang") String lang);

    @GET("/captcha/valid")
    public Observable<ValidCaptchaResponseData> validCaptcha(@Query("uid") String uid, @Query("value") String value);
}
