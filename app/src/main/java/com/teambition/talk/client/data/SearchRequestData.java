package com.teambition.talk.client.data;

import com.teambition.talk.BizLogic;

import org.parceler.Parcel;

/**
 * Created by zeatual on 15/5/6.
 */
@Parcel
public class SearchRequestData {

    public static final String TYPE_FILE = "file";
    public static final String TYPE_LINK = "url";
    public static final String TYPE_RTF = "rtf";
    public static final String TYPE_SNIPPET = "snippet";

    public String _teamId;
    public String _roomId;
    public String _creatorId;
    public String[] _creatorIds;
    public String _toId;
    public String[] _toIds;
    public String type;
    public String fileCategory;
    public String q;
    public int limit = 30;
    public int page = 1;
    public boolean isDirectMessage;
    public Sort sort;
    public String _tagId;

    public SearchRequestData(){}

    public SearchRequestData(String _teamId, String type) {
        this._teamId = _teamId;
        this.type = type;
        clearRestrictions();
    }

    public void setTagId(String tagId) {
        clearRestrictions();
        _tagId = tagId;
    }

    public void setRoomId(String roomId) {
        clearRestrictions();
        _roomId = roomId;
    }

    public void setMemberId(String memberId) {
        clearRestrictions();
        _creatorIds = new String[]{BizLogic.getUserInfo().get_id(), memberId};
        _toIds = new String[]{memberId, BizLogic.getUserInfo().get_id()};
    }

    public void setKeyword(String keyword) {
        clearRestrictions();
        q = keyword;
    }

    public void setType(String type) {
        this.type = type;
        clearRestrictions();
    }

    public void clearRestrictions() {
        _roomId = null;
        _toIds = null;
        _creatorIds = null;
        _tagId = null;
        q = null;
        limit = 30;
        page = 1;
        isDirectMessage = false;
        sort = new Sort(Sort.DESC);
    }

    @Parcel
    public static class Sort {
        public static final String DESC = "desc";
        public static final String ASC = "asc";
        OrderBy createdAt;
        OrderBy favoritedAt;

        public Sort(){}

        public Sort(String order) {
            createdAt = new OrderBy(order);
        }

        public void byFavoritedAt(String order) {
            createdAt = null;
            favoritedAt = new OrderBy(order);
        }
    }

    @Parcel
    public static class OrderBy {
        String order;
        public OrderBy(){}

        public OrderBy(String order) {
            this.order = order;
        }
    }

    public SearchRequestData copy() {
        SearchRequestData data = new SearchRequestData(_teamId, type);
        data._roomId = _roomId;
        data._creatorIds = _creatorIds;
        data._toId = _toId;
        data._toIds = _toIds;
        data._tagId = _tagId;
        data.type = type;
        data.fileCategory = fileCategory;
        data.q = q;
        data.limit = limit;
        data.page = page;
        data.isDirectMessage = isDirectMessage;
        data.sort = sort;
        return data;
    }
}
