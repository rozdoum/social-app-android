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

package com.rozdoum.socialcomponents.main.post;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.main.pickImageBase.PickImageActivity;
import com.rozdoum.socialcomponents.managers.PostManager;

/**
 * Created by Alexey on 03.05.18.
 */
public abstract class BaseCreatePostActivity<V extends BaseCreatePostView, P extends BaseCreatePostPresenter<V>>
        extends PickImageActivity<V, P> implements BaseCreatePostView {

    protected ImageView imageView;
    protected ProgressBar progressBar;
    protected EditText titleEditText;
    protected EditText descriptionEditText;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_create_post_activity);
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        titleEditText = findViewById(R.id.titleEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        progressBar = findViewById(R.id.progressBar);

        imageView = findViewById(R.id.imageView);

        imageView.setOnClickListener(v -> onSelectImageClick(v));

        titleEditText.setOnTouchListener((v, event) -> {
            if (titleEditText.hasFocus() && titleEditText.getError() != null) {
                titleEditText.setError(null);
                return true;
            }

            return false;
        });
    }

    @Override
    protected ProgressBar getProgressView() {
        return progressBar;
    }

    @Override
    protected ImageView getImageView() {
        return imageView;
    }

    @Override
    protected void onImagePikedAction() {
        loadImageToImageView(imageUri);
    }

    @Override
    public void setDescriptionError(String error) {
        descriptionEditText.setError(error);
        descriptionEditText.requestFocus();
    }

    @Override
    public void setTitleError(String error) {
        titleEditText.setError(error);
        titleEditText.requestFocus();
    }

    @Override
    public String getTitleText() {
        return titleEditText.getText().toString();
    }

    @Override
    public String getDescriptionText() {
        return descriptionEditText.getText().toString();
    }

    @Override
    public void requestImageViewFocus() {
        imageView.requestFocus();
    }

    @Override
    public void onPostSavedSuccess() {
        setResult(RESULT_OK);
        this.finish();
    }

    @Override
    public Uri getImageUri() {
        return imageUri;
    }
}
