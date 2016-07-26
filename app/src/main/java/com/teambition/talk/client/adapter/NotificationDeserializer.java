package com.teambition.talk.client.adapter;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.teambition.talk.GsonProvider;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Notification;
import com.teambition.talk.entity.Room;
import com.teambition.talk.entity.Story;
import com.teambition.talk.realm.NotificationDataProcess;

import java.lang.reflect.Type;

/**
 * Created by wlanjie on 15/10/23.
 */
public class NotificationDeserializer implements JsonDeserializer<Notification> {

    final Gson gson;

    public NotificationDeserializer() {
        GsonProvider.Builder builder = new GsonProvider.Builder();
        gson = builder
                .setDateAdapter()
                .setStoryAdapter()
                .setRoomAdapter()
                .create();
    }

    @Override
    public Notification deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Notification notification = null;
        JsonObject jsonObject = json.getAsJsonObject();
        try {
            String type = jsonObject.get("type").getAsString();
            JsonElement jsonElement = jsonObject.get("target");
            if (jsonElement.toString().equals("null")) return null;
            JsonObject targetObject = jsonElement.getAsJsonObject();
            notification = gson.fromJson(json, Notification.class);
            notification.setAuthorName(notification.getCreator().getAlias());
            switch (NotificationDataProcess.Type.getEnum(type)) {
                case DMS:
                    Member member = gson.fromJson(targetObject, Member.class);
                    notification.setMember(member);
                    break;
                case ROOM:
                    Room room = gson.fromJson(targetObject, Room.class);
                    if (room.getIsGeneral()) {
                        room.setTopic(MainApp.CONTEXT.getString(R.string.general));
                    }
                    notification.setRoom(room);
                    break;
                case STORY:
                    Story story = gson.fromJson(targetObject, Story.class);
                    notification.setStory(story);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return notification;
    }
}
