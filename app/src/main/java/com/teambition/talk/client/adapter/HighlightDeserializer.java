package com.teambition.talk.client.adapter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.teambition.talk.entity.Highlight;

import java.lang.reflect.Type;

/**
 * Created by zeatual on 14/11/5.
 */
public class HighlightDeserializer implements JsonDeserializer<Highlight> {

    @Override
    public Highlight deserialize(
            JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        Highlight highlight = new Highlight();

        if (!json.isJsonObject()) {
            return highlight;
        }
        JsonObject jObj = json.getAsJsonObject();

        if (jObj.get("body") != null){
            highlight.setBody(jObj.get("body").getAsJsonArray().get(0).getAsString());
        }
        if (jObj.get("attachments.data.text") != null){
            highlight.setText(jObj.get("attachments.data.text").getAsJsonArray().get(0).getAsString());
        }
        if (jObj.get("attachments.data.title") != null){
            highlight.setTitle(jObj.get("attachments.data.title").getAsJsonArray().get(0).getAsString());
        }
        if (jObj.get("attachments.data.fileName") != null){
            highlight.setFileName(jObj.get("attachments.data.fileName").getAsJsonArray().get(0).getAsString());
        }

        return highlight;
    }

}
