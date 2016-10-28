package com.rozdoum.socialcomponents;

import com.rozdoum.socialcomponents.managers.DatabaseHelper;

/**
 * Created by Kristina on 10/28/16.
 */

public class ApplicationHelper {

    private static final String TAG = ApplicationHelper.class.getSimpleName();
    private static DatabaseHelper databaseHelper;

    public static DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

    public static void initDatabaseHelper(android.app.Application application) {
        databaseHelper = DatabaseHelper.getInstance(application);
        databaseHelper.init();
    }
}
