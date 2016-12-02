//package com.rozdoum.socialcomponents.activities;
//
//import android.Manifest;
//import android.annotation.SuppressLint;
//import android.app.Activity;
//import android.app.ProgressDialog;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.net.Uri;
//import android.os.Bundle;
//import android.support.design.widget.Snackbar;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
//import android.text.TextUtils;
//import android.view.KeyEvent;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.inputmethod.EditorInfo;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//
//import com.rozdoum.socialcomponents.R;
//import com.rozdoum.socialcomponents.model.Profile;
//import com.rozdoum.socialcomponents.utils.LogUtil;
//import com.rozdoum.socialcomponents.utils.ValidationUtil;
//import com.squareup.picasso.Callback;
//import com.squareup.picasso.Picasso;
//import com.theartofdev.edmodo.cropper.CropImage;
//import com.theartofdev.edmodo.cropper.CropImageView;
//
//import java.io.File;
//
//public class RegistrationActivity extends AppCompatActivity {
//    private static final String TAG = RegistrationActivity.class.getSimpleName();
//    public static final int MAX_FILE_SIZE_IN_BYTES = 10485760;   //10 Mb
//    private static final int MAX_AVATAR_SIZE = 1280; //px, side of square
//    private static final int MIN_AVATAR_SIZE = 100; //px, side of square
//    private static final String SAVED_STATE_IMAGE_URI = "RegistrationActivity.SAVED_STATE_IMAGE_URI";
//
//    // UI references.
//    private EditText emailEditText;
//    private EditText nameEditText;
//    private EditText passwordEditText;
//    private EditText confirmPasswordEditText;
//    private ImageView imageView;
//    private ProgressBar progressBar;
//    private ProgressDialog mProgressDialog;
//
//    private Uri imageUri;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_registration);
//        // Set up the login form.
//        progressBar = (ProgressBar) findViewById(R.id.progressBar);
//        imageView = (ImageView) findViewById(R.id.imageView);
//        emailEditText = (EditText) findViewById(R.id.emailEditText);
//        nameEditText = (EditText) findViewById(R.id.nameEditText);
//        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
//        confirmPasswordEditText = (EditText) findViewById(R.id.confirmPasswordEditText);
//
//
//        confirmPasswordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
//                if (id == R.id.login || id == EditorInfo.IME_NULL) {
//                    attemptRegistration();
//                    return true;
//                }
//                return false;
//            }
//        });
//
//        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
//        mEmailSignInButton.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                attemptRegistration();
//            }
//        });
//
//        imageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onSelectImageClick(v);
//            }
//        });
//    }
//
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        outState.putParcelable(SAVED_STATE_IMAGE_URI, imageUri);
//        super.onSaveInstanceState(outState);
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        if (savedInstanceState != null) {
//            if (savedInstanceState.containsKey(SAVED_STATE_IMAGE_URI)) {
//                imageUri = savedInstanceState.getParcelable(SAVED_STATE_IMAGE_URI);
//                loadImage(imageView, imageUri);
//            }
//        }
//
//        super.onRestoreInstanceState(savedInstanceState);
//    }
//
//    private void attemptRegistration() {
//
//        // Reset errors.
//        emailEditText.setError(null);
//        nameEditText.setError(null);
//        passwordEditText.setError(null);
//        confirmPasswordEditText.setError(null);
//
//        // Store values at the time of the login attempt.
//        String email = emailEditText.getText().toString().trim();
//        String name = nameEditText.getText().toString().trim();
//        String password = passwordEditText.getText().toString().trim();
//        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
//
//        boolean cancel = false;
//        View focusView = null;
//
//
//        if (TextUtils.isEmpty(email)) {
//            emailEditText.setError(getString(R.string.error_field_required));
//            if (!cancel) {
//                focusView = emailEditText;
//                cancel = true;
//            }
//        } else if (!ValidationUtil.isEmailValid(email)) {
//            emailEditText.setError(getString(R.string.error_invalid_email));
//            if (!cancel) {
//                focusView = emailEditText;
//                cancel = true;
//            }
//        }
//
//        if (TextUtils.isEmpty(name)) {
//            nameEditText.setError(getString(R.string.error_field_required));
//            focusView = nameEditText;
//            cancel = true;
//        }
//
//        // Check for a valid password, if the user entered one.
//        if (TextUtils.isEmpty(password)) {
//            passwordEditText.setError(getString(R.string.error_field_required));
//            if (!cancel) {
//                focusView = passwordEditText;
//                cancel = true;
//            }
//        } else if (!ValidationUtil.isPasswordValid(password)) {
//            passwordEditText.setError(getString(R.string.error_invalid_password));
//            if (!cancel) {
//                focusView = passwordEditText;
//                cancel = true;
//            }
//        }
//
//        if (TextUtils.isEmpty(confirmPassword)) {
//            confirmPasswordEditText.setError(getString(R.string.error_field_required));
//            if (!cancel) {
//                focusView = confirmPasswordEditText;
//                cancel = true;
//            }
//        } else if (!confirmPassword.equals(password)) {
//            confirmPasswordEditText.setError(getString(R.string.error_confirm_password));
//            if (!cancel) {
//                focusView = confirmPasswordEditText;
//                cancel = true;
//            }
//        }
//
//        if (imageUri == null) {
//            showWarningDialog(R.string.warning_empty_image);
//        }
//
//        if (cancel) {
//            // There was an error; don't attempt login and focus the first
//            // form field with an error.
//            focusView.requestFocus();
//        } else {
//            // Show a progress spinner, and kick off a background task to
//            // perform the user login attempt.
//            showProgressDialog();
//
//            Profile profile = new Profile();
//            profile.setEmail(email);
//            profile.setUsername(name);
//            profile
//
//
//            // TODO: 21.11.16 do registration
//        }
//    }
//
//    public void showProgressDialog() {
//        if (mProgressDialog == null) {
//            mProgressDialog = new ProgressDialog(this);
//            mProgressDialog.setMessage(getString(R.string.loading));
//            mProgressDialog.setIndeterminate(true);
//        }
//
//        mProgressDialog.show();
//    }
//
//    public void hideProgressDialog() {
//        if (mProgressDialog != null && mProgressDialog.isShowing()) {
//            mProgressDialog.dismiss();
//        }
//    }
//
//    private void showWarningDialog(int messageId) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage(messageId);
//        builder.setPositiveButton(R.string.button_ok, null);
//        builder.show();
//    }
//
//    @SuppressLint("NewApi")
//    public void onSelectImageClick(View view) {
//        if (CropImage.isExplicitCameraPermissionRequired(this)) {
//            requestPermissions(new String[]{Manifest.permission.CAMERA}, CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
//        } else {
//            CropImage.startPickImageActivity(this);
//        }
//    }
//
//    @Override
//    @SuppressLint("NewApi")
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        // handle result of pick image chooser
//        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
//            Uri imageUri = CropImage.getPickImageResultUri(this, data);
//
//            // For API >= 23 we need to check specifically that we have permissions to read external storage.
//            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
//                // request permissions and handle the result in onRequestPermissionsResult()
//                this.imageUri = imageUri;
//                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
//            } else {
//                // no permissions required or already grunted, can start crop image activity
//                startCropImageActivity(imageUri);
//            }
//        }
//
//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//            if (resultCode == RESULT_OK) {
//                imageUri = result.getUri();
//                loadImage(imageView, imageUri);
//            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
//                LogUtil.logError(TAG, "crop image error", result.getError());
//                showSnackBar(R.string.error_fail_crop_image);
//            }
//        }
//    }
//
//    private void loadImage(ImageView iv, final Uri uri) {
//        Picasso.with(RegistrationActivity.this)
//                .load(uri)
//                .fit()
//                .centerInside()
//                .into(iv, new Callback() {
//                    @Override
//                    public void onSuccess() {
//                        LogUtil.logDebug(TAG, "Picasso Success Loading image - " + uri.getPath());
//                        progressBar.setVisibility(View.GONE);
//                    }
//
//                    @Override
//                    public void onError() {
//                        LogUtil.logDebug(TAG, "Picasso Error Loading image - " + uri.getPath());
//                        progressBar.setVisibility(View.GONE);
//                        imageView.setImageResource(R.drawable.ic_stub);
//
//                        showSnackBar(R.string.error_fail_load_image);
//                    }
//                });
//    }
//
//    public boolean isImageFileValid(String filePath) {
//        int message = -1;
//        boolean result = false;
//
//        if (filePath != null) {
//            if (ValidationUtil.isImage(filePath)) {
//                File imageFile = new File(filePath);
//                if (imageFile.length() > MAX_FILE_SIZE_IN_BYTES) {
//                    message = R.string.error_bigger_file;
//                } else {
//                    result = true;
//                }
//            } else {
//                message = R.string.error_incorrect_file_type;
//            }
//        }
//
//        if (!result) {
//            showSnackBar(message);
//            progressBar.setVisibility(View.GONE);
//        }
//
//        return result;
//    }
//
//    private void showSnackBar(int messageId) {
//        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
//                messageId, Snackbar.LENGTH_LONG);
//        snackbar.show();
//    }
//
//    private void startCropImageActivity(Uri imageUri) {
//        if (isImageFileValid(imageUri.getPath())) {
//            CropImage.activity(imageUri)
//                    .setGuidelines(CropImageView.Guidelines.ON)
//                    .setFixAspectRatio(true)
//                    .setMinCropResultSize(MIN_AVATAR_SIZE, MIN_AVATAR_SIZE)
//                    .setRequestedSize(MAX_AVATAR_SIZE, MAX_AVATAR_SIZE)
//                    .start(this);
//        }
//    }
//
//    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//        if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                LogUtil.logDebug(TAG, "CAMERA_CAPTURE_PERMISSIONS granted");
//                CropImage.startPickImageActivity(this);
//            } else {
//                showSnackBar(R.string.permissions_not_granted);
//                LogUtil.logDebug(TAG, "CAMERA_CAPTURE_PERMISSIONS not granted");
//            }
//        }
//        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
//            if (imageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // required permissions granted, start crop image activity
//                LogUtil.logDebug(TAG, "PICK_IMAGE_PERMISSIONS granted");
//                startCropImageActivity(imageUri);
//            } else {
//                showSnackBar(R.string.permissions_not_granted);
//                LogUtil.logDebug(TAG, "PICK_IMAGE_PERMISSIONS not granted");
//            }
//        }
//    }
//}
//
