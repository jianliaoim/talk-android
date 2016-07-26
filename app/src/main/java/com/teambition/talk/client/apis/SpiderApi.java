package com.teambition.talk.client.apis;

import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

/**
 * Created by zeatual on 15/12/3.
 */
public interface SpiderApi {

    @GET("/api/track")
    Observable<Object> spider(@Query("data") String data);

}
