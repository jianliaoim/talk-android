package com.teambition.talk.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.talk.dialog.TalkDialog;
import com.teambition.common.PinyinUtil;
import com.teambition.talk.BizLogic;
import com.teambition.talk.BusProvider;
import com.teambition.talk.event.NewInvitationEvent;
import com.teambition.talk.imageloader.ImageLoaderConfig;
import com.teambition.talk.MainApp;
import com.teambition.talk.R;
import com.teambition.talk.rx.ApiErrorAction;
import com.teambition.talk.rx.RealmErrorAction;
import com.teambition.talk.client.TalkClient;
import com.teambition.talk.entity.Contact;
import com.teambition.talk.entity.Invitation;
import com.teambition.talk.entity.Member;
import com.teambition.talk.event.UpdateMemberEvent;
import com.teambition.talk.realm.InvitationRealm;
import com.teambition.talk.realm.MemberDataProcess;
import com.teambition.talk.realm.MemberRealm;
import com.teambition.talk.util.StringUtil;
import com.teambition.talk.util.ThemeUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by zeatual on 15/6/26.
 */
public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {

    private List<Contact> originContacts;
    private List<Contact> contacts;
    private Context context;
    private String keyword;
    private boolean filterMode;

    public ContactsAdapter(Context context) {
        this.context = context;
        this.originContacts = new ArrayList<>();
        this.contacts = new ArrayList<>();
    }

    public void setFilterMode(boolean filterMode) {
        this.filterMode = filterMode;
    }

    public void updateData(List<Contact> contacts) {
        this.originContacts.clear();
        this.contacts.clear();
        this.originContacts.addAll(contacts);
        this.contacts.addAll(contacts);
        notifyDataSetChanged();
    }

    public void showSearchResult(Member member) {
        contacts.clear();
        Contact contact = new Contact(member.getName(), member.getPhoneForLogin(), null,
                member.getAvatarUrl(), member.get_id());
        contacts.add(contact);
        notifyDataSetChanged();
    }

    public void filter(String email) {
        keyword = email;
        contacts.clear();
        for (Contact contact : originContacts) {
            if (contact.getPhoneNum() != null && contact.getPhoneNum().contains(email)) {
                contacts.add(contact);
            } else if (contact.getEmailAddress() != null && contact.getEmailAddress().contains(email)) {
                contacts.add(contact);
            }
        }
        notifyDataSetChanged();
    }

    public void filterName(String name) {
        keyword = name;
        contacts.clear();
        for (Contact originContact : originContacts) {
            if (originContact.getName().contains(name) || PinyinUtil.converterToSpell(originContact.getName()).contains(name)) {
                contacts.add(originContact);
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ContactViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.item_contact, null));
    }

    @Override
    public void onBindViewHolder(final ContactViewHolder holder, final int position) {
        final Contact contact = contacts.get(position);

        if (!filterMode) {
            holder.key.setVisibility(View.VISIBLE);
            if (position == 0 || !contact.getIndex().equalsIgnoreCase(contacts.get(position - 1).getIndex())) {
                holder.key.setVisibility(View.VISIBLE);
                holder.key.setText(contact.getIndex());
            } else {
                holder.key.setVisibility(View.INVISIBLE);
            }
        } else {
            holder.key.setVisibility(View.INVISIBLE);
        }

        holder.name.setText(contact.getName());
        String content = null;
        if (StringUtil.isBlank(contact.getPhoneNum())) {
            content = contact.getEmailAddress();
        } else {
            content = contact.getPhoneNum();
        }
        CharSequence contentSpannable;
        if (StringUtil.isNotBlank(keyword)) {
            contentSpannable = StringUtil.getHighlightSpan(content, keyword, context.getResources());
        } else {
            contentSpannable = content;
        }
        holder.phone.setText(contentSpannable);
        if (StringUtil.isNotBlank(contact.getAvatar()) && contact.getAvatar().startsWith(ImageLoaderConfig.PREFIX_DRAWABLE)) {
            holder.avatar.setImageResource(Integer.valueOf(contact.getAvatar()
                    .substring(ImageLoaderConfig.PREFIX_DRAWABLE.length(), contact.getAvatar().length())));
            holder.firstChar.setVisibility(View.VISIBLE);
            if (StringUtil.isNotBlank(contact.getName())) {
                holder.firstChar.setText(contact.getName().substring(0, 1));
            } else {
                holder.firstChar.setText("");
            }
        } else {
            holder.firstChar.setVisibility(View.GONE);
            MainApp.IMAGE_LOADER.displayImage(contact.getAvatar(), holder.avatar,
                    ImageLoaderConfig.AVATAR_OPTIONS);
        }

        if (contact.getIsInTeam() == null) {
            Observable.create(new Observable.OnSubscribe<Boolean>() {
                @Override
                public void call(Subscriber<? super Boolean> subscriber) {
                    List<Invitation> invitations = InvitationRealm.getInstance().getInvitationWithCurrentThread();
                    for (Invitation invitation : invitations) {
                        if (invitation.getMobile() != null && invitation.getMobile().equals(contact.getPhoneNum())) {
                            subscriber.onNext(true);
                        } else if (invitation.getEmail() != null && invitation.getEmail().equals(contact.getEmailAddress())) {
                            subscriber.onNext(true);
                        }
                        return;
                    }
                    for (Member member : MainApp.globalMembers.values()) {
                        if (member.getIsQuit() != null && member.getIsQuit()) {
                            continue;
                        }
                        if (StringUtil.isNotBlank(member.getPhoneForLogin()) &&
                                contact.getPhoneNum() != null) {
                            if (contact.getPhoneNum().contains(member.getPhoneForLogin())) {
                                contact.setAvatar(member.getAvatarUrl());
                                subscriber.onNext(true);
                                return;
                            }
                        }
                    }
                    subscriber.onNext(false);
                }
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Boolean>() {
                        @Override
                        public void call(Boolean b) {
                            contact.setIsInTeam(b);
                            renderButton(contact, position, holder);
                        }
                    });
        } else {
            renderButton(contact, position, holder);
        }
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    private void renderButton(final Contact contact, final int position, final ContactViewHolder holder) {
        if (contact.getIsInTeam()) {
            holder.add.setOnClickListener(null);
            holder.add.setImageResource(R.drawable.ic_contact_in);
            if (!contact.getAvatar().startsWith(ImageLoaderConfig.PREFIX_DRAWABLE)) {
                holder.firstChar.setVisibility(View.GONE);
                MainApp.IMAGE_LOADER.displayImage(contact.getAvatar(), holder.avatar,
                        ImageLoaderConfig.AVATAR_OPTIONS);
            }
        } else {
            holder.add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Observable<List<Member>> memberStream = null;
                    if (StringUtil.isNotBlank(contact.getPhoneNum())) {
                        memberStream = TalkClient.getInstance().getTalkApi()
                                .getUserByPhone(contact.getPhoneNum());
                    }
                    if (StringUtil.isNotBlank(contact.getEmailAddress())){
                        memberStream = TalkClient.getInstance().getTalkApi()
                                .getUserByKeywords(contact.getEmailAddress());
                    }
                    if (memberStream == null) {
                        memberStream = Observable.create(new Observable.OnSubscribe<List<Member>>() {
                            @Override
                            public void call(Subscriber<? super List<Member>> subscriber) {
                                subscriber.onNext(MemberRealm.getInstance().getMemberWithCurrentThread());
                            }
                        });
                    }
                    memberStream.observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<List<Member>>() {
                                @Override
                                public void call(List<Member> members) {
                                    if (!members.isEmpty()) {
                                        Observable<Member> memberStream = null;
                                        if (StringUtil.isNotBlank(contact.getUserId())) {
                                            memberStream = TalkClient.getInstance().getTalkApi().inviteViaUserId(BizLogic.getTeamId(), contact.getUserId());
                                        }
                                        if (memberStream == null) return;
                                        memberStream.observeOn(AndroidSchedulers.mainThread())
                                                .subscribe(new Action1<Member>() {
                                                    @Override
                                                    public void call(Member member) {
                                                        MainApp.showToastMsg(R.string.add_member_success);
                                                        MemberDataProcess.getInstance().processPrefers(member);
                                                        MemberDataProcess.getInstance().processNewMember(member);
                                                        MemberRealm.getInstance().addOrUpdate(member)
                                                                .observeOn(AndroidSchedulers.mainThread())
                                                                .subscribe(new Action1<Member>() {
                                                                    @Override
                                                                    public void call(Member member) {
                                                                        MainApp.IS_MEMBER_CHANGED = true;
                                                                        BusProvider.getInstance().post(new UpdateMemberEvent());
                                                                        holder.add.setOnClickListener(null);
                                                                        holder.add.setImageResource(R.drawable.ic_contact_in);
                                                                    }
                                                                }, new RealmErrorAction());
                                                        contact.setAvatar(member.getAvatarUrl());
                                                        contact.setIsInTeam(true);
                                                        notifyItemChanged(position);
                                                    }
                                                }, new ApiErrorAction());
                                    } else {
                                        if (StringUtil.isBlank(contact.getPhoneNum())) {
                                            sendInvitationByEmail(contact.getEmailAddress());
                                        } else {
                                            sendInvitationBySms(contact.getPhoneNum());
                                        }
                                    }
                                }
                            }, new ApiErrorAction());
                }
            });
            holder.add.setImageDrawable(ThemeUtil.getThemeDrawable(context.getResources(),
                    R.drawable.ic_contact_add, BizLogic.getTeamColor()));
        }
    }

    private void sendInvitationBySms(final String phoneNumber) {
        new TalkDialog.Builder(context)
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
                                .inviteRookieViaPhone(BizLogic.getTeamId(), phoneNumber)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Invitation>() {
                                    @Override
                                    public void call(Invitation invitation) {
                                        InvitationRealm.getInstance().addOrUpdate(invitation)
                                                .subscribe(new Action1<Invitation>() {
                                                    @Override
                                                    public void call(Invitation invitation) {
                                                        MainApp.showToastMsg(R.string.invitation_sent);
                                                        BusProvider.getInstance().post(new NewInvitationEvent(invitation));
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

    private void sendInvitationByEmail(final String emailAddress) {
        new TalkDialog.Builder(context)
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
                                .inviteRookieViaEmail(BizLogic.getTeamId(), emailAddress)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Action1<Invitation>() {
                                    @Override
                                    public void call(Invitation invitation) {
                                        InvitationRealm.getInstance().addOrUpdate(invitation)
                                                .subscribe(new Action1<Invitation>() {
                                                    @Override
                                                    public void call(Invitation invitation) {
                                                        MainApp.showToastMsg(R.string.invitation_sent);
                                                        BusProvider.getInstance().post(new NewInvitationEvent(invitation));
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

    static class ContactViewHolder extends RecyclerView.ViewHolder {

        @InjectView(R.id.tv_key)
        public TextView key;
        @InjectView(R.id.tv_name)
        public TextView name;
        @InjectView(R.id.tv_char)
        public TextView firstChar;
        @InjectView(R.id.tv_phone)
        public TextView phone;
        @InjectView(R.id.img_add)
        public ImageView add;
        @InjectView(R.id.img_avatar)
        public RoundedImageView avatar;

        public ContactViewHolder(View itemView) {
            super(itemView);
            ButterKnife.inject(this, itemView);
        }
    }
}
