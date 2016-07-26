package com.teambition.talk.client.adapter;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.teambition.talk.GsonProvider;
import com.teambition.talk.entity.Story;

import java.lang.reflect.Type;

/**
 * Created by zeatual on 15/9/30.
 */
public class StoryDeserializer implements JsonDeserializer<Story> {
    @Override
    public Story deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Story story;
        JsonObject jsonObj = json.getAsJsonObject();
        Gson gson = new GsonProvider.Builder().addDeserializationExclusionStrategy("data")
                .setDateAdapter()
                .create();
        try {
            story = gson.fromJson(json, Story.class);
            String data = jsonObj.get("data").toString();
            story.setData(data);
        } catch (Exception e) {
            return null;
        }
        return story;
    }
}
