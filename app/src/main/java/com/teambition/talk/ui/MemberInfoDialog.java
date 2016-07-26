package com.teambition.talk.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.joooonho.SelectableRoundedImageView;
import com.makeramen.roundedimageview.RoundedImageView;
import com.talk.dialog.TalkDialog;
import com.teambition.talk.BizLogic;
import com.teambition.talk.BusProvider;
import com.teambition.talk.imageloader.ImageLoaderConfig;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.entity.Member;
import com.teambition.talk.entity.Notification;
import com.teambition.talk.event.CallPhoneEvent;
import com.teambition.talk.event.RemoveNotificationEvent;
import com.teambition.talk.event.SyncLeaveMemberFinisEvent;
import com.teambition.talk.presenter.MemberDetailPresenter;
import com.teambition.talk.realm.MemberRealm;
import com.teambition.talk.realm.NotificationRealm;
import com.teambition.talk.ui.activity.ChatActivity;
import com.teambition.talk.ui.activity.PreferenceActivity;
import com.teambition.talk.ui.activity.TopicSettingActivity;
import com.teambition.talk.util.TransactionUtil;
import com.teambition.talk.view.MemberDetailView;

import org.parceler.Parcels;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by wlanjie on 15/9/16.
 */
public class MemberInfoDialog extends AppCompatDialog implements Toolbar.OnMenuItemClickListener, MemberDetailView {

    private Context mContext;
    private Member mMember;
    private MemberDetailPresenter mPresenter;
    private MenuItem mItemSetAdmin;

    protected MemberInfoDialog(Context context, Member member) {
        super(context);
        mContext = context;
        mMember = member;
        mPresenter = new MemberDetailPresenter(this);
        setContentView(getContentView());
    }

    protected MemberInfoDialog(Context context, int theme, Member member) {
        super(context, theme);
        mContext = context;
        mMember = member;
        mPresenter = new MemberDetailPresenter(this);
        setContentView(getContentView());
    }

    protected MemberInfoDialog(Context context, boolean cancelable, OnCancelListener cancelListener, Member member) {
        super(context, cancelable, cancelListener);
        mContext = context;
        mMember = member;
        mPresenter = new MemberDetailPresenter(this);
        setContentView(getContentView());
    }

    private View getContentView() {
        if (mMember == null) return null;
        final View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_member_info, null);
        final Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        final RoundedImageView avatarImage = (RoundedImageView) view.findViewById(R.id.avatar);
        final TextView nameText = (TextView) view.findViewById(R.id.name);
        final View phoneEmailView = view.findViewById(R.id.phone_email_layout);
        final TextView phoneNumberText = (TextView) view.findViewById(R.id.phone_number);
        final TextView emailText = (TextView) view.findViewById(R.id.email);
        final View phoneMessageView = view.findViewById(R.id.phone_message_layout);
        final ImageView messageImage = (ImageView) view.findViewById(R.id.send_message);
        final ImageView phoneImage = (ImageView) view.findViewById(R.id.call_phone);
        final SelectableRoundedImageView maskImage = (SelectableRoundedImageView) view.findViewById(R.id.mask);

        if (!TextUtils.isEmpty(mMember.getAvatarUrl())) {
            MainApp.IMAGE_LOADER.displayImage(mMember.getAvatarUrl(), avatarImage, ImageLoaderConfig.AVATAR_OPTIONS);
        }
        toolbar.setOnMenuItemClickListener(this);
        if (!BizLogic.isAdmin(mMember) || Member.OWNER.equals(MainApp.globalMembers.get(BizLogic.getUserInfo().get_id()).getRole())) {
            resetMenu(toolbar, mMember);
        }
        if (Member.ADMIN.equals(mMember.getRole())) {
            nameText.setText(String.format(mContext.getString(R.string.admin), mMember.getAlias()));
        } else if (Member.OWNER.equals(mMember.getRole())) {
            nameText.setText(String.format(mContext.getString(R.string.owner), mMember.getAlias()));
        } else {
            nameText.setText(mMember.getAlias());
        }
        if (TextUtils.isEmpty(mMember.getPhoneForLogin())) {
            phoneNumberText.setVisibility(View.GONE);
        } else {
            phoneNumberText.setText(mMember.getPhoneForLogin());
        }
        if (TextUtils.isEmpty(mMember.getEmail())) {
            emailText.setVisibility(View.GONE);
        } else {
            emailText.setText(mMember.getEmail());
        }
        if (TextUtils.isEmpty(mMember.getPhoneForLogin()) && TextUtils.isEmpty(mMember.getEmail())) {
            phoneEmailView.setVisibility(View.GONE);
        }
        if (BizLogic.getUserInfo() != null && BizLogic.isMe(mMember.get_id())) {
            phoneMessageView.setVisibility(View.GONE);
        } else {
            if (phoneEmailView.getVisibility() == View.VISIBLE) {
            }
            phoneMessageView.setVisibility(View.VISIBLE);
        }
        messageImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                if (mContext instanceof TopicSettingActivity || mContext instanceof ChatActivity) {
                    ((Activity) mContext).finish();
                }
                Bundle bundle = new Bundle();
                bundle.putParcelable(ChatActivity.EXTRA_MEMBER, Parcels.wrap(mMember));
                TransactionUtil.goTo((Activity) mContext, ChatActivity.class, bundle);
            }
        });
        phoneImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                BusProvider.getInstance().post(new CallPhoneEvent(mMember));
            }
        });
        if (phoneEmailView.getVisibility() == View.GONE && phoneMessageView.getVisibility() == View.GONE) {
            maskImage.setCornerRadiiDP(2, 2, 2, 2);
        } else {
            maskImage.setCornerRadiiDP(2, 2, 0, 0);
        }
        return view;
    }

    private void resetMenu(Toolbar toolbar, Member member) {
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.menu_member_info);
        Menu menu = toolbar.getMenu();
        if (BizLogic.isMe(member.get_id())) {
            menu.findItem(R.id.action_set_admin).setVisible(false);
            menu.findItem(R.id.action_kick_out).setVisible(false);
        } else {
            menu.findItem(R.id.action_edit_profile).setVisible(false);
            if (BizLogic.isAdmin() &&
                    !Member.OWNER.equals(member.getRole()) && !BizLogic.isXiaoai(member)) {
                if (Member.ADMIN.equals(member.getRole())) {
                    mItemSetAdmin = menu.findItem(R.id.action_set_admin);
                    mItemSetAdmin.setTitle(R.string.action_set_normal);
                }
            } else {
                menu.findItem(R.id.action_set_admin).setVisible(false);
                menu.findItem(R.id.action_kick_out).setVisible(false);
            }
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_edit_profile:
                dismiss();
                TransactionUtil.goTo((Activity) mContext, PreferenceActivity.class);
                break;
            case R.id.action_set_admin:
                if (Member.ADMIN.equals(mMember.getRole())) {
                    mPresenter.setRole(mMember, Member.MEMBER);
                } else {
                    mPresenter.setRole(mMember, Member.ADMIN);
                }
                dismiss();
                break;
            case R.id.action_kick_out:
                new TalkDialog.Builder(mContext)
                        .title(R.string.title_remove_member)
                        .titleColorRes(R.color.white)
                        .titleBackgroundColorRes(R.color.talk_warning)
                        .content(R.string.confirm_remove_member_from_team)
                        .positiveText(R.string.confirm)
                        .positiveColorRes(R.color.talk_warning)
                        .negativeText(R.string.cancel)
                        .negativeColorRes(R.color.material_grey_700)
                        .callback(new TalkDialog.ButtonCallback() {
                            @Override
                            public void onPositive(TalkDialog materialDialog, View v) {
                                mPresenter.removeTeamMember(mMember);
                            }
                        })
                        .show();
                break;
        }
        return true;
    }


    @Override
    public void onSetAdminSuccess(String role) {
        if (mItemSetAdmin == null) return;
        if (Member.ADMIN.equals(role)) {
            mItemSetAdmin.setTitle(R.string.action_set_normal);
        } else {
            mItemSetAdmin.setTitle(R.string.action_set_admin);
        }
    }

    @Override
    public void onRemoveMemberSuccess() {
        if (mMember == null) return;
        mMember.setIsQuit(true);
        MemberRealm.getInstance().addOrUpdate(mMember)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Member>() {
                    @Override
                    public void call(Member member) {
                        BusProvider.getInstance().post(new SyncLeaveMemberFinisEvent(member));
                    }
                }, new RealmErrorAction());
        NotificationRealm.getInstance()
                .removeByTargetId(mMember.get_id())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Notification>() {
                    @Override
                    public void call(Notification notification) {
                        if (notification != null) {
                            BusProvider.getInstance().post(new RemoveNotificationEvent(notification));
                        }
                    }
                }, new RealmErrorAction());
        dismiss();
    }

    @Override
    public void showProgressDialog(int message) {

    }

    @Override
    public void dismissProgressDialog() {

    }

    @Override
    public void showProgressBar() {

    }

    @Override
    public void dismissProgressBar() {

    }

    public static class Builder {

        private Member member;
        private Context context;
        private int theme;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder(Context context, int theme) {
            this.context = context;
            this.theme = theme;
        }

        public Builder setMember(Member member) {
            this.member = member;
            return this;
        }

        public Builder show() {
            MemberInfoDialog dialog = new MemberInfoDialog(context, theme, member);
            dialog.show();
            return this;
        }
    }
}
