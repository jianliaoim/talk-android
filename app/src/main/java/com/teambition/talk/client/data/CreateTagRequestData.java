package com.teambition.talk.client.data;

/**
 * Created by wlanjie on 15/7/14.
 */
public class CreateTagRequestData {
    private String _teamId;
    private String name;

    public CreateTagRequestData(String _teamId, String name) {
        this._teamId = _teamId;
        this.name = name;
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
}
