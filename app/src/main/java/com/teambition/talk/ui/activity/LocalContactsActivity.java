package com.teambition.talk.ui.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.squareup.otto.Subscribe;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.adapter.ContactsAdapter;
import com.teambition.talk.entity.Contact;
import com.teambition.talk.event.NewInvitationEvent;
import com.teambition.talk.presenter.LocalContactsPresenter;
import com.teambition.talk.ui.widget.MaterialSearchView;
import com.teambition.talk.view.LocalContactsView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by zeatual on 15/6/24.
 */
public class LocalContactsActivity extends BaseActivity implements LocalContactsView {

    private static final int READ_CONTACTS_PERMISSION = 0;
    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.search_view)
    MaterialSearchView searchView;
    @InjectView(R.id.recyclerView)
    RecyclerView recyclerView;

    private ContactsAdapter adapter;
    private LocalContactsPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_contacts);
        ButterKnife.inject(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.local_contacts);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        presenter = new LocalContactsPresenter(this, this);
        adapter = new ContactsAdapter(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) {
                MainApp.showToastMsg(R.string.record_contacts_permission_denied);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_PERMISSION);
            }
        } else {
            presenter.getContacts();
        }
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filterName(newText);
                return false;
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case READ_CONTACTS_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    presenter.getContacts();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem searchItem = menu.add(Menu.NONE, 0, Menu.NONE, R.string.action_search).setIcon(R.drawable.ic_search);
        searchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        searchView.setMenuItem(searchItem);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLoadContactsFinish(List<Contact> contacts) {
        adapter.updateData(contacts);
    }

    @Subscribe
    public void onNewInvitationEvent(NewInvitationEvent event) {
        presenter.getContacts();
    }
}