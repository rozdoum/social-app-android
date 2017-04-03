package com.rozdoum.socialcomponents.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesUtil {

    private static final String TAG = PreferencesUtil.class.getSimpleName();

    private static final String SHARED_PREFERENCES_NAME = "com.rozdoum.socialcomponents";
    private static final String PREF_PARAM_IS_PROFILE_CREATED = "isProfileCreated";
    private static final String PREF_PARAM_IS_POSTS_WAS_LOADED_AT_LEAST_ONCE = "isPostsWasLoadedAtLeastOnce";

    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public static Boolean isProfileCreated(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_PARAM_IS_PROFILE_CREATED, false);
    }

    public static Boolean isPostWasLoadedAtLeastOnce(Context context) {
        return getSharedPreferences(context).getBoolean(PREF_PARAM_IS_POSTS_WAS_LOADED_AT_LEAST_ONCE, false);
    }

    public static void setProfileCreated(Context context, Boolean isProfileCreated) {
        getSharedPreferences(context).edit().putBoolean(PREF_PARAM_IS_PROFILE_CREATED, isProfileCreated).commit();
    }

    public static void setPostWasLoadedAtLeastOnce(Context context, Boolean isPostWasLoadedAtLeastOnce) {
        getSharedPreferences(context).edit().putBoolean(PREF_PARAM_IS_POSTS_WAS_LOADED_AT_LEAST_ONCE, isPostWasLoadedAtLeastOnce).commit();
    }

    public static void clearPreferences(Context context){
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.clear();
        editor.apply();
    }
}
