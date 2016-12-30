package com.rozdoum.socialcomponents.utils;

import android.content.Context;
import android.text.format.DateUtils;

import com.rozdoum.socialcomponents.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Kristina on 10/17/16.
 */

public class FormatterUtil {

    public static String firebaseDBDate = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static String firebaseDBDay = "yyyy-MM-dd";
    public static final long UNCOUNTABLE_TIME_LIMIT = DateUtils.MINUTE_IN_MILLIS * 5; // 5 minutes

    public static String dateTime = "yyyy-MM-dd HH:mm:ss";

    public static SimpleDateFormat getFirebaseDateFormat() {
        SimpleDateFormat cbDateFormat = new SimpleDateFormat(firebaseDBDate);
        cbDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return cbDateFormat;
    }

    public static String formatFirebaseDay(Date date) {
        SimpleDateFormat cbDateFormat = new SimpleDateFormat(firebaseDBDay);
        cbDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return cbDateFormat.format(date);
    }

    public static String formatDateTime(Date date) {
        SimpleDateFormat cbDateFormat = new SimpleDateFormat(dateTime);
        cbDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return cbDateFormat.format(date);
    }

    public static CharSequence getRelativeTimeSpanString(Context context, long time) {
        long now = System.currentTimeMillis();
        long range = Math.abs(now - time);

        if (range < UNCOUNTABLE_TIME_LIMIT) {
            return context.getString(R.string.uncountable_time_label);
        }

        return DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);
    }
}
