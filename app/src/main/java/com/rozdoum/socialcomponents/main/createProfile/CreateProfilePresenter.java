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

package com.rozdoum.socialcomponents.main.createProfile;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.main.pickImageBase.PickImagePresenter;
import com.rozdoum.socialcomponents.managers.ProfileManager;
import com.rozdoum.socialcomponents.model.Profile;
import com.rozdoum.socialcomponents.utils.PreferencesUtil;
import com.rozdoum.socialcomponents.utils.ValidationUtil;

/**
 * Created by Alexey on 03.05.18.
 */

class CreateProfilePresenter extends PickImagePresenter<CreateProfileView> {

    private Profile profile;
    private ProfileManager profileManager;

    CreateProfilePresenter(Context context) {
        super(context);
        profileManager = ProfileManager.getInstance(context.getApplicationContext());
    }

    public void buildProfile(String largeAvatarURL) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        profile = profileManager.buildProfile(firebaseUser, largeAvatarURL);

        ifViewAttached(view -> {
            view.setName(profile.getUsername());

            if (profile.getPhotoUrl() != null) {
                view.setProfilePhoto(profile.getPhotoUrl());
            } else {
                view.hideLocalProgress();
                view.setDefaultProfilePhoto();
            }
        });
    }

    public void attemptCreateProfile(Uri imageUri) {
        if (checkInternetConnection()) {
            ifViewAttached(view -> {
                view.setNameError(null);

                String name = view.getNameText().trim();
                boolean cancel = false;

                if (TextUtils.isEmpty(name)) {
                    view.setNameError(context.getString(R.string.error_field_required));
                    cancel = true;
                } else if (!ValidationUtil.isNameValid(name)) {
                    view.setNameError(context.getString(R.string.error_profile_name_length));
                    cancel = true;
                }

                if (!cancel) {
                    view.showProgress();
                    profile.setUsername(name);
                    createProfile(imageUri);
                }
            });
        }
    }

    private void createProfile(Uri imageUri) {
        profileManager.createOrUpdateProfile(profile, imageUri, success -> {
            ifViewAttached(view -> {
                view.hideProgress();
                if (success) {
                    view.finish();
                    PreferencesUtil.setProfileCreated(context, success);
                    profileManager.addRegistrationToken(FirebaseInstanceId.getInstance().getToken(), profile.getId());
                } else {
                    view.showSnackBar(R.string.error_fail_create_profile);
                }
            });
        });
    }
}
