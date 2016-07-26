package com.teambition.talk.realm;

import android.os.Parcel;

import com.teambition.talk.entity.Member;

import org.parceler.Parcels;

/**
 * Created by wlanjie on 15/10/14.
 */
public class MemberListParcelConverter extends RealmListParcelConverter<Member> {

    @Override
    public void itemToParcel(Member input, Parcel parcel) {
        parcel.writeParcelable(Parcels.wrap(input), 0);
    }

    @Override
    public Member itemFromParcel(Parcel parcel) {
        return Parcels.unwrap(parcel.readParcelable(Member.class.getClassLoader()));
    }
}
