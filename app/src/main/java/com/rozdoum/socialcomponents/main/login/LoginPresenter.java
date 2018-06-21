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

package com.rozdoum.socialcomponents.main.login;

import android.content.Context;
import android.net.Uri;

import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.iid.FirebaseInstanceId;
import com.rozdoum.socialcomponents.Constants;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.main.base.BasePresenter;
import com.rozdoum.socialcomponents.main.interactors.ProfileInteractor;
import com.rozdoum.socialcomponents.managers.ProfileManager;
import com.rozdoum.socialcomponents.utils.LogUtil;
import com.rozdoum.socialcomponents.utils.PreferencesUtil;
import com.google.android.gms.tasks.Task;

/**
 * Created by Alexey on 03.05.18.
 */

class LoginPresenter extends BasePresenter<LoginView> {

    LoginPresenter(Context context) {
        super(context);
    }

    public void checkIsProfileExist(final String userId) {
        ProfileManager.getInstance(context).isProfileExist(userId, exist -> {
            ifViewAttached(view -> {
                if (!exist) {
                    view.startCreateProfileActivity();
                } else {
                    PreferencesUtil.setProfileCreated(context, true);
                    ProfileInteractor.getInstance(context.getApplicationContext())
                            .addRegistrationToken(FirebaseInstanceId.getInstance().getToken(), userId);
                }

                view.hideProgress();
                view.finish();
            });
        });
    }

    public void onGoogleSignInClick() {
        if (checkInternetConnection()) {
            ifViewAttached(LoginView::signInWithGoogle);
        }
    }

    public void onFacebookSignInClick() {
        if (checkInternetConnection()) {
            ifViewAttached(LoginView::signInWithFacebook);
        }
    }

    public void handleGoogleSignInResult(GoogleSignInResult result) {
        ifViewAttached(view -> {
            if (result.isSuccess()) {
                view.showProgress();

                GoogleSignInAccount account = result.getSignInAccount();

                view.setProfilePhotoUrl(buildGooglePhotoUrl(account.getPhotoUrl()));

                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                view.firebaseAuthWithCredentials(credential);

                LogUtil.logDebug(TAG, "firebaseAuthWithGoogle:" + account.getId());

            } else {
                LogUtil.logDebug(TAG, "SIGN_IN_GOOGLE failed :" + result);
                view.hideProgress();
            }
        });
    }

    public void handleFacebookSignInResult(LoginResult loginResult) {
        ifViewAttached(view -> {
            LogUtil.logDebug(TAG, "handleFacebookSignInResult: " + loginResult);
            view.setProfilePhotoUrl(buildFacebookPhotoUrl(loginResult.getAccessToken().getUserId()));
            view.showProgress();
            AuthCredential credential = FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());
            view.firebaseAuthWithCredentials(credential);
        });
    }

    private String buildGooglePhotoUrl(Uri photoUrl) {
        return String.format(context.getString(R.string.google_large_image_url_pattern),
                photoUrl, Constants.Profile.MAX_AVATAR_SIZE);
    }

    private String buildFacebookPhotoUrl(String userId) {
        return String.format(context.getString(R.string.facebook_large_image_url_pattern),
                userId);
    }

    public void handleAuthError(Task<AuthResult> task) {
        Exception exception = task.getException();
        LogUtil.logError(TAG, "signInWithCredential", exception);

        ifViewAttached(view -> {
            if (exception != null) {
                view.showWarningDialog(exception.getMessage());
            } else {
                view.showSnackBar(R.string.error_authentication);
            }

            view.hideProgress();
        });
    }
}
