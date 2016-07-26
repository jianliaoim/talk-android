package com.teambition.talk;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.format.DateUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.entity.File;
import com.teambition.talk.entity.Group;
import com.teambition.talk.entity.Invitation;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Room;
import com.teambition.talk.entity.Team;
import com.teambition.talk.entity.User;
import com.teambition.talk.event.NetworkEvent;
import com.teambition.talk.event.SyncFinishEvent;
import com.teambition.talk.event.SyncLeaveMemberFinisEvent;
import com.teambition.talk.event.UpdateMemberEvent;
import com.teambition.talk.event.UpdateRoomEvent;
import com.teambition.talk.realm.GroupRealm;
import com.teambition.talk.realm.InvitationRealm;
import com.teambition.talk.realm.MemberRealm;
import com.teambition.talk.realm.RoomRealm;
import com.teambition.talk.realm.TeamRealm;
import com.teambition.talk.rx.ApiErrorAction;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.util.DateUtil;
import com.teambition.talk.util.Logger;
import com.teambition.talk.util.StringUtil;

import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by zeatual on 14-8-4.
 */
public class BizLogic {

    private static final String XIAOAI_EMAIL = "talkai@talk.ai";
    private static final String XIAOAI_JIANLIAO = "talkai@jianliao.com";
    private static final String XIAOAI_SERVICE_NAME = "talkai";
    private static final String HOST = "https://talk.ai";

    public static final String TEAM_SYNC_PREFIX = "team_sync_prefix_";

    public static Subscription SYNC_SUBSCRIPTION;

    public static Subscription LEAVE_MEMBER_SUBSCRIPTION;

    public static boolean isLogin() {
        String accessToken = MainApp.PREF_UTIL.getString(Constant.ACCESS_TOKEN);
        return StringUtil.isNotBlank(accessToken);
    }

    public static boolean isAdmin() {
        if (getUserInfo() == null) {
            return false;
        }
        if (MainApp.globalMembers.get(getUserInfo().get_id()) == null) {
            return false;
        } else {
            String role = MainApp.globalMembers.get(getUserInfo().get_id()).getRole();
            return Member.ADMIN.equals(role) || Member.OWNER.equals(role);
        }
    }

    public static boolean isAdmin(Member member) {
        return member != null && (Member.ADMIN.equals(member.getRole()) ||
                Member.OWNER.equals(member.getRole()));
    }

    public static boolean isAdminOfRoom(String roomId) {
        return MainApp.globalRooms.containsKey(roomId) &&
                BizLogic.isMe(MainApp.globalRooms.get(roomId).get_creatorId());
    }

    public static boolean isXiaoai(Member member) {
        return member != null && member.getIsRobot() != null && member.getIsRobot()
                && (XIAOAI_SERVICE_NAME.equals(member.getService())
                || XIAOAI_EMAIL.equals(member.getEmail()) || XIAOAI_JIANLIAO.equals(member.getEmail()));
    }

    public static boolean hasPostToken() {
        return MainApp.PREF_UTIL.getBoolean(Constant.HAS_POST_TOKEN, false);
    }

    public static boolean hasChosenTeam() {
        return MainApp.PREF_UTIL.getString(Constant.TEAM, null) != null;
    }

    public static int getVersionCode(Context context) {
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(),
                    0);
            return pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static User getUserInfo() {
        return (User) MainApp.PREF_UTIL.getObject(Constant.USER, User.class);
    }

    public static Team getTeam() {
        Team team = (Team) MainApp.PREF_UTIL.getObject(Constant.TEAM, Team.class);
        return team == null ? null : team;
    }

    public static String getTeamId() {
        Team team = (Team) MainApp.PREF_UTIL.getObject(Constant.TEAM, Team.class);
        return team == null ? null : team.get_id();
    }

    public static String getTeamColor() {
        Team team = (Team) MainApp.PREF_UTIL.getObject(Constant.TEAM, Team.class);
        return team == null ? Constant.DEFAULT_COLOR : team.getColor();
    }

    public static String getSignCode() {
        Team team = (Team) MainApp.PREF_UTIL.getObject(Constant.TEAM, Team.class);
        return team == null ? null : team.getSignCode();
    }

    public static String getTeamInviteUrl() {
        Team team = (Team) MainApp.PREF_UTIL.getObject(Constant.TEAM, Team.class);
        return team == null ? HOST : team.getInviteUrl();
    }

    public static String getTeamName() {
        Team team = (Team) MainApp.PREF_UTIL.getObject(Constant.TEAM, Team.class);
        return team.getName();
    }

    public static boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) MainApp.CONTEXT
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null;
    }

    public static boolean isApplicationShowing(Context context) {
        return ((MainApp) context.getApplicationContext()).getActivityCount() != 0;
    }

    public static void initGlobalData() {
        Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                List<Member> members = MemberRealm.getInstance().getMemberWithCurrentThread();
                List<Room> rooms = RoomRealm.getInstance().getRoomWithCurrentThread();
                if (!members.isEmpty() && !rooms.isEmpty()) {
                    for (Member member : members) {
                        MainApp.globalMembers.put(member.get_id(), member);
                    }
                    for (Room room : rooms) {
                        List<Member> roomMembers = MemberRealm.getInstance().getMembersByIdsWithCurrentThread(room.get_memberIds());
                        room.setMembers(roomMembers);
                        MainApp.globalRooms.put(room.get_id(), room);
                    }
                    MainApp.DATA_IS_READY = true;
                    subscriber.onNext(null);
                }
            }
        }).subscribeOn(Schedulers.computation()).subscribe();
    }

    public static void initGlobalMembers(final Action1<Object> action) {
        Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                List<Member> members = MemberRealm.getInstance().getMemberWithCurrentThread();
                for (Member member : members) {
                    MainApp.globalMembers.put(member.get_id(), member);
                }
                subscriber.onNext(null);
            }
        }).subscribeOn(Schedulers.computation()).subscribe(new Action1<Object>() {
            @Override
            public void call(Object o) {
                if (action != null) action.call(o);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {

            }
        });
    }

    /**
     * 刷新latestMessages、members、rooms
     *
     * @param field
     */
    public static void refreshTeamInfo(String field) {
        if (isNetworkConnected()) {
            MainApp.IS_SYNCING = true;
            cancelSync();
            SYNC_SUBSCRIPTION = TalkClient.getInstance().getTalkApi()
                    .getTeamDetail(BizLogic.getTeamId(), field)
                    .subscribe(new SyncCompleteAction(), new SyncFailAction());
        } else {
            MainApp.IS_SYNCING = false;
            BusProvider.getInstance().post(new SyncFinishEvent(false));
        }
    }

    public static void syncTeamData() {
        String syncDateStr = MainApp.PREF_UTIL.getString(BizLogic.TEAM_SYNC_PREFIX + BizLogic.getTeamId());
        long syncTimeInMillis = 0;
        if (StringUtil.isNotBlank(syncDateStr)) {
            syncTimeInMillis = DateUtil.parseISO8601(syncDateStr, DateUtil.DATE_FORMAT_JSON).getTime();
        }
        if (System.currentTimeMillis() - syncTimeInMillis > Constant.FULL_SYNC_INTERVAL) {
            BizLogic.syncMembers();
            BizLogic.syncRooms();
            BizLogic.syncLeaveMemberData();
        }
    }

    public static void saveTeamSyncTime(String teamId) {
        String syncDateStr = DateUtil.formatISO8601(new Date(), DateUtil.DATE_FORMAT_JSON);
        MainApp.PREF_UTIL.putString(BizLogic.TEAM_SYNC_PREFIX + teamId, syncDateStr);
    }

    public static void syncLeaveMemberData() {
        if (isNetworkConnected()) {
            if (MainApp.IS_LEAVE_MEMBER_SYNCING) return;
            MainApp.IS_LEAVE_MEMBER_SYNCING = true;
            if (LEAVE_MEMBER_SUBSCRIPTION != null && !LEAVE_MEMBER_SUBSCRIPTION.isUnsubscribed()) {
                LEAVE_MEMBER_SUBSCRIPTION.unsubscribe();
            }
            LEAVE_MEMBER_SUBSCRIPTION = TalkClient.getInstance().getTalkApi()
                    .leaveMember(getTeamId())
                    .subscribe(new Action1<List<Member>>() {
                        @Override
                        public void call(final List<Member> members) {
                            Observable.create(new Observable.OnSubscribe<List<Member>>() {
                                @Override
                                public void call(Subscriber<? super List<Member>> subscriber) {
                                    if (members != null) {
                                        for (Member member : members) {
                                            member.setIsQuit(true);
                                            MemberRealm.getInstance().addOrUpdateWithCurrentThread(member);
                                        }
                                    }
                                    subscriber.onNext(members);
                                }
                            }).observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Action1<List<Member>>() {
                                        @Override
                                        public void call(List<Member> members) {
                                            MainApp.IS_LEAVE_MEMBER_SYNCING = false;
                                            BusProvider.getInstance().post(new SyncLeaveMemberFinisEvent(members));
                                            BizLogic.saveTeamSyncTime(BizLogic.getTeamId());
                                        }
                                    }, new Action1<Throwable>() {
                                        @Override
                                        public void call(Throwable throwable) {
                                            Logger.e("SYNC", "failed", throwable);
                                        }
                                    });
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            MainApp.IS_LEAVE_MEMBER_SYNCING = false;
                            BusProvider.getInstance().post(new SyncLeaveMemberFinisEvent(false));
                        }
                    });
        } else {
            MainApp.IS_LEAVE_MEMBER_SYNCING = false;
            BusProvider.getInstance().post(new SyncLeaveMemberFinisEvent(false));
        }
    }

    public static void syncMembers() {
        TalkClient.getInstance().getTalkApi()
                .getMembers(getTeamId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Member>>() {
                    @Override
                    public void call(final List<Member> members) {
                        if (members == null || members.isEmpty()) return;
                        Observable.create(new Observable.OnSubscribe<List<Member>>() {
                            @Override
                            public void call(Subscriber<? super List<Member>> subscriber) {
                                MemberRealm.getInstance().batchAddWithCurrentThread(members);
                                subscriber.onNext(members);
                            }
                        }).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<List<Member>>() {
                                    @Override
                                    public void call(List<Member> members) {
                                        BusProvider.getInstance().post(new UpdateMemberEvent(members));
                                        for (Member member : members) {
                                            Member m = new Member();
                                            MemberRealm.getInstance().copy(m, member);
                                            MainApp.globalMembers.put(member.get_id(), m);
                                        }
                                        BizLogic.saveTeamSyncTime(BizLogic.getTeamId());
                                    }
                                }, new RealmErrorAction());
                    }
                }, new ApiErrorAction());
    }

    public static void syncRooms() {
        TalkClient.getInstance().getTalkApi()
                .getRooms(getTeamId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Room>>() {
                    @Override
                    public void call(List<Room> rooms) {
                        if (rooms == null || rooms.isEmpty()) return;
                        RoomRealm.getInstance().batchAdd(rooms)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<List<Room>>() {
                                    @Override
                                    public void call(List<Room> rooms) {
                                        for (Room room : rooms) {
                                            Room r = new Room();
                                            RoomRealm.getInstance().copy(r, room);
                                            MainApp.globalRooms.put(room.get_id(), r);
                                        }
                                        BizLogic.saveTeamSyncTime(BizLogic.getTeamId());
                                    }
                                }, new RealmErrorAction());
                    }
                }, new ApiErrorAction());
    }

    public static void syncGroups() {
        TalkClient.getInstance().getTalkApi()
                .getGroups(BizLogic.getTeamId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Group>>() {
                    @Override
                    public void call(List<Group> groups) {
                        GroupRealm.getInstance().batchAdd(groups)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<List<Group>>() {
                                               @Override
                                               public void call(List<Group> groups) {
                                                   System.out.println();
                                               }
                                           },
                                        new RealmErrorAction());
                    }
                }, new ApiErrorAction());
    }

    public static void syncData() {
        if (isNetworkConnected()) {
            MainApp.IS_SYNCING = true;
            cancelSync();

            // fetch data from server
            SYNC_SUBSCRIPTION = TalkClient.getInstance().getTalkApi()
                    .joinTeam(BizLogic.getTeamId())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SyncCompleteAction(), new SyncFailAction());
        } else {
            MainApp.IS_SYNCING = false;
            Observable.just(null).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<Object>() {
                @Override
                public void call(Object o) {
                    BusProvider.getInstance().post(new SyncFinishEvent(false));
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(Throwable throwable) {

                }
            });

        }
    }

    public static void cancelSync() {
        if (SYNC_SUBSCRIPTION != null && !SYNC_SUBSCRIPTION.isUnsubscribed()) {
            SYNC_SUBSCRIPTION.unsubscribe();
        }
    }

    public static boolean isMe(String id) {
        return getUserInfo() != null && getUserInfo().get_id().equals(id);
    }

    public static boolean isCurrentTeam(String id) {
        return id != null && id.equals(getTeamId());
    }

    public static boolean isImg(File file) {
        return file != null && ("png".equals(file.getFileType()) || "gif".equals(file.getFileType())
                || "jpg".equals(file.getFileType()) || "jpeg".equals(file.getFileType())
                || "bmp".equals(file.getFileType()));
    }

    public static boolean isImg(String path) {
        String lowerCasePath = path.toLowerCase();
        return lowerCasePath.endsWith("png") || lowerCasePath.endsWith("jpg")
                || lowerCasePath.endsWith("jpeg") || lowerCasePath.endsWith("gif")
                || lowerCasePath.endsWith("bmp");
    }

    public static boolean isNotificationOn() {
        return MainApp.PREF_UTIL.getInt(Constant.NOTIFY_PREF, 0) != NotificationConfig.NOTIFICATION_OFF;
    }

    public static class SyncFailAction implements Action1<Throwable> {
        @Override
        public void call(Throwable throwable) {
            MainApp.IS_SYNCING = false;
            BusProvider.getInstance().post(new SyncFinishEvent(false));
            BusProvider.getInstance().post(new NetworkEvent(NetworkEvent.STATE_DISCONNECTED));
        }
    }

    public static class SyncCompleteAction implements Action1<Team> {

        @Override
        public void call(final Team teamInfoResponseData) {
            Observable.create(new Observable.OnSubscribe<Object>() {
                @Override
                public void call(Subscriber<? super Object> subscriber) {
                    Team team = getTeam();
                    if (teamInfoResponseData.getSignCode() != null) {
                        team.setSignCode(teamInfoResponseData.getSignCode());
                        team.setPrefs(teamInfoResponseData.getPrefs());
                        MainApp.PREF_UTIL.putObject(Constant.TEAM, team);
                    }
                    List<Invitation> invitations = teamInfoResponseData.getInvitations();

                    if (invitations != null) {
                        for (Invitation invitation : invitations) {
                            InvitationRealm.getInstance().addOrUpdateWithCurrentThread(invitation);
                        }
                    }
                    subscriber.onNext(null);
                }
            }).observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Object>() {
                        @Override
                        public void call(Object o) {
                            MainApp.IS_SYNCING = false;
                            BusProvider.getInstance().post(new SyncFinishEvent(true));
                            BusProvider.getInstance().post(new NetworkEvent(NetworkEvent.STATE_CONNECTED));
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            BusProvider.getInstance().post(new SyncFinishEvent(false));
                            BusProvider.getInstance().post(new NetworkEvent(NetworkEvent.STATE_DISCONNECTED));
                            Logger.e("SYNC", "failed", throwable);
                        }
                    });
        }
    }
}
