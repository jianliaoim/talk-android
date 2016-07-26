package com.teambition.talk.entity;

import org.parceler.Parcel;

/**
 * Created by michael on 6/19/15.
 */
@Parcel
public class CountryModel {
    public static final String COUNTRY_CODE_CN = "CN";
    public static final String COUNTRY_CODE_HK = "HK";
    public static final String COUNTRY_CODE_TW = "TW";
    public static final String COUNTRY_CODE_US = "US";
    public static final String COUNTRY_CODE_JP = "JP";

    public String countryName = "";
    public int callingCode;

    public String groupName = "";
    public String pinyin = "";
}
