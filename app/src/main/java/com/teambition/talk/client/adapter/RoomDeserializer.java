package com.teambition.talk.client.adapter;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.teambition.talk.GsonProvider;
import com.teambition.talk.entity.Room;

import org.json.JSONObject;

import java.lang.reflect.Type;

/**
 * Created by zeatual on 14/11/5.
 */
public class RoomDeserializer implements JsonDeserializer<Room> {

    @Override
    public Room deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        Room room;
        GsonProvider.Builder builder = new GsonProvider.Builder();
        Gson gson = builder.addDeserializationExclusionStrategy("prefs")
                .setDateAdapter()
                .create();
        try {
            JSONObject jsonObj = new JSONObject(json.toString());
            room = gson.fromJson(json, Room.class);
            boolean isMute = false;
            if (jsonObj.optJSONObject("prefs") != null) {
                isMute = jsonObj.optJSONObject("prefs").optBoolean("isMute", false);
            }
            room.setIsMute(isMute);
        } catch (Exception e) {
            return null;
        }
        return room;
    }
}
