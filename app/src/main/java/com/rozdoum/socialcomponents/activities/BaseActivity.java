package com.rozdoum.socialcomponents.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rozdoum.socialcomponents.enums.ProfileStatus;
import com.rozdoum.socialcomponents.managers.ProfileManager;
import com.rozdoum.socialcomponents.model.Profile;

/**
 * Created by alexey on 05.12.16.
 */

public class BaseActivity extends AppCompatActivity {

    public void doAuthorization(ProfileStatus status) {
        if (status.equals(ProfileStatus.NOT_AUTHORIZED)) {
            startLoginActivity();
        } else if (status.equals(ProfileStatus.NO_PROFILE)) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            Profile profile = ProfileManager.getInstance(this).buildProfile(user, null);
            startCreateProfileActivity(profile);
        }
    }

    private void startCreateProfileActivity(Profile profile) {
        Intent intent = new Intent(this, CreateProfileActivity.class);
        intent.putExtra(CreateProfileActivity.PROFILE_EXTRA_KEY, profile);
        startActivity(intent);
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
