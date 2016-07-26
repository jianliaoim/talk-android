package com.teambition.talk.client.apis;

import com.teambition.talk.client.data.call.CreateCallMeetingData;
import com.teambition.talk.client.data.call.DialBackData;
import com.teambition.talk.client.data.call.CallCancelData;
import com.teambition.talk.client.data.call.InviteJoinCallMeetingData;
import com.teambition.talk.client.data.call.QuitCallMeeting;
import com.teambition.talk.entity.call.CallResultRes;
import com.teambition.talk.entity.call.CreateCallMeeting;
import com.teambition.talk.entity.call.DialBack;
import com.teambition.talk.entity.call.InviteJoinCallMeeting;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by wlanjie on 15/9/12.
 */
public interface CallApi {

    @POST("/2013-12-26/SubAccounts/{subAccountSid}/Calls/Callback")
    Observable<DialBack> createDialBackCall(@Header("authorization") String authorization, @Path("subAccountSid") String subAccountSid, @Query("sig") String signature, @Body DialBackData data);

    @POST("/2013-12-26/SubAccounts/{subAccountSid}/Calls/CallCancel")
    Observable<Object> cancelDialBackCall(@Header("authorization") String authorization, @Path("subAccountSid") String subAccountSid, @Query("sig") String signature, @Body CallCancelData data);

    @POST("/2013-12-26/Accounts/{accountSid}/ivr/createconf?maxmember=300")
    Observable<CreateCallMeeting> createCallMeeting(@Header("authorization") String authorization, @Path("accountSid") String accountSid, @Query("sig") String signature, @Body CreateCallMeetingData data);

    @POST("/2013-12-26/Accounts/{accountSid}/ivr/conf")
    Observable<InviteJoinCallMeeting> inviteJoinCallMeeting(@Header("authorization") String authorization, @Path("accountSid") String accountSid, @Query("sig") String signature, @Query("confid") String confid, @Body InviteJoinCallMeetingData data);

    @POST("/2013-12-26/Accounts/{accountSid}/ivr/conf")
    Observable<Object> quitCallMeeting(@Header("authorization") String authorization, @Path("accountSid") String accountSid, @Query("sig") String signature, @Query("confid") String confid, @Body QuitCallMeeting data);

    @GET("/2013-12-26/Accounts/{accountSid}/CallResult")
    Observable<CallResultRes> getCallResult(@Header("authorization") String authorization, @Path("accountSid") String accountSid, @Query("sig") String signature, @Query("callsid") String callSid);
}
