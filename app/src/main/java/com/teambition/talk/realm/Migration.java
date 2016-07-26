package com.teambition.talk.realm;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.internal.Table;

/**
 * Created by wlanjie on 15/10/16.
 *
 * 这个类控制Realm版本,包括每个表字段的增加/删除, 每次修改之后需要将version+1
 *
 * 为Message Table增加一个字段示例:
 * Table messageTable = realm.getTable(Message.class);
 * message.addColumn(ColumnType.STRING, "_id");
 * //是否可以插入null值
 * message.addColumn(ColumnType.STRING, "_id", true);
 *
 * 为Member Table删除一个字段示例:
 * Table memberTable = realm.getTable(Member.class);
 * memberTable.removeColumn(getIndexForProperty(memberTable, "_id"));
 *
 * getIndexForProperty方法是获取"_id"这个列的位置
 */
public class Migration implements RealmMigration {

    private long getIndexForProperty(Table table, String name) {
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (table.getColumnName(i).equals(name)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void migrate(DynamicRealm dynamicRealm, long oldVersion, long newVersion) {
        if (oldVersion == 0) {
            dynamicRealm.getSchema().get("Invitation").addField("email", String.class);
            oldVersion++;
        }
        if (oldVersion == 1) {
            dynamicRealm.getSchema().get("Message").addField("receiptorsStr", String.class);
            dynamicRealm.getSchema().get("Member").addField("service", String.class);
            dynamicRealm.getSchema().create("IdeaDraft")
                    .addField("title", String.class)
                    .addField("description", String.class);
            dynamicRealm.getSchema().rename("TeamInfo", "Team");
            dynamicRealm.getSchema().get("Team").addField("source", String.class);
            dynamicRealm.getSchema().get("Team").addField("sourceId", String.class);
            dynamicRealm.getSchema().get("Team").addField("signCode", String.class);
            dynamicRealm.getSchema().get("Team").addField("inviteCode", String.class);
            dynamicRealm.getSchema().get("Team").addField("inviteUrl", String.class);
            dynamicRealm.getSchema().get("Team").addField("isQuit", boolean.class);
            dynamicRealm.getSchema().get("Team").addField("nonJoinable", boolean.class);
            dynamicRealm.getSchema().get("Team").addField("hasUnread", boolean.class);
            dynamicRealm.getSchema().get("Team").addField("unread", int.class);
            dynamicRealm.getSchema().get("Team").addPrimaryKey("_id");

            RealmObjectSchema roomSchema = dynamicRealm.getSchema().get("Room");
            dynamicRealm.getSchema().get("Notification").addRealmObjectField("room", roomSchema);
            RealmObjectSchema memberSchema = dynamicRealm.getSchema().get("Member");
            dynamicRealm.getSchema().get("Notification").addRealmObjectField("member", memberSchema);
            RealmObjectSchema storySchema = dynamicRealm.getSchema().get("Story");
            dynamicRealm.getSchema().get("Notification").addRealmObjectField("story", storySchema);

            oldVersion++;
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Migration;
    }
}
