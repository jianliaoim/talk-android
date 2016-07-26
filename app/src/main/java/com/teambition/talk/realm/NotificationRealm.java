package com.teambition.talk.realm;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.teambition.talk.BizLogic;
import com.teambition.talk.GsonProvider;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Message;
import com.teambition.talk.entity.Notification;
import com.teambition.talk.entity.Room;
import com.teambition.talk.entity.Story;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by zeatual on 15/10/16.
 */
public class NotificationRealm extends AbstractRealm {

    private static NotificationRealm realm;

    final Gson gson;

    private NotificationRealm() {
        gson = GsonProvider.getGson();
    }

    public static NotificationRealm getInstance() {
        if (realm == null) {
            realm = new NotificationRealm();
        }
        return realm;
    }

    public List<Notification> getAllNotificationWithCurrentThread() {
        List<Notification> notifications = new ArrayList<>();
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            RealmResults<Notification> realmResults = realm
                    .where(Notification.class)
                    .equalTo(Notification.TEAM_ID, BizLogic.getTeamId())
                    .findAll();
            for (Notification realmResult : realmResults) {
                Notification notification = new Notification();
                copy(notification, realmResult);
                copyObject(notification, realmResult);
                notifications.add(notification);
            }
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
        return notifications;
    }

    public Notification getSingleNotificationWithCurrentThread(final String notificationId) {
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            Notification realmNotification = realm.where(Notification.class)
                    .equalTo(Notification.TEAM_ID, BizLogic.getTeamId())
                    .equalTo(Notification.ID, notificationId)
                    .findFirst();
            if (realmNotification == null) return null;
            Notification notification = new Notification();
            copy(notification, realmNotification);
            copyObject(notification, realmNotification);
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
        return null;
    }

    public Observable<List<Notification>> getNotPinnedNotification() {
        return Observable.create(new OnSubscribeRealm<List<Notification>>() {
            @Override
            public List<Notification> get(Realm realm) {
                RealmResults<Notification> realmResults = realm.where(Notification.class)
                        .equalTo(Notification.TEAM_ID, BizLogic.getTeamId())
                        .notEqualTo(Notification.IS_PINNED, true)
                        .findAllSorted(Notification.UPDATED_AT, Sort.DESCENDING);
                List<Notification> notifications = new ArrayList<>(realmResults.size());
                for (Notification realmResult : realmResults) {
                    Notification notification = new Notification();
                    copy(notification, realmResult);
                    copyObject(notification, realmResult);
                    notifications.add(notification);
                }
                return notifications;
            }
        });
    }

    public Observable<List<Notification>> getPinnedNotification() {
        return Observable.create(new OnSubscribeRealm<List<Notification>>() {
            @Override
            public List<Notification> get(Realm realm) {
                RealmResults<Notification> realmResults = realm.where(Notification.class)
                        .equalTo(Notification.TEAM_ID, BizLogic.getTeamId())
                        .equalTo(Notification.IS_PINNED, true)
                        .findAllSorted(Notification.UPDATED_AT, Sort.DESCENDING);
                List<Notification> notifications = new ArrayList<>(realmResults.size());
                for (Notification realmResult : realmResults) {
                    Notification notification = new Notification();
                    copy(notification, realmResult);
                    copyObject(notification, realmResult);
                    notifications.add(notification);
                }
                return notifications;
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<List<Notification>> getAllNotification() {
        return Observable.create(new OnSubscribeRealm<List<Notification>>() {
            @Override
            public List<Notification> get(Realm realm) {
                RealmResults<Notification> realmResults = realm.where(Notification.class)
                        .equalTo(Notification.TEAM_ID, BizLogic.getTeamId())
                        .findAll();
                List<Notification> notifications = new ArrayList<>(realmResults.size());
                for (Notification realmResult : realmResults) {
                    Notification notification = new Notification();
                    copy(notification, realmResult);
                    notifications.add(notification);
                }
                return notifications;
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<List<Notification>> getNotifications(final int count) {
        return Observable.create(new OnSubscribeRealm<List<Notification>>() {
            @Override
            public List<Notification> get(Realm realm) {
                RealmResults<Notification> realmResults = realm.where(Notification.class)
                        .equalTo(Notification.TEAM_ID, BizLogic.getTeamId())
                        .findAll();
                realmResults.sort(Notification.UPDATED_AT, Sort.DESCENDING);
                List<Notification> notifications = new ArrayList<>(realmResults.size());
                int currCount = 0;
                for (Notification realmResult : realmResults) {
                    currCount++;
                    Notification notification = new Notification();
                    copy(notification, realmResult);
                    notifications.add(notification);
                    if (currCount >= count) {
                        break;
                    }
                }
                return notifications;
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<List<Notification>> getMoreNotifications(final Date maxDate, final int count) {
        final long maxDateInMill;
        if (maxDate != null) {
            maxDateInMill = maxDate.getTime();
        } else {
            maxDateInMill = 0;
        }
        return Observable.create(new OnSubscribeRealm<List<Notification>>() {
            @Override
            public List<Notification> get(Realm realm) {
                RealmResults<Notification> realmResults = realm.where(Notification.class)
                        .equalTo(Notification.TEAM_ID, BizLogic.getTeamId())
                        .lessThan(Notification.UPDATED_AT, maxDateInMill)
                        .findAll();
                List<Notification> notifications = new ArrayList<>(realmResults.size());
                int currCount = 0;
                for (Notification realmResult : realmResults) {
                    currCount++;
                    Notification notification = new Notification();
                    copy(notification, realmResult);
                    notifications.add(notification);
                    if (currCount >= count) {
                        break;
                    }
                }
                return notifications;
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<Notification> getSingleNotification(final String notificationId) {
        return Observable.create(new OnSubscribeRealm<Notification>() {
            @Override
            public Notification get(Realm realm) {
                Notification realmNotification = realm.where(Notification.class)
                        .equalTo(Notification.TEAM_ID, BizLogic.getTeamId())
                        .equalTo(Notification.ID, notificationId)
                        .findFirst();
                if (realmNotification == null) return null;
                Notification notification = new Notification();
                copy(notification, realmNotification);
                copyObject(notification, realmNotification);
                return notification;
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<Notification> getSingleNotificationByTargetId(final String targetId) {
        return Observable.create(new OnSubscribeRealm<Notification>() {
            @Override
            public Notification get(Realm realm) {
                Notification realmNotification = realm.where(Notification.class)
                        .equalTo(Notification.TEAM_ID, BizLogic.getTeamId())
                        .equalTo(Notification.TARGET_ID, targetId)
                        .findFirst();
                Notification notification = new Notification();
                copy(notification, realmNotification);
                copyObject(notification, realmNotification);
                return notification;
            }
        }).subscribeOn(Schedulers.io());
    }

    public void addWithCurrentThread(final Notification notification) {
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            Notification realmNotification = realm.createObject(Notification.class);
            copy(realmNotification, notification);
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
    }

    public void batchAddWithCurrentThread(final List<Notification> notifications) {
        List<Notification> realmNotifications = new ArrayList<>(notifications.size());
        Realm realm = RealmProvider.getInstance();
        try {
            for (Notification realmNotification : notifications) {
                Notification notification = new Notification();
                copy(notification, realmNotification);
                realmNotifications.add(notification);
            }
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(realmNotifications);
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
    }

    public Observable<List<Notification>> batchAdd(final List<Notification> notifications) {
        return Observable.create(new OnSubscribeRealm<List<Notification>>() {
            @Override
            public List<Notification> get(Realm realm) {
                List<Notification> realmNotifications = new ArrayList<>(notifications.size());
                for (Notification realmNotification : notifications) {
                    if (realmNotification != null) {
                        Notification notification = new Notification();
                        copyWhenAdd(notification, realmNotification);
                        realmNotifications.add(notification);
                    }
                }
                realm.copyToRealmOrUpdate(realmNotifications);
                return notifications;
            }
        }).subscribeOn(Schedulers.io());
    }

    public void removeWithCurrentThread(final String notificationId) {
        Realm realm = RealmProvider.getInstance();
        try {
            realm.beginTransaction();
            Notification realmNotification = realm.where(Notification.class)
                    .equalTo(Notification.TEAM_ID, BizLogic.getTeamId())
                    .equalTo(Notification.ID, notificationId)
                    .findFirst();
            if (realmNotification == null) return;
            realmNotification.removeFromRealm();
            realm.commitTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            realm.cancelTransaction();
        } finally {
            realm.close();
        }
    }

    public Observable<Notification> remove(final String notificationId) {
        return Observable.create(new OnSubscribeRealm<Notification>() {
            @Override
            public Notification get(Realm realm) {
                Notification realmNotification = realm.where(Notification.class)
                        .equalTo(Notification.TEAM_ID, BizLogic.getTeamId())
                        .equalTo(Notification.ID, notificationId)
                        .findFirst();
                if (realmNotification == null) return null;
                Notification notification = new Notification();
                copy(notification, realmNotification);
                realmNotification.removeFromRealm();
                return notification;
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<Notification> removeByTargetId(final String targetId) {
        return Observable.create(new OnSubscribeRealm<Notification>() {
            @Override
            public Notification get(Realm realm) {
                Notification realmNotification = realm.where(Notification.class)
                        .equalTo(Notification.TEAM_ID, BizLogic.getTeamId())
                        .equalTo(Notification.TARGET_ID, targetId)
                        .findFirst();
                if (realmNotification == null) return null;
                Notification notification = new Notification();
                copy(notification, realmNotification);
                realmNotification.removeFromRealm();
                return notification;
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<Notification> addOrUpdate(final Notification notification) {
        return Observable.create(new OnSubscribeRealm<Notification>() {
            @Override
            public Notification get(Realm realm) {
                copyWhenAdd(notification, notification);
                realm.copyToRealmOrUpdate(notification);
                return notification;
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<Void> clearUnreadWithTargetId(final String id) {
        return Observable.create(new OnSubscribeRealm<Void>() {
            @Override
            public Void get(Realm realm) {
                Notification notificationRealm = realm.where(Notification.class)
                        .equalTo(Notification.TARGET_ID, id)
                        .equalTo(Notification.TEAM_ID, BizLogic.getTeamId())
                        .findFirst();
                if (notificationRealm != null) {
                    notificationRealm.setUnreadNum(0);
                }
                return null;
            }
        }).subscribeOn(Schedulers.io());
    }

    public Observable<Notification> updateNotificationWithLocalMessage(final Message message) {
        return Observable.create(new OnSubscribeRealm<Notification>() {
            @Override
            public Notification get(Realm realm) {
                Notification notificationRealm = realm.where(Notification.class)
                        .equalTo(Notification.TEAM_ID, BizLogic.getTeamId())
                        .equalTo(Notification.TARGET_ID, message.getForeignId())
                        .findFirst();
                if (notificationRealm == null) return null;
                notificationRealm.setCreator(MainApp.globalMembers.get(BizLogic.getUserInfo().get_id()));
                notificationRealm.set_creatorId(BizLogic.getUserInfo().get_id());
                notificationRealm.setCreatedAt(new Date(message.getCreateAtTime()));
                notificationRealm.setCreatedAtTime(message.getCreateAtTime());
                notificationRealm.setUpdatedAt(new Date(message.getCreateAtTime()));
                notificationRealm.setUpdateAtTime(message.getCreateAtTime());
                notificationRealm.setStatus(message.getStatus());
                if (MessageDataProcess.DisplayMode.getEnum(message.getDisplayMode())
                        == MessageDataProcess.DisplayMode.IMAGE) {
                    notificationRealm.setText(MainApp.CONTEXT.getString(R.string.picture));
                } else if (MessageDataProcess.DisplayMode.getEnum(message.getDisplayMode())
                        == MessageDataProcess.DisplayMode.FILE) {
                    notificationRealm.setText(MainApp.CONTEXT.getString(R.string.addons_file));
                } else {
                    notificationRealm.setText(message.getBody());
                }
                notificationRealm.setUnreadNum(0);

                Notification notification = new Notification();
                copy(notification, notificationRealm);
                return notification;
            }
        }).subscribeOn(Schedulers.io());
    }

    public void copyObject(Notification notification, Notification realmNotification) {
        if (notification == null || realmNotification == null) return;
        final String type = realmNotification.getType();
        if (TextUtils.isEmpty(type)) return;
        switch (NotificationDataProcess.Type.getEnum(type)) {
            case DMS:
                Member member = MemberRealm.getInstance().getMemberWithCurrentThread(realmNotification.get_targetId());
                notification.setMember(member);
                break;
            case ROOM:
                Room room = RoomRealm.getInstance().getRoomWithCurrentThread(realmNotification.get_targetId());
                notification.setRoom(room);
                break;
            case STORY:
                Story story = StoryRealm.getInstance().getSingleStoryWithCurrentThread(realmNotification.get_targetId());
                notification.setStory(story);
                break;
        }
        Member creatorMember = MemberRealm.getInstance().getMemberWithCurrentThread(realmNotification.get_creatorId());
        notification.setCreator(creatorMember);
    }

    public void copyWhenAdd(Notification a, Notification b) {
        if (b.get_id() != null) {
            a.set_id(b.get_id());
        }
        if (b.get_userId() != null) {
            a.set_userId(b.get_userId());
        }
        if (b.get_teamId() != null) {
            a.set_teamId(b.get_teamId());
        }
        if (b.get_targetId() != null) {
            a.set_targetId(b.get_targetId());
        }
        if (b.get_creatorId() != null) {
            a.set_creatorId(b.get_creatorId());
        }
        if (b.getType() != null) {
            a.setType(b.getType());
        }
        if (b.getCreator() != null) {
            a.setCreator(b.getCreator());
        }
        if (b.getEvent() != null) {
            a.setEvent(b.getEvent());
        }
        if (b.getText() != null) {
            a.setText(b.getText());
        }
        if (b.getUnreadNum() != null) {
            a.setUnreadNum(b.getUnreadNum());
        }
        if (b.getIsPinned() != null) {
            a.setIsPinned(b.getIsPinned());
        }
        if (b.getIsMute() != null) {
            a.setIsMute(b.getIsMute());
        }
        if (b.getIsHidden() != null) {
            a.setIsHidden(b.getIsHidden());
        }
        if (b.getUpdatedAt() != null) {
            a.setUpdateAtTime(b.getUpdatedAt().getTime());
        }
        if (b.getCreatedAt() != null) {
            a.setCreatedAtTime(b.getCreatedAt().getTime());
        }
        if (b.getCreatedAtTime() != 0) {
            a.setCreatedAt(new Date(b.getCreatedAtTime()));
        }
        if (b.getUpdateAtTime() != 0) {
            a.setUpdatedAt(new Date(b.getUpdateAtTime()));
        }
        a.setStatus(b.getStatus());

        a.setRoom(b.getRoom());
        a.setStory(b.getStory());
        a.setMember(b.getMember());
        if (a.getMember() != null) {
            // Notification中的Member数据不全，teamMemberId前加“_”防止覆盖Member表里的原有数据
            a.getMember().set_teamMemberId("_" + a.get_teamId() + a.getMember().get_id());
        }

        if (b.getCreator() != null && MainApp.globalMembers.get(b.getCreator().get_id()) != null) {
            a.setAuthorName(b.getCreator().getAlias());
        } else if (b.getAuthorName() != null) {
            a.setAuthorName(b.getAuthorName());
        }
    }

    public void copy(Notification a, Notification b) {
        if (b.get_id() != null) {
            a.set_id(b.get_id());
        }
        if (b.get_userId() != null) {
            a.set_userId(b.get_userId());
        }
        if (b.get_teamId() != null) {
            a.set_teamId(b.get_teamId());
        }
        if (b.get_targetId() != null) {
            a.set_targetId(b.get_targetId());
        }
        if (b.get_creatorId() != null) {
            a.set_creatorId(b.get_creatorId());
        }
        if (b.getType() != null) {
            a.setType(b.getType());
        }
        if (b.getCreator() != null) {
            a.setCreator(b.getCreator());
        }
        if (b.getEvent() != null) {
            a.setEvent(b.getEvent());
        }
        if (b.getText() != null) {
            a.setText(b.getText());
        }
        if (b.getUnreadNum() != null) {
            a.setUnreadNum(b.getUnreadNum());
        }
        if (b.getIsPinned() != null) {
            a.setIsPinned(b.getIsPinned());
        }
        if (b.getIsMute() != null) {
            a.setIsMute(b.getIsMute());
        }
        if (b.getIsHidden() != null) {
            a.setIsHidden(b.getIsHidden());
        }
        if (b.getUpdatedAt() != null) {
            a.setUpdateAtTime(b.getUpdatedAt().getTime());
        }
        if (b.getCreatedAt() != null) {
            a.setCreatedAtTime(b.getCreatedAt().getTime());
        }
        if (b.getCreatedAtTime() != 0) {
            a.setCreatedAt(new Date(b.getCreatedAtTime()));
        }
        if (b.getUpdateAtTime() != 0) {
            a.setUpdatedAt(new Date(b.getUpdateAtTime()));
        }
        a.setStatus(b.getStatus());

        if (b.getRoom() != null) {
            Room room = new Room();
            RoomRealm.getInstance().copy(room, b.getRoom());
            a.setRoom(room);
        } else if (b.getStory() != null) {
            Story story = new Story();
            StoryRealm.getInstance().copy(story, b.getStory());
            a.setStory(story);
        } else if (b.getMember() != null) {
            Member member = new Member();
            MemberRealm.getInstance().copy(member, b.getMember());
            a.setMember(member);
        }

        if (b.getCreator() != null) {
            Member creator = new Member();
            MemberRealm.getInstance().copy(creator, b.getCreator());
            a.setCreator(creator);
        }

        if (b.getAuthorName() != null) {
            a.setAuthorName(b.getAuthorName());
        } else if (b.getCreator() != null) {
            a.setAuthorName(b.getCreator().getAlias());
        }
    }
}
