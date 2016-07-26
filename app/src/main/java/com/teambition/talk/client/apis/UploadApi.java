package com.teambition.talk.client.apis;


import com.teambition.talk.client.data.FileUploadResponseData;

import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedFile;
import rx.Observable;

/**
 * Created by zeatual on 14-10-11.
 */
public interface UploadApi {

    @Multipart
    @POST("/upload")
    Observable<FileUploadResponseData> uploadFile(@Part("name") String name, @Part("type") String mimeType, @Part("size") long fileSize, @Part("file") TypedFile file);
}
