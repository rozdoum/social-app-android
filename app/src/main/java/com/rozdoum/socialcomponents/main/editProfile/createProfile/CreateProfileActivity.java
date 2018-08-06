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

package com.rozdoum.socialcomponents.main.editProfile.createProfile;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.main.editProfile.EditProfileActivity;

public class CreateProfileActivity extends EditProfileActivity<CreateProfileView, CreateProfilePresenter> implements CreateProfileView {
    public static final String LARGE_IMAGE_URL_EXTRA_KEY = "CreateProfileActivity.LARGE_IMAGE_URL_EXTRA_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initContent() {
        String largeAvatarURL = getIntent().getStringExtra(LARGE_IMAGE_URL_EXTRA_KEY);
        presenter.buildProfile(largeAvatarURL);
    }

    @NonNull
    @Override
    public CreateProfilePresenter createPresenter() {
        if (presenter == null) {
            return new CreateProfilePresenter(this);
        }
        return presenter;
    }

    @Override
    public void setDefaultProfilePhoto() {
        imageView.setImageResource(R.drawable.ic_stub);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.continueButton:
                presenter.attemptCreateProfile(imageUri);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
