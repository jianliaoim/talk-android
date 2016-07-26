package com.teambition.talk.client.data;

import com.teambition.talk.entity.Attachment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zeatual on 14/12/10.
 */
public class MessageRequestData {

    public String _teamId;
    public String _storyId;
    public String _roomId;
    public String _toId;
    public String body;
    public List<Attachment> attachments;
    public String _maxId;
    public String _minId;
    public String _besideId;
    public Boolean hasFile;
    public String fileCategory;
    public Integer limit;
    public String reservedType;

    public MessageRequestData() {
    }

    public MessageRequestData(String _teamId, String _roomId, String _toId, String _storyId,
                              String body, List<Attachment> attachments) {
        this._teamId = _teamId;
        this._roomId = _roomId;
        this._toId = _toId;
        this._storyId = _storyId;
        this.body = body;
        this.attachments = attachments;
    }

    public MessageRequestData(String _teamId, String _roomId, String _toId, String _storyId,
                              String body, List<Attachment> attachments, String reservedType) {
        this._teamId = _teamId;
        this._roomId = _roomId;
        this._toId = _toId;
        this._storyId = _storyId;
        this.body = body;
        this.attachments = attachments;
        this.reservedType = reservedType;
    }

    public static Map<String, String> getImageQueryMap(String _teamId, String _roomId, String _toId, String _storyId,
                                                       String _maxId, String _minId, String _besideId, String _creatorId) {
        Map<String, String> map = new HashMap<>();
        map.put("_teamId", _teamId);
        if (_roomId != null) {
            map.put("_roomId", _roomId);
        }
        if (_toId != null) {
            map.put("_toId", _toId);
        }
        if (_storyId != null) {
            map.put("_storyId", _storyId);
        }
        if (_maxId != null) {
            map.put("_maxId", _maxId);
        }
        if (_minId != null) {
            map.put("_minId", _minId);
        }
        if (_besideId != null) {
            map.put("_besideId", _besideId);
        }
        if (_besideId != null) {
            map.put("limit", Integer.toString(15));
        } else {
            map.put("limit", Integer.toString(30));
        }
        if (_creatorId != null) {
            map.put("_creatorId", _creatorId);
        }
        map.put("hasFile", Boolean.toString(true));
        map.put("fileCategory", "image");
        return map;
    }
}
