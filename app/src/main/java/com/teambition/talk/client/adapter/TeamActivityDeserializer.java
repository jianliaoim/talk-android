package com.teambition.talk.client.adapter;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.teambition.talk.GsonProvider;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Room;
import com.teambition.talk.entity.Story;
import com.teambition.talk.entity.TeamActivity;

import java.lang.reflect.Type;

/**
 * Created by nlmartian on 2/17/16.
 */
public class TeamActivityDeserializer implements JsonDeserializer<TeamActivity> {

    final Gson gson;

    public TeamActivityDeserializer() {
        GsonProvider.Builder builder = new GsonProvider.Builder();
        gson = builder.setDateAdapter()
                .addDeserializationExclusionStrategy("creator")
                .setStoryAdapter()
                .setRoomAdapter()
                .create();
    }


    @Override
    public TeamActivity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        TeamActivity activity = gson.fromJson(json, TeamActivity.class);
        JsonObject jsonObject = json.getAsJsonObject();
        if (jsonObject.get("type") != null) {
            try {
                String type = jsonObject.get("type").getAsString();
                JsonElement jsonElement = jsonObject.get("target");
                if (jsonElement != null && jsonElement.isJsonObject()) {
                    JsonObject targetObject = jsonElement.getAsJsonObject();
                    activity = gson.fromJson(json, TeamActivity.class);
                    if ("room".equals(type)) {
                        activity.setRoom(gson.fromJson(targetObject, Room.class));
                    } else if ("story".equals(type)) {
                        activity.setStory(gson.fromJson(targetObject, Story.class));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (jsonObject.get("creator").isJsonObject()) {
            Member member = gson.fromJson(jsonObject.get("creator"), Member.class);
            activity.setCreator(member);
        }
        return activity;
    }
}
