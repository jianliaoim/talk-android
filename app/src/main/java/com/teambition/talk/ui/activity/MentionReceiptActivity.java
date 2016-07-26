package com.teambition.talk.ui.activity;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.teambition.talk.BizLogic;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.adapter.MentionReceiptorAdapter;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Room;
import com.teambition.talk.entity.Story;
import com.teambition.talk.imageloader.RecyclerViewPauseOnScrollListener;
import com.teambition.talk.realm.MemberRealm;
import com.teambition.talk.realm.StoryRealm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Created by nlmartian on 2/18/16.
 */
public class MentionReceiptActivity extends BaseActivity {

    public static final String EXTRA_MENTIONED_IDS = "extra_mentioned_ids";
    public static final String EXTRA_RECEIVED_IDS = "extra_received_ids";
    public static final String EXTRA_ROOM_ID = "extra_room_id";
    public static final String EXTRA_STORY_ID = "extra_story_id";

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.listView)
    RecyclerView listView;
    private String roomId;
    private String storyId;
    private List<String> mentionedIds;
    private List<String> receivedIds;
    private MentionReceiptorAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mention_receipt);
        ButterKnife.inject(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mentionedIds = getIntent().getStringArrayListExtra(EXTRA_MENTIONED_IDS);
        receivedIds = getIntent().getStringArrayListExtra(EXTRA_RECEIVED_IDS);
        roomId = getIntent().getStringExtra(EXTRA_ROOM_ID);
        storyId = getIntent().getStringExtra(EXTRA_STORY_ID);
        if (receivedIds == null) {
            receivedIds = Collections.EMPTY_LIST;
        }


        listView.setLayoutManager(new LinearLayoutManager(this));
        listView.addOnScrollListener(new RecyclerViewPauseOnScrollListener(MainApp.IMAGE_LOADER, false, true, null));
        adapter = new MentionReceiptorAdapter();
        listView.setAdapter(adapter);

        boolean hasAll = false;
        for (String mentionedId : mentionedIds) {
            if ("all".equals(mentionedId)) {
                hasAll = true;
                mentionedIds.clear();
                Observable<List<String>> memberIdStream = null;
                if (roomId != null) {
                    memberIdStream = TalkClient.getInstance().getTalkApi()
                            .readOneRoom(roomId)
                            .map(new Func1<Room, List<String>>() {
                                @Override
                                public List<String> call(Room room) {
                                    List<String> ids = new ArrayList<String>();
                                    if (room.getMembers() != null) {
                                        for (Member member : room.getMembers()) {
                                            ids.add(member.get_id());
                                        }
                                    }
                                    return ids;
                                }
                            });
                } else {
                    memberIdStream = StoryRealm.getInstance().getSingleStory(storyId)
                            .map(new Func1<Story, List<String>>() {
                                @Override
                                public List<String> call(Story story) {
                                    return story.get_memberIds();
                                }
                            });
                }
                memberIdStream.observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<List<String>>() {
                            @Override
                            public void call(List<String> ids) {
                                mentionedIds.addAll(ids);
                                adapter.updateData(mentionedIds, receivedIds);
                                getSupportActionBar().setTitle(getString(R.string.title_receiptor_activity,
                                        receivedIds.size(), mentionedIds.size()));
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                            }
                        });
                break;
            }
        }
        if (!hasAll) {
            adapter.updateData(mentionedIds, receivedIds);
            getSupportActionBar().setTitle(getString(R.string.title_receiptor_activity,
                    receivedIds.size(), mentionedIds.size()));
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
