package com.teambition.talk.entity;

import com.teambition.talk.util.StringUtil;

import org.parceler.Parcel;

import java.util.Date;

import io.realm.MemberRealmProxy;
import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

/**
 * Created by zeatual on 14/10/28.
 */
@Parcel(implementations = {MemberRealmProxy.class}, value = Parcel.Serialization.BEAN, analyze = {Member.class})
public class Member extends RealmObject {

    public static final String OWNER = "owner";
    public static final String ADMIN = "admin";
    public static final String MEMBER = "member";

    //realm key
    public static final String TEAM_MEMBER_ID = "_teamMemberId";
    public static final String TEAM_ID = "_teamId";
    public static final String ID = "_id";
    public static final String IS_QUIT = "isQuit";
    public static final String ROLE = "role";
    public static final String PINYIN = "pinyin";
    public static final String IS_ROBOT = "isRobot";
    public static final String ALIAS_PINYIN = "aliasPinyin";

    @PrimaryKey
    private String _teamMemberId;
    private String _id;
    private String _teamId;
    private String name;
    private String avatarUrl;
    private String email;
    private String mobile;
    private String phoneForLogin;
    private String pinyin;
    private Boolean isRobot;
    private String role;
    private Boolean isQuit;
    private Integer unread;
    @Ignore
    private Date pinnedAt;
    private long pinnedAtTime;
    private String alias;
    private String aliasPinyin;
    private Boolean hideMobile;
    @Ignore
    private Prefs prefs;
    private boolean isInvite;
    @Ignore
    private Date createdAt;
    private long createdAtTime;
    private String service;

    public Member(){}

    public String get_teamMemberId() {
        return _teamMemberId;
    }

    public void set_teamMemberId(String _teamMemberId) {
        this._teamMemberId = _teamMemberId;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String get_teamId() {
        return _teamId;
    }

    public void set_teamId(String _teamId) {
        this._teamId = _teamId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPhoneForLogin() {
        return phoneForLogin;
    }

    public void setPhoneForLogin(String phoneForLogin) {
        this.phoneForLogin = phoneForLogin;
    }

    public Boolean getIsRobot() {
        return isRobot == null ? false : isRobot;
    }

    public void setIsRobot(Boolean isRobot) {
        this.isRobot = isRobot;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getIsQuit() {
        return isQuit;
    }

    public void setIsQuit(Boolean isQuit) {
        this.isQuit = isQuit;
    }

    public Integer getUnread() {
        return unread == null ? 0 : unread;
    }

    public void setUnread(Integer unread) {
        this.unread = unread;
    }

    public Date getPinnedAt() {
        return pinnedAt;
    }

    public void setPinnedAt(Date pinnedAt) {
        this.pinnedAt = pinnedAt;
    }

    public String getAlias() {
        if (StringUtil.isNotBlank(alias)) {
            return alias;
        } else {
            return name;
        }
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public long getPinnedAtTime() {
        return pinnedAtTime;
    }

    public void setPinnedAtTime(long pinnedAtTime) {
        this.pinnedAtTime = pinnedAtTime;
    }

    public String getAliasPinyin() {
        return aliasPinyin;
    }

    public void setAliasPinyin(String aliasPinyin) {
        this.aliasPinyin = aliasPinyin;
    }

    public Boolean getHideMobile() {
        return hideMobile == null ? false : hideMobile;
    }

    public void setHideMobile(Boolean hideMobile) {
        this.hideMobile = hideMobile;
    }

    public Prefs getPrefs() {
        return prefs;
    }

    public void setPrefs(Prefs prefs) {
        this.prefs = prefs;
    }

    public boolean isInvite() {
        return isInvite;
    }

    public void setIsInvite(boolean isInvite) {
        this.isInvite = isInvite;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public long getCreatedAtTime() {
        return createdAtTime;
    }

    public void setCreatedAtTime(long createdAtTime) {
        this.createdAtTime = createdAtTime;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }
}
