package com.teambition.talk.tag;

import android.support.v7.widget.RecyclerView;
import android.test.ActivityInstrumentationTestCase2;

import com.teambition.talk.R;
import com.teambition.talk.ui.activity.AddTagActivity;
import com.teambition.talk.adapter.AddTagAdapter;
import com.teambition.talk.entity.Message;
import com.teambition.talk.presenter.TagPresenter;
import com.teambition.talk.view.TagView;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Created by wlanjie on 15/7/28.
 */
public class AddTagTest extends ActivityInstrumentationTestCase2<AddTagActivity> {

    private TagPresenter mPresenter;

    private final TestTagModel mModule;

    private RecyclerView mRecyclerView;

    public AddTagTest() {
        super(AddTagActivity.class);
        mModule = new TestTagModel();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        AddTagActivity activity = getActivity();
        mPresenter = new TagPresenter(activity);
        mRecyclerView = (RecyclerView) activity.findViewById(R.id.tag_recycler);
    }

    public void testAddTag() {
        mPresenter.attachModule(mModule);
        mPresenter.createTag(null);
        assertNotNull(mRecyclerView);
        AddTagAdapter adapter = (AddTagAdapter) mRecyclerView.getAdapter();
        assertEquals(adapter.getItems().get(0).getName(), "bbbbb");
    }

    public void testGetTags() {
        assertNotNull(mRecyclerView);
        AddTagAdapter adapter = (AddTagAdapter) mRecyclerView.getAdapter();
        adapter.getItems().clear();
        mPresenter.attachModule(mModule);
        mPresenter.getTags();
        assertEquals(adapter.getItems().size(), 30);
    }

    public void testCreateMessageTag() {
        TagView mockTagView = mock(TagView.class);
        mPresenter = new TagPresenter(mockTagView);
        mPresenter.attachModule(mModule);
        mPresenter.createMessageTag("", null);

        verify(mockTagView).onCreateMessageTag(any(Message.class));
//        assertNotNull(mMessage);
//        assertEquals(mMessage.getTags().size(), 3);
    }
}
