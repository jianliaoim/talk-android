package com.teambition.talk.client;

/**
 * Created by nlmartian on 2/28/15.
 */
public class TalkException extends Throwable {
    private int errorCode;

    public TalkException(int errCode) {
        super();
        this.errorCode = errCode;
    }

    public TalkException(int errCode, Throwable e) {
        super(e);
        this.errorCode = errCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
