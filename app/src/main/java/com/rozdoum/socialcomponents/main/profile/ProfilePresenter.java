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

package com.rozdoum.socialcomponents.main.profile;

import android.app.Activity;

import com.google.firebase.auth.FirebaseAuth;
import com.rozdoum.socialcomponents.enums.FollowState;
import com.rozdoum.socialcomponents.main.base.BasePresenter;
import com.rozdoum.socialcomponents.main.base.BaseView;
import com.rozdoum.socialcomponents.managers.FollowManager;
import com.rozdoum.socialcomponents.utils.LogUtil;
import com.rozdoum.socialcomponents.views.FollowButton;

/**
 * Created by Alexey on 03.05.18.
 */

class ProfilePresenter extends BasePresenter<ProfileView> {

    private final FollowManager followManager;
    private Activity activity;

    ProfilePresenter(Activity activity) {
        super(activity);
        this.activity = activity;

        followManager = FollowManager.getInstance(context);
    }

    private String getCurrentUserId() {
        return FirebaseAuth.getInstance().getUid();
    }

    private void followUser(String targetUserId) {
        ifViewAttached(BaseView::showProgress);
        followManager.followUser(activity, getCurrentUserId(), targetUserId, success -> {
            ifViewAttached(view -> {
                if (success) {
                    view.setFollowStateChangeResultOk();
                    checkFollowState(targetUserId);
                } else {
                    LogUtil.logDebug(TAG, "followUser, success: " + false);
                }
            });
        });
    }

    public void unfollowUser(String targetUserId) {
        ifViewAttached(BaseView::showProgress);
        followManager.unfollowUser(activity, getCurrentUserId(), targetUserId, success ->
                ifViewAttached(view -> {
                    if (success) {
                        view.setFollowStateChangeResultOk();
                        checkFollowState(targetUserId);
                    } else {
                        LogUtil.logDebug(TAG, "unfollowUser, success: " + false);
                    }
                }));
    }

    public void onFollowButtonClick(int state, String targetUserId) {
        if (checkInternetConnection() && checkAuthorization()) {
            if (state == FollowButton.FOLLOW_STATE || state == FollowButton.FOLLOW_BACK_STATE) {
                followUser(targetUserId);
            } else if (state == FollowButton.FOLLOWING_STATE) {
                ifViewAttached(ProfileView::showUnfollowConfirmation);
            }
        }
    }

    public void checkFollowState(String targetUserId) {
        String currentUserId = getCurrentUserId();

        if(currentUserId != null) {
            if (!targetUserId.equals(currentUserId)) {
                followManager.checkFollowState(currentUserId, targetUserId, followState -> {
                    ifViewAttached(view -> {
                        view.hideProgress();
                        view.updateFollowButtonState(followState);
                    });
                });
            }
        } else {
            ifViewAttached(view -> view.updateFollowButtonState(FollowState.NO_ONE_FOLLOW));
        }
    }

    public void getFollowersCount(String targetUserId) {
        followManager.getFollowersCount(context, targetUserId, count -> {
            ifViewAttached(view -> view.updateFollowersCount((int) count));
        });
    }

    public void getFollowingsCount(String targetUserId) {
        followManager.getFollowingsCount(context, targetUserId, count -> {
            ifViewAttached(view -> view.updateFollowingsCount((int) count));
        });
    }
}
