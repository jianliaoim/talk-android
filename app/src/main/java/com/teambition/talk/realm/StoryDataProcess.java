package com.teambition.talk.realm;

import com.google.gson.Gson;
import com.teambition.talk.GsonProvider;
import com.teambition.talk.imageloader.ImageLoaderConfig;
import com.teambition.talk.R;
import com.teambition.talk.entity.File;
import com.teambition.talk.entity.Link;
import com.teambition.talk.entity.Story;
import com.teambition.talk.util.StringUtil;

/**
 * Created by wlanjie on 15/10/23.
 */
public class StoryDataProcess {

    public enum Category {

        FILE("file"),
        LINK("link"),
        TOPIC("topic"),
        DEFAULT("default");

        public String value;

        Category(String value) {
            this.value = value;
        }

        public static Category getEnum(String value) {
            for (Category v : values()) {
                if (v.value.equals(value)) {
                    return v;
                }
            }
            return DEFAULT;
        }
    }

    public static String getIconUrl(Story story) {
        Gson gson = GsonProvider.getGson();
        switch (Category.getEnum(story.getCategory())) {
            case TOPIC:
                return ImageLoaderConfig.PREFIX_DRAWABLE + R.drawable.ic_notification_topic;
            case FILE:
                File file = gson.fromJson(story.getData(), File.class);
                return file.getThumbnailUrl();
            case LINK:
                Link link = gson.fromJson(story.getData(), Link.class);
                if (StringUtil.isNotBlank(link.getImageUrl())) {
                    return link.getImageUrl();
                } else {
                    return ImageLoaderConfig.PREFIX_DRAWABLE + R.drawable.ic_type_link;
                }
            default:
                return null;
        }
    }
}
