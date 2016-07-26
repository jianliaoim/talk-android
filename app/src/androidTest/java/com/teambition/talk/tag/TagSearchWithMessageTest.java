package com.teambition.talk.tag;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;

import com.teambition.talk.R;
import com.teambition.talk.ui.activity.TagSearchWithMessageActivity;
import com.teambition.talk.adapter.TagSearchWithMessageAdapter;
import com.teambition.talk.presenter.TagSearchWithMessagePresenter;

/**
 * Created by wlanjie on 15/7/28.
 */
public class TagSearchWithMessageTest extends ActivityInstrumentationTestCase2<TagSearchWithMessageActivity> {


    private TagSearchWithMessagePresenter mPresenter;

    private RecyclerView mRecyclerView;

    private TestTagModel mModule;

    public TagSearchWithMessageTest() {
        super(TagSearchWithMessageActivity.class);
        mModule = new TestTagModel();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Intent intent = new Intent();
        intent.putExtra("name", "name");
        intent.putExtra("tagId", "2");
        setActivityIntent(intent);
        TagSearchWithMessageActivity activity = getActivity();
        mRecyclerView = (RecyclerView) activity.findViewById(R.id.recyclerView);
        mPresenter = new TagSearchWithMessagePresenter(activity);
    }

    @UiThreadTest
    public void testReadTagWithMessage() {
        mPresenter.attachModule(mModule);
        mPresenter.readTagWithMessage(null);
        TagSearchWithMessageAdapter adapter = (TagSearchWithMessageAdapter) mRecyclerView.getAdapter();
        assertEquals(adapter.getItemCount(), 30);
    }
}
