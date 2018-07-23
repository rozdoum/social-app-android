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
import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.view.View;

import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.enums.FollowState;
import com.rozdoum.socialcomponents.enums.PostStatus;
import com.rozdoum.socialcomponents.main.base.BasePresenter;
import com.rozdoum.socialcomponents.main.base.BaseView;
import com.rozdoum.socialcomponents.main.postDetails.PostDetailsActivity;
import com.rozdoum.socialcomponents.managers.FollowManager;
import com.rozdoum.socialcomponents.managers.PostManager;
import com.rozdoum.socialcomponents.managers.ProfileManager;
import com.rozdoum.socialcomponents.managers.listeners.OnObjectChangedListenerSimple;
import com.rozdoum.socialcomponents.model.Post;
import com.rozdoum.socialcomponents.model.Profile;
import com.rozdoum.socialcomponents.utils.LogUtil;
import com.rozdoum.socialcomponents.views.FollowButton;

/**
 * Created by Alexey on 03.05.18.
 */

class ProfilePresenter extends BasePresenter<ProfileView> {

    private final FollowManager followManager;
    private Activity activity;
    private ProfileManager profileManager;

    private Profile profile;

    ProfilePresenter(Activity activity) {
        super(activity);
        this.activity = activity;
        profileManager = ProfileManager.getInstance(context);
        followManager = FollowManager.getInstance(context);
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
            } else if (state == FollowButton.FOLLOWING_STATE && profile != null) {
                ifViewAttached(view -> view.showUnfollowConfirmation(profile));
            }
        }
    }

    public void checkFollowState(String targetUserId) {
        String currentUserId = getCurrentUserId();

        if (currentUserId != null) {
            if (!targetUserId.equals(currentUserId)) {
                followManager.checkFollowState(currentUserId, targetUserId, followState -> {
                    ifViewAttached(view -> {
                        view.hideProgress();
                        view.updateFollowButtonState(followState);
                    });
                });
            } else {
                ifViewAttached(view -> view.updateFollowButtonState(FollowState.MY_PROFILE));
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

    void onPostClick(Post post, View postItemView) {
        PostManager.getInstance(context).isPostExistSingleValue(post.getId(), exist -> {
            ifViewAttached(view -> {
                if (exist) {
                    view.openPostDetailsActivity(post, postItemView);
                } else {
                    view.showSnackBar(R.string.error_post_was_removed);
                }
            });
        });
    }

    public Spannable buildCounterSpannable(String label, int value) {
        SpannableStringBuilder contentString = new SpannableStringBuilder();
        contentString.append(String.valueOf(value));
        contentString.append("\n");
        int start = contentString.length();
        contentString.append(label);
        contentString.setSpan(new TextAppearanceSpan(context, R.style.TextAppearance_Second_Light), start, contentString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return contentString;
    }

    public void onEditProfileClick() {
        if (checkInternetConnection()) {
            ifViewAttached(ProfileView::startEditProfileActivity);
        }
    }

    public void onCreatePostClick() {
        if (checkInternetConnection()) {
            ifViewAttached(ProfileView::openCreatePostActivity);
        }
    }

    public void loadProfile(Context activityContext, String userID) {
        profileManager.getProfileValue(activityContext, userID, new OnObjectChangedListenerSimple<Profile>() {
            @Override
            public void onObjectChanged(Profile obj) {
                profile = obj;
                ifViewAttached(view -> {
                    view.setProfileName(profile.getUsername());

                    if (profile.getPhotoUrl() != null) {
                        view.setProfilePhoto(profile.getPhotoUrl());
                    } else {
                        view.setDefaultProfilePhoto();
                    }

                    int likesCount = (int) profile.getLikesCount();
                    String likesLabel = context.getResources().getQuantityString(R.plurals.likes_counter_format, likesCount, likesCount);
                    view.updateLikesCounter(buildCounterSpannable(likesLabel, likesCount));
                });
            }
        });
    }

    public void onPostListChanged(int postsCount) {
        ifViewAttached(view -> {
            String postsLabel = context.getResources().getQuantityString(R.plurals.posts_counter_format, postsCount, postsCount);
            view.updatePostsCounter(buildCounterSpannable(postsLabel, postsCount));
            view.showLikeCounter(true);
            view.showPostCounter(true);
            view.hideLoadingPostsProgress();


        });
        }

    public void checkPostChanges(Intent data) {
        ifViewAttached(view -> {
            if (data != null) {
                PostStatus postStatus = (PostStatus) data.getSerializableExtra(PostDetailsActivity.POST_STATUS_EXTRA_KEY);

                if (postStatus.equals(PostStatus.REMOVED)) {
                    view.onPostRemoved();
                } else if (postStatus.equals(PostStatus.UPDATED)) {
                    view.onPostUpdated();
                }
            }
        });
    }
}
