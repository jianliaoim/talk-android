package com.teambition.talk.client.data;

/**
 * Created by wlanjie on 15/8/3.
 */
public class RepostData {

    private String _teamId;
    private String _roomId;
    private String _toId;
    private String[] _favoriteIds;

    public RepostData(String teamId, String roomId, String told, String[] favoriteIds) {
        this._roomId = roomId;
        this._teamId = teamId;
        this._toId = told;
        this._favoriteIds = favoriteIds;
    }

    public String get_teamId() {
        return _teamId;
    }

    public void set_teamId(String _teamId) {
        this._teamId = _teamId;
    }

    public String get_roomId() {
        return _roomId;
    }

    public void set_roomId(String _roomId) {
        this._roomId = _roomId;
    }

    public String get_toId() {
        return _toId;
    }

    public void set_toId(String _toId) {
        this._toId = _toId;
    }

    public String[] get_favoriteIds() {
        return _favoriteIds;
    }

    public void set_favoriteIds(String[] _favoriteIds) {
        this._favoriteIds = _favoriteIds;
    }
}
