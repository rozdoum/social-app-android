package com.rozdoum.socialcomponents;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Kristina on 10/28/16.
 */

public class Application extends android.app.Application {

    public static final String TAG = Application.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        ApplicationHelper.initDatabaseHelper(this);
    }
}
