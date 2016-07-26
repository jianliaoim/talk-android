package com.teambition.talk.entity;

import com.google.gson.annotations.Expose;
import com.squareup.otto.Bus;
import com.teambition.talk.BizLogic;
import com.teambition.talk.Constant;
import com.teambition.talk.MainApp;
import com.teambition.talk.event.UpdateTeamEvent;

import org.parceler.Parcel;

import java.util.List;

import io.realm.RealmObject;
import io.realm.TeamRealmProxy;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.RealmClass;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by zeatual on 14/10/28.
 */
@Parcel(implementations = {TeamRealmProxy.class}, value = Parcel.Serialization.BEAN, analyze = {Team.class})
@RealmClass
public class Team extends RealmObject {

    public static final String ID = "_id";

    @Expose
    @PrimaryKey
    private String _id;
    @Expose
    private String name;
    @Expose
    private String sourceId;
    @Expose
    private String source;
    @Expose
    private String color;
    @Expose
    private String signCode;
    @Expose
    private String inviteCode;
    @Expose
    private String inviteUrl;
    @Expose
    private boolean isQuit;
    @Expose
    private boolean nonJoinable;
    @Expose
    private boolean hasUnread;
    @Expose
    private int unread;
    @Ignore
    private Prefs prefs;
    @Ignore
    private List<Member> members;
    @Ignore
    private List<Room> rooms;
    @Ignore
    private List<Message> latestMessages;
    @Ignore
    private List<Invitation> invitations;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isQuit() {
        return isQuit;
    }

    public void setQuit(boolean isQuit) {
        this.isQuit = isQuit;
    }

    public boolean isNonJoinable() {
        return nonJoinable;
    }

    public void setNonJoinable(boolean nonJoinable) {
        this.nonJoinable = nonJoinable;
    }

    public boolean isHasUnread() {
        return hasUnread;
    }

    public void setHasUnread(boolean hasUnread) {
        this.hasUnread = hasUnread;
    }

    public int getUnread() {
        return unread;
    }

    public void setUnread(int unread) {
        this.unread = unread;
    }

    public Prefs getPrefs() {
        return prefs;
    }

    public void setPrefs(Prefs prefs) {
        this.prefs = prefs;
    }

    public String getSignCode() {
        return signCode;
    }

    public void setSignCode(String signCode) {
        this.signCode = signCode;
    }

    public String getInviteUrl() {
        return inviteUrl;
    }

    public void setInviteUrl(String inviteUrl) {
        this.inviteUrl = inviteUrl;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public static void update(final Team team, final Bus bus) {
        Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                if (BizLogic.isCurrentTeam(team.get_id())) {
                    MainApp.PREF_UTIL.putObject(Constant.TEAM, team);
                    subscriber.onNext(null);
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        bus.post(new UpdateTeamEvent());
                    }
                });

    }

    public void setIsQuit(boolean isQuit) {
        this.isQuit = isQuit;
    }

    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

    public List<Message> getLatestMessages() {
        return latestMessages;
    }

    public void setLatestMessages(List<Message> latestMessages) {
        this.latestMessages = latestMessages;
    }

    public List<Invitation> getInvitations() {
        return invitations;
    }

    public void setInvitations(List<Invitation> invitations) {
        this.invitations = invitations;
    }

    /*
    @Override
    public int compareTo(Team another) {
        int result = 0;
        if (getUnread() < another.getUnread()) {
            result = 1;
        }
        if (getUnread() > another.getUnread()) {
            result = -1;
        }
        return result;
    }
    */
}
