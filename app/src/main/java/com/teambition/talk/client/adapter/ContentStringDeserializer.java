package com.teambition.talk.client.adapter;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.teambition.talk.GsonProvider;
import com.teambition.talk.entity.ContentData;

import java.lang.reflect.Type;

/**
* Created by zeatual on 14/11/7.
*/
public class ContentStringDeserializer implements JsonDeserializer<String> {

    @Override
    public String deserialize(
            JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {

        String str = "";

        if (!json.isJsonArray()) {
            return null;
        }

        Gson gson = new GsonProvider.Builder().create();

        for (int i = 0; i < json.getAsJsonArray().size(); i++) {
            JsonElement j = json.getAsJsonArray().get(i);
            if (j.isJsonPrimitive()) {
                str += j.getAsString();
            } else if (j.isJsonObject()) {
                ContentData data = gson.fromJson(j, ContentData.class);
                str += data.text;
            }
        }
        return str;
    }
}
