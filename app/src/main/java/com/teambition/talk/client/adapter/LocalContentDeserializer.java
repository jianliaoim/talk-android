package com.teambition.talk.client.adapter;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.teambition.talk.GsonProvider;
import com.teambition.talk.entity.Content;
import com.teambition.talk.entity.ContentData;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zeatual on 14/11/5.
 */
public class LocalContentDeserializer implements JsonDeserializer<List<Content>> {

    @Override
    public List<Content> deserialize(
            JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        List<Content> contents = new ArrayList<>();

        if (!json.isJsonArray()) {
            return null;
        }

        GsonProvider.Builder builder = new GsonProvider.Builder();
        Gson gson = builder.create();

        for (JsonElement j : json.getAsJsonArray()) {
            JsonObject jObj = j.getAsJsonObject();
            String type = jObj.get("type").getAsString();
            if (type.equals(Content.CONTENT_TEXT)) {
                contents.add(new Content(Content.CONTENT_TEXT, jObj.get("value").getAsString()));
            } else {
                ContentData data = gson.fromJson(jObj.get("value"), ContentData.class);
                contents.add(new Content(Content.CONTENT_DATA, data));
            }
        }

        return contents;
    }

}
