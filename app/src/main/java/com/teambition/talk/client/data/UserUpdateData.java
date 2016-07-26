package com.teambition.talk.client.data;

/**
 * Created by zeatual on 15/9/18.
 */
public class UserUpdateData {

    String name;
    String email;
    String avatarUrl;

    public UserUpdateData(String name, String email, String avatarUrl) {
        this.name = name;
        this.email = email;
        this.avatarUrl = avatarUrl;
    }
}
