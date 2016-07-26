package com.teambition.talk.client.adapter;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
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
public class ContentDeserializer implements JsonDeserializer<List<Content>> {

    @Override
    public List<Content> deserialize(
            JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        List<Content> contents = new ArrayList<Content>();

        if (!json.isJsonArray()) {
            return null;
        }

        GsonProvider.Builder builder = new GsonProvider.Builder();
        Gson gson = builder.create();

        for (JsonElement j : json.getAsJsonArray()) {
            if (j.isJsonPrimitive()) {
                contents.add(new Content(Content.CONTENT_TEXT, j.getAsString()));
            } else if (j.isJsonObject()) {
                ContentData data = gson.fromJson(j, ContentData.class);
                contents.add(new Content(Content.CONTENT_DATA, data));
            }
        }

        return contents;
    }

}
