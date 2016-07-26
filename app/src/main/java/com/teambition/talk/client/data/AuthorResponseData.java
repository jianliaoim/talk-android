package com.teambition.talk.client.data;

/**
 * Created by zeatual on 14-10-11.
 */
public class AuthorResponseData {

    private String access_token;
    private String refresh_token;

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }
}
