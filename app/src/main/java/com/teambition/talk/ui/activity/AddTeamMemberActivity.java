package com.teambition.talk.ui.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.talk.dialog.TalkDialog;
import com.teambition.talk.BizLogic;
import com.teambition.talk.BusProvider;
import com.teambition.talk.Constant;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.event.UpdateMemberEvent;
import com.teambition.talk.realm.MemberRealm;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.adapter.ContactsAdapter;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.client.data.RefreshSignCodeRequestData;
import com.teambition.talk.entity.Contact;
import com.teambition.talk.entity.Invitation;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.QRCodeData;
import com.teambition.talk.entity.Team;
import com.teambition.talk.presenter.LocalContactsPresenter;
import com.teambition.talk.ui.ShareDialogHelper;
import com.teambition.talk.realm.InvitationRealm;
import com.teambition.talk.util.DensityUtil;
import com.teambition.talk.util.EmojiUtil;
import com.teambition.talk.util.QRCodeUtil;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.util.ThemeUtil;
import com.teambition.talk.util.TransactionUtil;
import com.teambition.talk.view.LocalContactsView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by nlmartian on 4/17/15.
 */
public class AddTeamMemberActivity extends BaseActivity implements LocalContactsView {

    @InjectView(R.id.toolbar)
    Toolbar toolbar;
    @InjectView(R.id.recyclerView)
    RecyclerView recyclerView;
    @InjectView(R.id.et_phone_num)
    EditText etPhoneNum;
    @InjectView(R.id.img_clear)
    ImageView imgClear;
    @InjectView(R.id.section_add_local_contacts)
    TextView tvContacts;
    @InjectView(R.id.section_show_qr_code)
    TextView tvQrCode;
    @InjectView(R.id.section_share_invitation)
    TextView tvShareInvitation;

    ContactsAdapter adapter;
    LocalContactsPresenter presenter;
    String teamId = BizLogic.getTeamId();

    private ShareDialogHelper shareDialogHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_teammember);
        ButterKnife.inject(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.add_new_members);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tvContacts.setCompoundDrawablesWithIntrinsicBounds(ThemeUtil.
                        getThemeDrawable(getResources(), R.drawable.ic_local_contacts, BizLogic.getTeamColor()),
                null, null, null);
        tvQrCode.setCompoundDrawablesWithIntrinsicBounds(ThemeUtil.
                        getThemeDrawable(getResources(), R.drawable.ic_qrcode, BizLogic.getTeamColor()),
                null, null, null);
        tvShareInvitation.setCompoundDrawablesWithIntrinsicBounds(
                ThemeUtil.getThemeDrawable(getResources(), R.drawable.ic_share_grey, BizLogic.getTeamColor()),
                null, null, null);

        etPhoneNum.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    TalkClient.getInstance().getTalkApi()
                            .getUserByKeywords(v.getText().toString())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<List<Member>>() {
                                @Override
                                public void call(List<Member> members) {
                                    if (!members.isEmpty()) {
                                        imgClear.setVisibility(View.VISIBLE);
                                        recyclerView.setVisibility(View.VISIBLE);
                                        adapter.showSearchResult(members.get(0));
                                    } else {
                                        if (StringUtil.isEmail(etPhoneNum.getText().toString())) {
                                            sendInvitationByEmail();
                                        } else {
                                            sendInvitationBySms();
                                        }
                                    }
                                }

                            }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                    MainApp.showToastMsg(R.string.network_failed);
                                }
                            });

                    return true;
                }
                return false;
            }
        });

        adapter = new ContactsAdapter(this);
        adapter.setFilterMode(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);
        presenter = new LocalContactsPresenter(this, this);
        presenter.getContacts();

        Team team = BizLogic.getTeam();
        String shareContent = getString(R.string.share_invite_content, team.getName(), team.getInviteUrl(),
                EmojiUtil.BALLOON + team.getInviteCode() + EmojiUtil.BALLOON);
        shareDialogHelper = new ShareDialogHelper(this, shareContent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick({R.id.img_clear, R.id.section_add_local_contacts, R.id.section_show_qr_code, R.id.section_share_invitation})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_clear:
                etPhoneNum.setText("");
                break;
            case R.id.section_show_qr_code:
                try {
                    QRCodeData data = new QRCodeData(teamId, BizLogic.getTeamName(),
                            BizLogic.getTeamColor(), BizLogic.getSignCode());
                    View viewQRCode = LayoutInflater.from(this).inflate(R.layout.dialog_show_qr_code, null);
                    TextView tvTeamName = (TextView) viewQRCode.findViewById(R.id.tv_team_name);
                    final ImageView imageQRCode = (ImageView) viewQRCode.findViewById(R.id.image_qr_code);
                    TextView btnReset = (TextView) viewQRCode.findViewById(R.id.btn_reset);
                    if (!BizLogic.isAdmin()) {
                        btnReset.setVisibility(View.GONE);
                    }
                    tvTeamName.setText(BizLogic.getTeamName());
                    final int size = DensityUtil.dip2px(this, 144);
                    imageQRCode.setImageBitmap(QRCodeUtil.encode(Base64.encodeToString(data.toString().getBytes("UTF-8"), Base64.DEFAULT),
                            size));
                    btnReset.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            TalkClient.getInstance().getTalkApi()
                                    .refreshSignCode(teamId, new RefreshSignCodeRequestData())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Action1<Team>() {
                                        @Override
                                        public void call(Team team) {
                                            if (team != null) {
                                                MainApp.PREF_UTIL.putObject(Constant.TEAM, team);
                                                QRCodeData data = new QRCodeData(teamId, BizLogic.getTeamName(),
                                                        BizLogic.getTeamColor(), BizLogic.getSignCode());
                                                imageQRCode.setImageBitmap(QRCodeUtil.encode(Base64.encodeToString(data.toString().getBytes(), Base64.DEFAULT),
                                                        size));
                                            }
                                        }
                                    }, new Action1<Throwable>() {
                                        @Override
                                        public void call(Throwable throwable) {
                                            MainApp.showToastMsg(R.string.qr_code_refresh_error);
                                        }
                                    });
                        }
                    });
                    new TalkDialog.Builder(this)
                            .customView(viewQRCode, false)
                            .show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.section_add_local_contacts:
                TransactionUtil.goTo(this, LocalContactsActivity.class);
                break;
            case R.id.section_share_invitation:
                shareDialogHelper.showDialog();
                break;
        }
    }

    @Override
    public void onLoadContactsFinish(List<Contact> contacts) {
        adapter.updateData(contacts);
        etPhoneNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (StringUtil.isNotBlank(s.toString())) {
                    imgClear.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.filter(s.toString());
                } else {
                    imgClear.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.GONE);
                }
            }
        });
    }

    private void sendInvitationBySms() {
        new TalkDialog.Builder(AddTeamMemberActivity.this)
                .title(R.string.sms_title)
                .titleColorRes(R.color.white)
                .titleBackgroundColorRes(R.color.colorPrimary)
                .content(R.string.sms_message)
                .negativeText(R.string.cancel)
                .negativeColorRes(R.color.material_grey_700)
                .positiveText(R.string.invite)
                .callback(new TalkDialog.ButtonCallback() {
                    @Override
                    public void onPositive(TalkDialog dialog, View v) {
                        super.onPositive(dialog, v);
                        TalkClient.getInstance().getTalkApi()
                                .inviteRookieViaPhone(BizLogic.getTeamId(),
                                        etPhoneNum.getText().toString())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Invitation>() {
                                    @Override
                                    public void call(Invitation invitation) {
                                        InvitationRealm.getInstance().addOrUpdate(invitation)
                                                .subscribe(new Action1<Invitation>() {
                                                    @Override
                                                    public void call(Invitation invitation) {

                                                    }
                                                }, new RealmErrorAction());
                                        MainApp.IS_MEMBER_CHANGED = true;
                                        Uri smsToUri = Uri.parse("smsto:" + etPhoneNum.getText());
                                        Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
                                        intent.putExtra("sms_body",
                                                String.format(getString(R.string.sms_content),
                                                        BizLogic.getTeamName(),
                                                        BizLogic.getTeamInviteUrl()));
                                        startActivity(intent);
                                    }
                                }, new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable throwable) {
                                        MainApp.showToastMsg(R.string.network_failed);
                                    }
                                });
                    }
                }).show();
    }

    private void sendInvitationByEmail() {
        new TalkDialog.Builder(AddTeamMemberActivity.this)
                .title(R.string.sms_title)
                .titleColorRes(R.color.white)
                .titleBackgroundColorRes(R.color.colorPrimary)
                .content(R.string.email_message)
                .negativeText(R.string.cancel)
                .negativeColorRes(R.color.material_grey_700)
                .positiveText(R.string.invite)
                .callback(new TalkDialog.ButtonCallback() {
                    @Override
                    public void onPositive(TalkDialog dialog, View v) {
                        super.onPositive(dialog, v);
                        TalkClient.getInstance().getTalkApi()
                                .inviteRookieViaEmail(BizLogic.getTeamId(),
                                        etPhoneNum.getText().toString())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Invitation>() {
                                    @Override
                                    public void call(Invitation invitation) {
                                        InvitationRealm.getInstance().addOrUpdate(invitation)
                                                .subscribe(new Action1<Invitation>() {
                                                    @Override
                                                    public void call(Invitation invitation) {
                                                        MainApp.showToastMsg(R.string.invitation_sent);
                                                    }
                                                }, new RealmErrorAction());
                                        MainApp.IS_MEMBER_CHANGED = true;
                                    }
                                }, new Action1<Throwable>() {
                                    @Override
                                    public void call(Throwable throwable) {
                                        MainApp.showToastMsg(R.string.network_failed);
                                    }
                                });
                    }
                }).show();
    }

}
