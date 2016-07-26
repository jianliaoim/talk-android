package com.teambition.talk.realm;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.teambition.talk.BizLogic;
import com.teambition.talk.GsonProvider;
import com.teambition.talk.entity.Story;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by wlanjie on 15/10/23.
 */
public class StoryRealm extends AbstractRealm {

    static StoryRealm realm;

    final Gson gson;

    private StoryRealm() {
        gson = GsonProvider.getGson();
    }

    public static StoryRealm getInstance() {
        if (realm == null) {
            realm = new StoryRealm();
        }
        return realm;
    }

    public List<Story> getAllStoryWithCurrentThread() {
        List<Story> stories = new ArrayList<>();
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            RealmResults<Story> realmResults = realm.where(Story.class)
                    .equalTo(Story.TEAM_ID, BizLogic.getTeamId())
                    .findAll();
            for (Story realmResult : realmResults) {
                Story story = new Story();
                copy(story, realmResult);
                stories.add(story);
            }
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
        return stories;
    }

    public Observable<Story> getStory(final String storyId) {
        return Observable.create(new OnSubscribeRealm<Story>() {
            @Override
            public Story get(Realm realm) {
                Story realmStory = realm.where(Story.class)
                        .equalTo(Story.TEAM_ID, BizLogic.getTeamId())
                        .equalTo(Story.ID, storyId)
                        .findFirst();
                if (realmStory == null) return null;
                Story story = new Story();
                copy(story, realmStory);
                return story;
            }
        }).subscribeOn(Schedulers.io());
    }

    public Story getSingleStoryWithCurrentThread(final String storyId) {
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            Story realmStory = realm.where(Story.class)
                    .equalTo(Story.TEAM_ID, BizLogic.getTeamId())
                    .equalTo(Story.ID, storyId)
                    .findFirst();
            if (realmStory == null) return null;
            Story story = new Story();
            copy(story, realmStory);
            realm.commitTransaction();
            return story;
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
        return null;
    }

    public Observable<List<Story>> getAllStory() {
        return Observable.create(new OnSubscribeRealm<List<Story>>() {
            @Override
            public List<Story> get(Realm realm) {
                RealmResults<Story> realmResults = realm.where(Story.class)
                        .equalTo(Story.TEAM_ID, BizLogic.getTeamId())
                        .findAll();
                final List<Story> stories = new ArrayList<>(realmResults.size());
                for (Story realmResult : realmResults) {
                    Story story = new Story();
                    copy(story, realmResult);
                    stories.add(story);
                }
                return stories;
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<Story> getSingleStory(final String storyId) {
        return Observable.create(new OnSubscribeRealm<Story>() {
            @Override
            public Story get(Realm realm) {
                Story realmStory = realm.where(Story.class)
                        .equalTo(Story.TEAM_ID, BizLogic.getTeamId())
                        .equalTo(Story.ID, storyId)
                        .findFirst();
                if (realmStory == null) return null;
                Story story = new Story();
                copy(story, realmStory);
                return story;
            }
        }).subscribeOn(Schedulers.io());
    }

    public void batchAddWithCurrentThread(final List<Story> stories) {
        Realm realm = RealmProvider.getInstance();
        try {
            final List<Story> realmStories = new ArrayList<>(stories.size());
            for (Story story : stories) {
                Story realmStory = new Story();
                copy(realmStory, story);
                realmStories.add(realmStory);
            }
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(realmStories);
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
    }

    public Observable<List<Story>> batchAdd(final List<Story> stories) {
        return Observable.create(new OnSubscribeRealm<List<Story>>() {
            @Override
            public List<Story> get(Realm realm) {
                final List<Story> realmStories = new ArrayList<>(stories.size());
                for (Story story : stories) {
                    Story realmStory = new Story();
                    copy(realmStory, story);
                    realmStories.add(realmStory);
                }
                realm.copyToRealmOrUpdate(realmStories);
                return stories;
            }
        }).subscribeOn(Schedulers.io());
    }

    public void updateAndAddWithCurrentThread(final Story story) {
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            copy(story, story);
            realm.copyToRealmOrUpdate(story);
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
    }

    public Observable<Story> addOrUpdate(final Story story) {
        return Observable.create(new OnSubscribeRealm<Story>() {
            @Override
            public Story get(Realm realm) {
                copy(story, story);
                realm.copyToRealmOrUpdate(story);
                return story;
            }
        }).subscribeOn(Schedulers.io());
    }

    public void removeWithCurrentThread(final String storyId) {
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            Story realmStory = realm.where(Story.class)
                    .equalTo(Story.TEAM_ID, BizLogic.getTeamId())
                    .equalTo(Story.ID, storyId)
                    .findFirst();
            if (realmStory == null) {
                realm.cancelTransaction();
                return;
            }
            realmStory.removeFromRealm();
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
    }

    public Observable<Story> remove(final String storyId) {
        return Observable.create(new OnSubscribeRealm<Story>() {
            @Override
            public Story get(Realm realm) {
                Story realmStory = realm.where(Story.class)
                        .equalTo(Story.TEAM_ID, BizLogic.getTeamId())
                        .equalTo(Story.ID, storyId)
                        .findFirst();
                if (realmStory == null) return null;
                realmStory.removeFromRealm();
                return realmStory;
            }
        }).subscribeOn(Schedulers.io());
    }

    public void copy(Story realmStory, Story story) {
        if (story == null || realmStory == null) return;
        if (story.get_id() != null) {
            realmStory.set_id(story.get_id());
        }
        if (story.get_teamId() != null) {
            realmStory.set_teamId(story.get_teamId());
        }
        if (story.get_creatorId() != null) {
            realmStory.set_creatorId(story.get_creatorId());
        }
        if (story.getCategory() != null) {
            realmStory.setCategory(story.getCategory());
        }
        if (story.getTitle() != null) {
            realmStory.setTitle(story.getTitle());
        }
        if (story.getData() != null) {
            realmStory.setData(story.getData());
        }
        if (story.getCreatedAt() != null) {
            realmStory.setCreatedAtTime(story.getCreatedAt().getTime());
        }
        if (story.getCreatedAtTime() != 0) {
            realmStory.setCreatedAt(new Date(story.getCreatedAtTime()));
        }
        if (story.getActivedAt() != null) {
            realmStory.setActivedAtTime(story.getActivedAt().getTime());
        }
        if (story.getActivedAtTime() != 0) {
            realmStory.setActivedAt(new Date(story.getActivedAtTime()));
        }
        if (story.get_memberIds() != null) {
            realmStory.setMemberIds(gson.toJson(story.get_memberIds()));
        }
        if (story.getMemberIds() != null) {
            final List<String> memberIds = gson.fromJson(story.getMemberIds(), new TypeToken<List<String>>(){}.getType());
            realmStory.set_memberIds(memberIds);
        }
    }
}
