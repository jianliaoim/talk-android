package com.teambition.talk.client.apis;

import com.teambition.talk.client.data.BatchInviteRequestData;
import com.teambition.talk.client.data.CreateStoryRequestData;
import com.teambition.talk.client.data.CreateTagRequestData;
import com.teambition.talk.client.data.FeedbackRequestData;
import com.teambition.talk.client.data.GroupRequestData;
import com.teambition.talk.client.data.MemberRequestData;
import com.teambition.talk.client.data.MessageAddTagData;
import com.teambition.talk.client.data.MessageRequestData;
import com.teambition.talk.client.data.RefreshSignCodeRequestData;
import com.teambition.talk.client.data.RemoveFavoritesRequestData;
import com.teambition.talk.client.data.RepostData;
import com.teambition.talk.client.data.RoomArchiveRequestData;
import com.teambition.talk.client.data.RoomUpdateRequestData;
import com.teambition.talk.client.data.SearchFavoriteResponseData;
import com.teambition.talk.client.data.SearchRequestData;
import com.teambition.talk.client.data.SearchResponseData;
import com.teambition.talk.client.data.StrikerTokenResponseData;
import com.teambition.talk.client.data.SubscribeResponseData;
import com.teambition.talk.client.data.TeamUpdateRequestData;
import com.teambition.talk.client.data.UpdateNotificationRequestData;
import com.teambition.talk.client.data.UpdateStoryRequestData;
import com.teambition.talk.client.data.UpdateTagRequestData;
import com.teambition.talk.client.data.UserUpdateData;
import com.teambition.talk.client.data.call.SaveCallUsageData;
import com.teambition.talk.entity.TeamActivity;
import com.teambition.talk.entity.Group;
import com.teambition.talk.entity.Invitation;
import com.teambition.talk.entity.Link;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Message;
import com.teambition.talk.entity.Notification;
import com.teambition.talk.entity.Preference;
import com.teambition.talk.entity.Room;
import com.teambition.talk.entity.Story;
import com.teambition.talk.entity.Tag;
import com.teambition.talk.entity.TagSearchMessage;
import com.teambition.talk.entity.Team;
import com.teambition.talk.entity.User;
import com.teambition.talk.entity.WebState;

import java.util.Date;
import java.util.List;
import java.util.Map;

import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.http.QueryMap;
import rx.Observable;

/**
 * Created by zeatual on 14-10-11.
 */
public interface TalkApi {

    @FormUrlEncoded
    @POST("/devicetokens")
    Observable<Object> postToken(@Field("token") String token);

    // Users

    @GET("/users/me")
    Observable<User> getUser();

    @GET("/users")
    Observable<List<Member>> getUserByPhone(@Query("mobiles") String mobile);

    @GET("/users")
    Observable<List<Member>> getUserByKeywords(@Query("q") String keywords);

    @GET("/users")
    Observable<List<Member>> getUserByEmails(@Query("emails") String emails);


    @POST("/users/signout")
    Observable<Object> signOut();

    @PUT("/users/{id}")
    Observable<User> updateUser(@Path("id") String userId,
                                @Body UserUpdateData data);

    @FormUrlEncoded
    @POST("/users/subscribe")
    Observable<SubscribeResponseData> subscribeUser(@Field("X-Socket-Id") String socketId);

    @POST("/teams/sync?refer=teambition")
    Observable<List<Team>> syncTeambition();

    @GET("/teams/thirds?refer=teambition")
    Observable<List<Team>> readTeambitionTeams();

    @POST("/teams/syncone")
    Observable<Team> syncOneTeam(@Query("refer") String refer, @Query("sourceId") String sourceId);

    // Preference

    @FormUrlEncoded
    @PUT("/preferences")
    Observable<Preference> updateEmailNotification(@Field("emailNotification") boolean isOn);

    @FormUrlEncoded
    @PUT("/preferences")
    Observable<Preference> updateNotifyOnRelated(@Field("notifyOnRelated") boolean isOn);

    @FormUrlEncoded
    @PUT("/preferences")
    Observable<Preference> updateMuteWhenWebOnline(@Field("muteWhenWebOnline") boolean isOn);

    @FormUrlEncoded
    @PUT("/preferences")
    Observable<Preference> updatePushOnWorkTime(
            @Field("pushOnWorkTime") boolean isOn,
            @Field("timezone") String timezone);

    @GET("/strikertoken")
    Observable<StrikerTokenResponseData> getStrikerToken();

    @GET("/state?scope=onlineweb")
    Observable<WebState> getWebState();

    // Teams
    @GET("/teams")
    Observable<List<Team>> getTeams();

    @POST("/teams")
    Observable<Team> createTeam(@Query("name") String name);

    @POST("/teams/{id}/join?fields=signCode,prefs,invitations")
    Observable<Team> joinTeam(@Path("id") String teamId);

    @POST("/teams/{id}/joinbysigncode")
    Observable<Team> joinBySignCode(@Path("id") String teamId, @Query("signCode") String signCode);

    @POST("/teams/joinbyinvitecode")
    Observable<Team> joinByInviteCode(@Query("inviteCode") String inviteCode);

    @GET("/teams/readbyinvitecode")
    Observable<Team> getTeamByInviteCode(@Query("inviteCode") String inviteCode);

    @POST("/teams/{id}/invite")
    Observable<Member> inviteViaPhone(@Path("id") String teamId,
                                      @Query("mobile") String mobile);

    @POST("/teams/{id}/invite")
    Observable<Member> inviteViaEmail(@Path("id") String teamId,
                                      @Query("email") String email);

    @POST("/teams/{id}/invite")
    Observable<Member> inviteViaUserId(@Path("id") String teamId,
                                       @Query("_userId") String userId);

    @POST("/teams/{id}/invite")
    Observable<Invitation> inviteRookieViaPhone(@Path("id") String teamId,
                                                @Query("mobile") String mobile);

    @POST("/teams/{id}/invite")
    Observable<Invitation> inviteRookieViaEmail(@Path("id") String teamId,
                                                @Query("email") String email);

    @POST("/teams/{id}/refresh")
    Observable<Team> refreshSignCode(@Path("id") String teamId,
                                     @Body RefreshSignCodeRequestData data);

    @POST("/teams/{id}/unsubscribe")
    Observable<Object> unsubscribeTeam(@Path("id") String teamId);

    @PUT("/teams/{id}")
    Observable<Team> updateTeam(@Path("id") String teamId,
                                @Body TeamUpdateRequestData data);

    @GET("/teams/{id}")
    Observable<Team> getTeamDetail(@Path("id") String teamId,
                                                   @Query("fields") String fields);

    @POST("/teams/{id}/leave")
    Observable<Object> leaveTeam(@Path("id") String teamId);

    @FormUrlEncoded
    @POST("/teams/{id}/subscribe")
    Observable<SubscribeResponseData> subscribeTeam(
            @Path("id") String teamId,
            @Field("X-Socket-Id") String socketId);

    @POST("/teams/{id}/removemember")
    Observable<Object> removeMemberFromTeam(@Path("id") String teamId, @Body MemberRequestData member);

    @POST("/teams/{id}/setmemberrole")
    Observable<Member> setMemberRole(@Path("id") String teamId, @Body MemberRequestData member);

    //Member
    @GET("/teams/{id}/members")
    Observable<List<Member>> getMembers(@Path("id") String teamId);

    // Room
    @GET("/teams/{id}/rooms")
    Observable<List<Room>> getRooms(@Path("id") String teamId);

    @GET("/rooms/{id}")
    Observable<Room> readOneRoom(@Path("id") String roomId);

    @GET("/teams/{id}/rooms?isArchived=true")
    Observable<List<Room>> getArchivedRoom(@Path("id") String teamId);

    @FormUrlEncoded
    @POST("/rooms")
    Observable<Room> createRoom(
            @Field("_teamId") String teamId,
            @Field("topic") String topic,
            @Field("color") String color,
            @Field("purpose") String goal);

    @FormUrlEncoded
    @POST("/rooms")
    Observable<Room> createRoom(
            @Field("_teamId") String teamId,
            @Field("topic") String topic,
            @Field("isPrivate") boolean isPrivate);

    @FormUrlEncoded
    @PUT("/rooms/{id}")
    Observable<Room> updateRoom(
            @Path("id") String roomId,
            @Field("topic") String topic,
            @Field("purpose") String purpose,
            @Field("color") String color);

    @FormUrlEncoded
    @PUT("/rooms/{id}")
    Observable<Room> updateRoom(@Path("id") String roomId,
                                @Field("isPrivate") boolean isPrivate);

    @PUT("/rooms/{id}")
    Observable<Room> updateRoom(@Path("id") String roomId, @Body RoomUpdateRequestData data);

    @POST("/rooms/{id}/leave")
    Observable<Object> leaveRoom(@Path("id") String roomId);

    @DELETE("/rooms/{id}")
    Observable<Room> deleteRoom(@Path("id") String roomId);

    @POST("/rooms/{id}/archive")
    Observable<Room> archiveRoom(@Path("id") String roomId,
                                 @Body RoomArchiveRequestData data);

    @POST("/rooms/{id}/join")
    Observable<Room> joinRoom(@Path("id") String roomId);

    @POST("/rooms/{id}/batchinvite")
    Observable<List<Member>> batchInviteToRoom(
            @Path("id") String roomId,
            @Body BatchInviteRequestData data);

    @FormUrlEncoded
    @POST("/rooms/{id}/removemember")
    Observable<Object> removeMemberFromRoom(@Path("id") String roomId,
                                            @Field("_userId") String memberId);

    // Message

    @GET("/messages")
    Observable<List<Message>> getMessage(@QueryMap Map<String, String> queryMap);

    @GET("/messages")
    Observable<List<Message>> getMsgWithUser(
            @Query("_withId") String targetUserId,
            @Query("_teamId") String teamId,
            @Query("limit") int limit);

    @GET("/messages")
    Observable<List<Message>> getMoreOldMsgWithUser(
            @Query("_withId") String targetUserId,
            @Query("_teamId") String teamId,
            @Query("_maxId") String maxId,
            @Query("limit") int limit);

    @GET("/messages")
    Observable<List<Message>> getMoreNewMsgWithUser(
            @Query("_withId") String targetUserId,
            @Query("_teamId") String teamId,
            @Query("_minId") String minId,
            @Query("limit") int limit);

    @GET("/messages")
    Observable<List<Message>> getMsgOfRoom(
            @Query("_roomId") String roomId,
            @Query("_teamId") String teamId,
            @Query("limit") int limit);

    @GET("/messages")
    Observable<List<Message>> getMoreOldMsgOfRoom(
            @Query("_roomId") String roomId,
            @Query("_teamId") String teamId,
            @Query("_maxId") String maxId,
            @Query("limit") int limit);

    @GET("/messages")
    Observable<List<Message>> getMoreNewMsgOfRoom(
            @Query("_roomId") String roomId,
            @Query("_teamId") String teamId,
            @Query("_minId") String minId,
            @Query("limit") int limit);


    @GET("/messages")
    Observable<List<Message>> getMsgOfStory(
            @Query("_storyId") String storyId,
            @Query("_teamId") String teamId,
            @Query("limit") int limit);

    @GET("/messages")
    Observable<List<Message>> getMoreOldMsgOfStory(
            @Query("_storyId") String storyId,
            @Query("_teamId") String teamId,
            @Query("_maxId") String maxId,
            @Query("limit") int limit);

    @GET("/messages")
    Observable<List<Message>> getMoreNewMsgOfStory(
            @Query("_storyId") String storyId,
            @Query("_teamId") String teamId,
            @Query("_minId") String minId,
            @Query("limit") int limit);

    @GET("/messages")
    Observable<List<Message>> getMsgOfStoryBeside(
            @Query("_storyId") String storyId,
            @Query("_teamId") String teamId,
            @Query("_besideId") String besideId,
            @Query("limit") int limit);

    @POST("/messages")
    Observable<Message> sendMessage(@Body MessageRequestData data);

    @DELETE("/messages/{id}")
    Observable<Object> deleteMessage(@Path("id") String messageId);

    @FormUrlEncoded
    @PUT("/messages/{id}")
    Observable<Message> updateMessage(@Path("id") String messageId,
                                      @Field(("body")) String body);

    @POST("/messages/{id}/receipt")
    Observable<Message> sendMessageReceipt(@Path("id") String messageId);

    // pin

    @POST("/teams/{teamId}/pin/{id}")
    Observable<Object> pin(@Path("teamId") String teamId,
                           @Path("id") String id);

    @POST("/teams/{teamId}/unpin/{id}")
    Observable<Object> unpin(@Path("teamId") String teamId,
                             @Path("id") String id);

    // search

    @FormUrlEncoded
    @POST("/messages/search")
    Observable<SearchResponseData> searchMessages(@Field("_teamId") String teamId,
                                                  @Field("q") String keyword,
                                                  @Field("limit") int limit);

    @FormUrlEncoded
    @POST("/messages/search")
    Observable<SearchResponseData> searchMessages(@Field("_teamId") String teamId,
                                                  @Field("q") String keyword,
                                                  @Field("page") int page,
                                                  @Field("limit") int limit,
                                                  @Field("_creatorId") String creatorId,
                                                  @Field("_roomId") String roomId,
                                                  @Field("isDirectMessage") Boolean isDirectMessage,
                                                  @Field("hasTag") Boolean hasTag,
                                                  @Field("_tagId") String tagId,
                                                  @Field("type") String type,
                                                  @Field("timeRange") String timeRange);

    @POST("/messages/search")
    Observable<SearchResponseData> search(@Body SearchRequestData data);

    @GET("/favorites")
    Observable<List<Message>> getFavorites(@Query("_teamId") String teamId);

    @GET("/favorites")
    Observable<List<Message>> getFavorites(@Query("_teamId") String teamId, @Query("_maxId") String maxId);

    @FormUrlEncoded
    @POST("/favorites")
    Observable<Object> favoriteMessage(@Field("_messageId") String messageId);

    @POST("/favorites/search")
    Observable<SearchFavoriteResponseData> searchFavorites(@Body SearchRequestData data);

    @POST("/favorites/batchremove")
    Observable<Object> batchRemoveFavorites(@Body RemoveFavoritesRequestData data);

    @POST("/tags")
    Observable<Tag> createTag(@Body CreateTagRequestData data);

    @GET("/tags")
    Observable<List<Tag>> getTags(@Query("_teamId") String teamId);

    @PUT("/messages/{messageId}")
    Observable<Message> createMessageTag(@Path("messageId") String messageId, @Body MessageAddTagData data);

    @DELETE("/tags/{id}")
    Observable<Tag> removeTag(@Path("id") String tagId);

    @PUT("/tags/{id}")
    Observable<Tag> updateTag(@Path("id") String tagId, @Body UpdateTagRequestData data);

    @POST("/messages/search")
    Observable<TagSearchMessage> readTagWithMessage(@Body SearchRequestData data);

    @GET("/messages/tags")
    Observable<List<Message>> readTagWithMessage(
            @Query("_teamId") String teamId,
            @Query("_tagId") String tagId,
            @Query("isDirectMessage") boolean isDirectMessage,
            @Query("limit") int limit
    );

    @GET("/messages/tags")
    Observable<List<Message>> readMoreTagWithMessage(
            @Query("_teamId") String teamId,
            @Query("_tagId") String tagId,
            @Query("_maxId") String maxId,
            @Query("isDirectMessage") boolean isDirectMessage,
            @Query("limit") int limit
    );

    @GET("/messages/mentions")
    Observable<List<Message>> getMentionedMessages(@Query("_teamId") String teamId,
                                                   @Query("limit") int limit);

    @GET("/messages/mentions")
    Observable<List<Message>> getMoreMentionedMessages(@Query("_teamId") String teamId,
                                                       @Query("maxId") String maxId,
                                                       @Query("limit") int limit);
    @POST("/messages/{id}/repost")
    Observable<Message> repostMessage(@Path("id") String messageId, @Body RepostData data);

    @POST("/favorites/{id}/repost")
    Observable<Message> favoritesRepostMessage(@Path("id") String messageId, @Body RepostData data);

    @POST("/favorites/reposts")
    Observable<List<Message>> favoritesRepostsMessage(@Body RepostData data);

    @GET("/teams/{teamId}/members?isQuit=true")
    Observable<List<Member>> leaveMember(@Path("teamId") String teamId);

    @DELETE("/invitations/{id}")
    Observable<Invitation> removeInvitation(@Path("id") String id);

    // story

    @POST("/stories")
    Observable<Story> createStory(@Body CreateStoryRequestData data);

    @PUT("/stories/{id}")
    Observable<Story> updateStory(@Path("id") String id,
                                  @Body UpdateStoryRequestData data);

    @GET("/stories/{id}")
    Observable<Story> getOneStory(@Path("id") String id);

    @DELETE("/stories/{id}")
    Observable<Story> deleteStory(@Path("id") String id);

    @POST("/stories/{id}/leave")
    Observable<Story> leaveStory(@Path("id") String id);

    // notification

    @GET("/notifications")
    Observable<List<Notification>> getNotifications(@Query("_teamId") String teamId,
                                                    @Query("maxUpdatedAt") Date maxUpdatedAt,
                                                    @Query("limit") Integer limit);

    @PUT("/notifications/{id}")
    Observable<Notification> updateNotification(@Path("id") String id,
                                                @Body UpdateNotificationRequestData data);

    @GET("/discover/urlmeta")
    Observable<Link> getUrlMeta(@Query("url") String url);

    // group

    @GET("/groups")
    Observable<List<Group>> getGroups(@Query("_teamId") String teamId);

    @POST("/groups")
    Observable<Group> createGroup(@Body GroupRequestData data);

    @PUT("/groups/{groupId}")
    Observable<Group> updateGroup(@Path("groupId") String groupId,
                                  @Body GroupRequestData data);

    @DELETE("/groups/{id}")
    Observable<Group> removeGroup(@Path("id") String id);

    @POST("/services/webhook/{id}")
    Observable<Object> feedback(@Path("id") String id, @Body FeedbackRequestData data);

    @DELETE("/activities/{id}")
    Observable<TeamActivity> deleteActivity(@Path("id") String id);

    @GET("/activities")
    Observable<List<TeamActivity>> getTeamActivities(
            @Query("_teamId") String teamId,
            @Query("limit") int limit
    );

    @GET("/activities")
    Observable<List<TeamActivity>> getOldTeamActivities(
            @Query("_teamId") String teamId,
            @Query("limit") int limit,
            @Query("_maxId") String maxId
    );

    @POST("/usages/call")
    Observable<Object> saveCallUsage(@Body SaveCallUsageData data);
}
