package com.teambition.push.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by nlmartian on 7/23/15.
 */
public class DateUtil {
    private static DateFormat iso8601Format;
    static {
        iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public synchronized static Date parseDate(String str) {
        try {
            Date date = iso8601Format.parse(str);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date(0);
        }
    }
}
