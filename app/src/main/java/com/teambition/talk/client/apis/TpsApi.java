package com.teambition.talk.client.apis;

import com.google.gson.JsonArray;
import com.teambition.talk.client.data.RegisterTpsResponseData;


import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by nlmartian on 11/2/15.
 */
public interface TpsApi {

    @FormUrlEncoded
    @POST("/v1/users/register")
    public Observable<RegisterTpsResponseData> register(
            @Field("appKey") String appKey,
            @Field("userId") String userId,
            @Field("deviceToken") String deviceToken
    );

    @GET("/v1/messages/broadcasthistory")
    public Observable<JsonArray> getHistory(
            @Query("userId") String userId,
            @Query("channelId") String channelId,
            @Query("minDate") String minDate
    );
}
