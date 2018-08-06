/*
 * Copyright 2018 Rozdoum
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

package com.rozdoum.socialcomponents.main.base;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter;
import com.hannesdorfmann.mosby3.mvp.MvpView;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.enums.ProfileStatus;
import com.rozdoum.socialcomponents.managers.ProfileManager;

/**
 * Created by Alexey on 03.05.18.
 */

public class BasePresenter<T extends BaseView & MvpView> extends MvpBasePresenter<T> {

    protected String TAG = this.getClass().getSimpleName();

    protected Context context;
    private ProfileManager profileManager;

    public BasePresenter(Context context) {
        this.context = context;
        profileManager = ProfileManager.getInstance(context);
    }

    public boolean checkInternetConnection() {
        return checkInternetConnection(null);
    }

    public boolean checkInternetConnection(@Nullable View anchorView) {
        boolean hasInternetConnection = hasInternetConnection();
        if (!hasInternetConnection) {
            ifViewAttached(view -> {
                if (anchorView != null) {
                    view.showSnackBar(anchorView, R.string.internet_connection_failed);
                } else {
                    view.showSnackBar(R.string.internet_connection_failed);
                }
            });
        }

        return hasInternetConnection;
    }

    public boolean hasInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public boolean checkAuthorization(){
        ProfileStatus profileStatus = profileManager.checkProfile();
        if (profileStatus.equals(ProfileStatus.NOT_AUTHORIZED) || profileStatus.equals(ProfileStatus.NO_PROFILE)) {
            ifViewAttached(BaseView::startLoginActivity);
            return false;
        } else {
            return true;
        }
    }

    public void doAuthorization(ProfileStatus status) {
        if (status.equals(ProfileStatus.NOT_AUTHORIZED) || status.equals(ProfileStatus.NO_PROFILE)) {
            ifViewAttached(BaseView::startLoginActivity);
        }
    }

    protected String getCurrentUserId() {
        return FirebaseAuth.getInstance().getUid();
    }

}
