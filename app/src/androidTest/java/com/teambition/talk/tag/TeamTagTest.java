package com.teambition.talk.tag;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.widget.TextView;

import com.teambition.talk.R;
import com.teambition.talk.ui.activity.TeamTagActivity;
import com.teambition.talk.adapter.AddTagAdapter;
import com.teambition.talk.adapter.TeamTagAdapter;
import com.teambition.talk.presenter.TeamTagPresenter;

/**
 * Created by wlanjie on 15/7/29.
 */
public class TeamTagTest extends ActivityInstrumentationTestCase2<TeamTagActivity> {

    private TeamTagPresenter mPresenter;

    public TeamTagTest() {
        super(TeamTagActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testGetTags() {
        TeamTagActivity activity = getActivity();
        RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.team_tag_recycler);
        assertNotNull(recyclerView);
        AddTagAdapter adapter = (AddTagAdapter) recyclerView.getAdapter();
        adapter.getItems().clear();
        mPresenter.attachModule(new TestTagModel());
        mPresenter.getTags();
        assertEquals(adapter.getItems().size(), 30);
    }

    @UiThreadTest
    public void testRemoveTag() {
        TeamTagActivity activity = getActivity();
        RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.team_tag_recycler);
        assertNotNull(recyclerView);
        TeamTagAdapter adapter = (TeamTagAdapter) recyclerView.getAdapter();
        int count = adapter.getItemCount();
        mPresenter = new TeamTagPresenter(activity);
        mPresenter.attachModule(new TestTagModel());
        mPresenter.removeTag("");
        assertEquals(adapter.getItemCount(), count - 1);
    }

    @UiThreadTest
    public void testUpdateTag() {
        TeamTagActivity activity = getActivity();
        mPresenter = new TeamTagPresenter(activity);
        mPresenter.attachModule(new TestTagModel());
        mPresenter.updateTag("", "");
        RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.team_tag_recycler);
        assertNotNull(recyclerView);
        LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
        TextView textView = (TextView) recyclerView.getChildViewHolder(manager.getChildAt(0)).itemView;
        assertEquals(textView.getText().toString(), "teambition");
    }
}
