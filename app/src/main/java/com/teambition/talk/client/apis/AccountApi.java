package com.teambition.talk.client.apis;

import com.teambition.talk.client.data.RandomCodeData;
import com.teambition.talk.entity.User;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by zeatual on 15/9/2.
 */
public interface AccountApi {

    @POST("/mobile/sendverifycode")
    Observable<RandomCodeData> sendVerifyCode(
            @Query("phoneNumber") String phoneNumber
    );

    @POST("/mobile/sendverifycode")
    Observable<RandomCodeData> sendVerifyCode(
            @Query("phoneNumber") String phoneNumber,
            @Query("uid") String uid
    );

    @POST("/mobile/sendverifycode")
    Observable<RandomCodeData> sendVerifyCode(
            @Query("phoneNumber") String phoneNumber,
            @Query("action") String action,
            @Query("uid") String uid
    );

    @POST("/mobile/sendverifycode")
    Observable<RandomCodeData> sendVerifyCode(
            @Query("phoneNumber") String phoneNumber,
            @Query("action") String action,
            @Query("password") String password,
            @Query("uid") String uid
    );

    @POST("/mobile/signinbyverifycode")
    Observable<User> signInByVerifyCodeWithMobile(
            @Query("randomCode") String randomCode,
            @Query("verifyCode") String verifyCode,
            @Query("action") String action
    );

    @POST("/email/signinbyverifycode")
    Observable<User> signInByVerifyCodeWithEmail(
            @Query("randomCode") String randomCode,
            @Query("verifyCode") String verifyCode,
            @Query("action") String action
    );

    @POST("/mobile/signin")
    Observable<User> mobileSignIn(@Query("phoneNumber") String phoneNumber,
                                  @Query("password") String password);

    @POST("/mobile/signup")
    Observable<User> mobileSignUp(
            @Query("phoneNumber") String phoneNumber,
            @Query("password") String password,
            @Query("randomCode") String randomCode,
            @Query("verifyCode") String verifyCode
    );

    @POST("/mobile/bind")
    Observable<User> bindMobile(@Query("randomCode") String randomCode,
                          @Query("verifyCode") String verifyCode);

    @POST("/mobile/change")
    Observable<User> change(@Query("randomCode") String randomCode,
                            @Query("verifyCode") String verifyCode);

    @POST("/mobile/forcebind")
    Observable<User> forceBind(@Query("bindCode") String bindCode);

    @POST("/mobile/resetpassword")
    Observable<User> resetPassword(@Query("newPassword") String newPassword);

    @POST("/union/signin/teambition")
    Observable<User> signInByTeambition(@Query("code") String code);

    @POST("/union/bind/teambition")
    Observable<User> bindTeambition(@Query("code") String code);

    @POST("/union/unbind/teambition")
    Observable<User> unbindTeambition();

    @POST("/union/forcebind/teambition")
    Observable<User> forceBindTeambition(@Query("bindCode") String bindCode);

    @GET("/user/accounts")
    Observable<List<User>> getAccounts();

    @POST("/email/bind")
    Observable<User> bindEmail(@Query("randomCode") String randomCode,
                                @Query("verifyCode") String verifyCode);

    @POST("/email/change")
    Observable<User> changeEmail(@Query("randomCode") String randomCode,
                            @Query("verifyCode") String verifyCode);

    @POST("/email/forcebind")
    Observable<User> forceBindEmail(@Query("bindCode") String bindCode);

    @POST("/email/signup")
    Observable<User> emailSignUp(
            @Query("emailAddress") String emailAddress,
            @Query("password") String password);

    @POST("/email/signin")
    Observable<User> emailSignIn(
            @Query("emailAddress") String emailAddress,
            @Query("password") String password
    );

    @POST("/email/sendverifycode")
    Observable<RandomCodeData> sendEmailVLink(
            @Query("emailAddress") String emailAddress,
            @Query("action") String action
    );

}
