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

package com.rozdoum.socialcomponents.main.pickImageBase;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.rozdoum.socialcomponents.Constants;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.main.base.BaseActivity;
import com.rozdoum.socialcomponents.utils.GlideApp;
import com.rozdoum.socialcomponents.utils.ImageUtil;
import com.rozdoum.socialcomponents.utils.LogUtil;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public abstract class PickImageActivity<V extends PickImageView, P extends PickImagePresenter<V>> extends BaseActivity<V, P> implements PickImageView {
    private static final String SAVED_STATE_IMAGE_URI = "RegistrationActivity.SAVED_STATE_IMAGE_URI";

    protected Uri imageUri;

    protected abstract ProgressBar getProgressView();

    protected abstract ImageView getImageView();

    protected abstract void onImagePikedAction();

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(SAVED_STATE_IMAGE_URI, imageUri);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SAVED_STATE_IMAGE_URI)) {
                imageUri = savedInstanceState.getParcelable(SAVED_STATE_IMAGE_URI);
                loadImageToImageView(imageUri);
            }
        }

        super.onRestoreInstanceState(savedInstanceState);
    }

    @SuppressLint("NewApi")
    public void onSelectImageClick(View view) {
        if (CropImage.isExplicitCameraPermissionRequired(this)) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
        } else {
            CropImage.startPickImageActivity(this);
        }
    }

    @Override
    public void loadImageToImageView(Uri imageUri) {
        if (imageUri == null) {
            return;
        }

        this.imageUri = imageUri;
        ImageUtil.loadLocalImage(GlideApp.with(this), imageUri, getImageView(), new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                getProgressView().setVisibility(View.GONE);
                LogUtil.logDebug(TAG, "Glide Success Loading image from uri : " + imageUri.getPath());
                return false;
            }
        });
    }

    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result of pick image chooser
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);

            if (presenter.isImageFileValid(imageUri)) {
                this.imageUri = imageUri;
            }

            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            } else {
                // no permissions required or already grunted
                onImagePikedAction();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                LogUtil.logDebug(TAG, "CAMERA_CAPTURE_PERMISSIONS granted");
                CropImage.startPickImageActivity(this);
            } else {
                showSnackBar(R.string.permissions_not_granted);
                LogUtil.logDebug(TAG, "CAMERA_CAPTURE_PERMISSIONS not granted");
            }
        }
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (imageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                LogUtil.logDebug(TAG, "PICK_IMAGE_PERMISSIONS granted");
                onImagePikedAction();
            } else {
                showSnackBar(R.string.permissions_not_granted);
                LogUtil.logDebug(TAG, "PICK_IMAGE_PERMISSIONS not granted");
            }
        }
    }

    protected void handleCropImageResult(int requestCode, int resultCode, Intent data) {
        presenter.handleCropImageResult(requestCode, resultCode, data);
    }

    protected void startCropImageActivity() {
        if (imageUri == null) {
            return;
        }

        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setFixAspectRatio(true)
                .setMinCropResultSize(Constants.Profile.MIN_AVATAR_SIZE, Constants.Profile.MIN_AVATAR_SIZE)
                .setRequestedSize(Constants.Profile.MAX_AVATAR_SIZE, Constants.Profile.MAX_AVATAR_SIZE)
                .start(this);
    }

    @Override
    public void hideLocalProgress() {
        getProgressView().setVisibility(View.GONE);
    }
}

