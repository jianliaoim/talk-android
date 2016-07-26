package com.teambition.talk.client.data;

import com.teambition.talk.entity.User;

/**
 * Created by zeatual on 15/5/27.
 */
public class Oauth2ResponseData {

    String accessToken;
    User user;

    public String getAccessToken() {
        return accessToken;
    }

    public User getUser() {
        return user;
    }
}
