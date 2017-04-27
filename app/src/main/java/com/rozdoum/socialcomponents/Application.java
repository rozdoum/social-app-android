/*
 * Copyright 2017 Rozdoum
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.rozdoum.socialcomponents;

import com.crashlytics.android.Crashlytics;
import com.rozdoum.socialcomponents.managers.DatabaseHelper;

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
        DatabaseHelper.getInstance(this).subscribeToNewPosts();
    }
}
