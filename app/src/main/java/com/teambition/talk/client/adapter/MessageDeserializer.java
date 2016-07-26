package com.teambition.talk.client.adapter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.teambition.talk.BizLogic;
import com.teambition.talk.GsonProvider;
import com.teambition.talk.entity.File;
import com.teambition.talk.entity.Message;
import com.teambition.talk.realm.MessageDataProcess;
import com.teambition.talk.util.Logger;

import java.lang.reflect.Type;

/**
 * Created by zeatual on 14/11/5.
 */
public class MessageDeserializer implements JsonDeserializer<Message> {

    private Gson mGson;

    public MessageDeserializer() {
        GsonProvider.Builder builder = new GsonProvider.Builder();
        mGson = builder.addDeserializationExclusionStrategy("attachments")
                .setStoryAdapter()
                .setRoomAdapter()
                .setDateAdapter()
                .setHighlightAdapter()
                .create();
    }

    @Override
    public Message deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        Message message;
        JsonObject jsonObj = json.getAsJsonObject();
        try {
            message = mGson.fromJson(json, Message.class);
            JsonElement jsonElement = jsonObj.get("attachments");
            if (jsonElement != null) {
                message.setAttachments(jsonElement.toString());

                JsonArray attachments = (JsonArray) jsonElement;
                if (attachments.size() == 0) {
                    message.setDisplayMode(MessageDataProcess.DisplayMode.MESSAGE.toString());
                } else if (attachments.size() == 1) {
                    JsonElement attachment = attachments.get(0);
                    String type = attachment.getAsJsonObject().get("category").getAsString();
                    if ("file".equals(type)) {
                        File file = mGson.fromJson(attachment.getAsJsonObject().get("data"), File.class);
                        if (BizLogic.isImg(file)) {
                            message.setDisplayMode(MessageDataProcess.DisplayMode.IMAGE.toString());
                        } else {
                            message.setDisplayMode(MessageDataProcess.DisplayMode.FILE.toString());
                        }
                    } else if ("rtf".equals(type)) {
                        message.setDisplayMode(MessageDataProcess.DisplayMode.RTF.toString());
                    } else if ("quote".equals(type)) {
                        message.setDisplayMode(MessageDataProcess.DisplayMode.INTEGRATION.toString());
                    } else if ("speech".equals(type)) {
                        message.setDisplayMode(MessageDataProcess.DisplayMode.SPEECH.toString());
                    } else if ("snippet".equals(type)) {
                        message.setDisplayMode(MessageDataProcess.DisplayMode.SNIPPET.toString());
                    } else if ("video".equals(type)) {
                        message.setDisplayMode(MessageDataProcess.DisplayMode.VIDEO.toString());
                    }
                }
            }
            // process for persistent
            MessageDataProcess.getInstance().processForPersistent(message);
        } catch (Exception e) {
            Logger.e("MessageDeserializer", "deserializer message fail", e);
            return null;
        }
        return message;
    }
}
