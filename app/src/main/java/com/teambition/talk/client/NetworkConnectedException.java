package com.teambition.talk.client;

/**
 * Created by wlanjie on 15/7/23.
 */
public class NetworkConnectedException extends Exception {

    final String message;

    public NetworkConnectedException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message + " \n " + super.getMessage();
    }
}
