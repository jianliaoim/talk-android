package com.teambition.talk;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.teambition.talk.client.adapter.ContentDeserializer;
import com.teambition.talk.client.adapter.ContentStringDeserializer;
import com.teambition.talk.client.adapter.HighlightDeserializer;
import com.teambition.talk.client.adapter.ISODateAdapter;
import com.teambition.talk.client.adapter.LocalContentDeserializer;
import com.teambition.talk.client.adapter.MessageDeserializer;
import com.teambition.talk.client.adapter.NotificationDeserializer;
import com.teambition.talk.client.adapter.RoomDeserializer;
import com.teambition.talk.client.adapter.StoryDeserializer;
import com.teambition.talk.client.adapter.TeamActivityDeserializer;
import com.teambition.talk.entity.Highlight;
import com.teambition.talk.entity.Message;
import com.teambition.talk.entity.Notification;
import com.teambition.talk.entity.Room;
import com.teambition.talk.entity.Story;
import com.teambition.talk.entity.TeamActivity;

import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.List;

import io.realm.RealmObject;

/**
 * Created by zeatual on 14/11/5.
 */
public class GsonProvider {

    public static class Builder {
        GsonBuilder builder = new GsonBuilder()
                .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
                .setExclusionStrategies(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        return f.getDeclaringClass().equals(RealmObject.class);
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                });

        public Builder setDateFormat(String pattern) {
            builder.setDateFormat(pattern);
            return this;
        }

        public Builder setMessageAdapter() {
            builder.registerTypeAdapter(Message.class, new MessageDeserializer());
            return this;
        }

        public Builder setDateAdapter() {
            builder.registerTypeAdapter(Date.class, new ISODateAdapter());
            return this;
        }

        public Builder setRoomAdapter() {
            builder.registerTypeAdapter(Room.class, new RoomDeserializer());
            return this;
        }

        public Builder setStoryAdapter() {
            builder.registerTypeAdapter(Story.class, new StoryDeserializer());
            return this;
        }

        public Builder setHighlightAdapter() {
            builder.registerTypeAdapter(Highlight.class, new HighlightDeserializer());
            return this;
        }

        public Builder setNotificationAdapter() {
            builder.registerTypeAdapter(Notification.class, new NotificationDeserializer());
            return this;
        }

        public Builder setLocalContentAdapter() {
            builder.registerTypeAdapter(List.class, new LocalContentDeserializer());
            return this;
        }

        public Builder setContentStringAdapter() {
            builder.registerTypeAdapter(String.class, new ContentStringDeserializer());
            return this;
        }

        public Builder setTeamActivitiesAdapter() {
            builder.registerTypeAdapter(TeamActivity.class, new TeamActivityDeserializer());
            return this;
        }

        public Builder addDeserializationExclusionStrategy(final String key) {
            builder.addDeserializationExclusionStrategy(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes f) {
                    return f.getName().equals(key);
                }

                @Override
                public boolean shouldSkipClass(Class<?> clazz) {
                    return false;
                }
            });
            return this;
        }

        public Gson create() {
            return builder.create();
        }
    }

    private static Gson gson;

    public static Gson getGson() {
        if (gson == null) {
            synchronized (GsonProvider.class) {
                if (gson == null) {
                    gson = new GsonProvider.Builder().create();
                }
            }
        }
        return gson;
    }
}