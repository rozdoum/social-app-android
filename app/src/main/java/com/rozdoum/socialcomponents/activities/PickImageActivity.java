package com.rozdoum.socialcomponents.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.rozdoum.socialcomponents.Constants;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.utils.LogUtil;
import com.rozdoum.socialcomponents.utils.ValidationUtil;
import com.squareup.picasso.Callback;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;

public abstract class PickImageActivity extends BaseActivity {
    private static final String TAG = PickImageActivity.class.getSimpleName();
    protected static final int MAX_FILE_SIZE_IN_BYTES = 10485760;   //10 Mb
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
                loadImageToImageView();
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

    protected void loadImageToImageView() {
        Picasso.with(PickImageActivity.this)
                .load(imageUri)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .fit()
                .centerInside()
                .into(getImageView(), new Callback() {
                    @Override
                    public void onSuccess() {
                        LogUtil.logDebug(TAG, "Picasso Success Loading image - " + imageUri.getPath());
                        getProgressView().setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        LogUtil.logDebug(TAG, "Picasso Error Loading image - " + imageUri.getPath());
                        getProgressView().setVisibility(View.GONE);
                        getImageView().setImageResource(R.drawable.ic_stub);

                        showSnackBar(R.string.error_fail_load_image);
                    }
                });
    }

    protected boolean isImageFileValid(Uri imageUri) {
        int message = R.string.error_general;
        boolean result = false;

        if (imageUri != null) {
            if (ValidationUtil.isImage(imageUri, this)) {
                File imageFile = new File(imageUri.getPath());
                if (imageFile.length() > MAX_FILE_SIZE_IN_BYTES) {
                    message = R.string.error_bigger_file;
                } else {
                    result = true;
                }
            } else {
                message = R.string.error_incorrect_file_type;
            }
        }

        if (!result) {
            showSnackBar(message);
            getProgressView().setVisibility(View.GONE);
        }

        return result;
    }

    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result of pick image chooser
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);

            this.imageUri = imageUri;
            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            } else {
                // no permissions required or already grunted
                if (isImageFileValid(imageUri)) {
                    onImagePikedAction();
                }
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
                // required permissions granted, start crop image activity
                LogUtil.logDebug(TAG, "PICK_IMAGE_PERMISSIONS granted");
//                if (isImageFileValid(imageUri)) {
                onImagePikedAction();
//                }
            } else {
                showSnackBar(R.string.permissions_not_granted);
                LogUtil.logDebug(TAG, "PICK_IMAGE_PERMISSIONS not granted");
            }
        }
    }

    protected void handleCropImageResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                loadImageToImageView();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                LogUtil.logError(TAG, "crop image error", result.getError());
                showSnackBar(R.string.error_fail_crop_image);
            }
        }
    }

    protected void startCropImageActivity() {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setFixAspectRatio(true)
                .setMinCropResultSize(Constants.Profile.MIN_AVATAR_SIZE, Constants.Profile.MIN_AVATAR_SIZE)
                .setRequestedSize(Constants.Profile.MAX_AVATAR_SIZE, Constants.Profile.MAX_AVATAR_SIZE)
                .start(this);
    }
}

