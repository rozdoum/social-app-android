package com.rozdoum.socialcomponents;

/**
 * Created by Kristina on 10/28/16.
 */

public class Application extends android.app.Application {

    public static final String TAG = Application.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        ApplicationHelper.initDatabaseHelper(this);
    }
}
