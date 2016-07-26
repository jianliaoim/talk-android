package com.teambition.talk.util;

import android.content.Context;

import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

/**
 * Created by nlmartian on 12/1/15.
 */
public class WeChatHelper {

    public static final String APP_ID = "wx96b02f6ae129bae9";

    private IWXAPI wxApi;

    private static WeChatHelper INSTANCE;

    private WeChatHelper(Context ctx) {
        wxApi = WXAPIFactory.createWXAPI(ctx, APP_ID);
        wxApi.registerApp(APP_ID);
    }

    public static WeChatHelper getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new WeChatHelper(context);
        }
        return INSTANCE;
    }

    public void sendTextToTimeline(String text) {
        sendTextToWX(text, true);
    }

    public void sendTextToSession(String text) {
        sendTextToWX(text, false);
    }

    private void sendTextToWX(String text, boolean isTimeline) {
        WXTextObject textObject = new WXTextObject();
        textObject.text = text;
        WXMediaMessage msg = new WXMediaMessage(textObject);
        msg.description = text;

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("text");
        req.message = msg;
        req.scene = isTimeline ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        wxApi.sendReq(req);
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }
}
