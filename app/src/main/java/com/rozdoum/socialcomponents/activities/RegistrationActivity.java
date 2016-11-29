package com.rozdoum.socialcomponents.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ChosenImages;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.dialogs.ChoseWayLoadImageDialog;
import com.rozdoum.socialcomponents.enums.TakePictureMenu;
import com.rozdoum.socialcomponents.utils.LogUtil;
import com.rozdoum.socialcomponents.utils.ValidationUtil;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;

public class RegistrationActivity extends AppCompatActivity {
    private static final String TAG = RegistrationActivity.class.getSimpleName();
    public static final int MAX_FILE_SIZE_IN_BYTES = 10485760;   //10 Mb
    private static final int HANDLE_CAMERA_PERM = 2;
    private static final int WRITE_EXTERNAL_STORAGE_PERM = 3;

    // UI references.
    private EditText emailEditText;
    private EditText nameEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private ImageView imageView;
    private ProgressBar progressBar;
    private ProgressDialog mProgressDialog;

    private int chooserType;
    private String filePath;
    private ImageChooserManager imageChooserManager;
    private boolean isActivityResultOver = false;

    private Uri mCropImageUri;

    private static final String SAVED_STATE_ACTIVITY_RESULT_OVER = "MainActivity.activity_result_over";
    private static final String SAVED_STATE_CHOOSER_TYPE = "MainActivity.chooserType";
    private static final String SAVED_STATE_FILE_PATH = "MainActivity.filePath";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        // Set up the login form.
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        imageView = (ImageView) findViewById(R.id.imageView);
        emailEditText = (EditText) findViewById(R.id.emailEditText);
        nameEditText = (EditText) findViewById(R.id.nameEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);
        confirmPasswordEditText = (EditText) findViewById(R.id.confirmPasswordEditText);


        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptRegistration();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegistration();
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                ChoseWayLoadImageDialog chooserDialog = new ChoseWayLoadImageDialog();
//                chooserDialog.show(getFragmentManager(), TAG);
                onSelectImageClick(v);
            }
        });
    }

    private void attemptRegistration() {

        // Reset errors.
        emailEditText.setError(null);
        nameEditText.setError(null);
        passwordEditText.setError(null);
        confirmPasswordEditText.setError(null);

        // Store values at the time of the login attempt.
        String email = emailEditText.getText().toString().trim();
        String name = nameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;


        if (TextUtils.isEmpty(email)) {
            emailEditText.setError(getString(R.string.error_field_required));
            if (!cancel) {
                focusView = emailEditText;
                cancel = true;
            }
        } else if (!ValidationUtil.isEmailValid(email)) {
            emailEditText.setError(getString(R.string.error_invalid_email));
            if (!cancel) {
                focusView = emailEditText;
                cancel = true;
            }
        }

        if (TextUtils.isEmpty(name)) {
            nameEditText.setError(getString(R.string.error_field_required));
            focusView = nameEditText;
            cancel = true;
        }

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError(getString(R.string.error_field_required));
            if (!cancel) {
                focusView = passwordEditText;
                cancel = true;
            }
        } else if (!ValidationUtil.isPasswordValid(password)) {
            passwordEditText.setError(getString(R.string.error_invalid_password));
            if (!cancel) {
                focusView = passwordEditText;
                cancel = true;
            }
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordEditText.setError(getString(R.string.error_field_required));
            if (!cancel) {
                focusView = confirmPasswordEditText;
                cancel = true;
            }
        } else if (!confirmPassword.equals(password)) {
            confirmPasswordEditText.setError(getString(R.string.error_confirm_password));
            if (!cancel) {
                focusView = confirmPasswordEditText;
                cancel = true;
            }
        }


        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgressDialog();

            // TODO: 21.11.16 do registration
        }
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public void onSelectImageClick(View view) {
        CropImage.startPickImageActivity(this);
    }

    @Override
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result of pick image chooser
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);

            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                mCropImageUri = imageUri;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},   CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            } else {
                // no permissions required or already grunted, can start crop image activity
                startCropImageActivity(imageUri);
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                filePath = result.getUri().getPath();
                loadImage(imageView, filePath);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

        private void loadImage(ImageView iv, final String path) {
        if (isImageFileValid(path)) {
            Picasso.with(RegistrationActivity.this)
                    .load(Uri.fromFile(new File(path)))
                    .fit()
                    .centerInside()
                    .into(iv, new Callback() {
                        @Override
                        public void onSuccess() {
                            LogUtil.logDebug(TAG, "Picasso Success Loading image - " + path);
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            LogUtil.logDebug(TAG, "Picasso Error Loading image - " + path);
                            progressBar.setVisibility(View.GONE);
                            imageView.setImageResource(R.drawable.ic_stub);

                            showSnackBar(R.string.error_fail_load_image);
                        }
                    });
        }
    }

    public boolean isImageFileValid(String filePath) {
        int message = -1;
        boolean result = false;

        if (filePath != null) {
            if (ValidationUtil.isImage(filePath)) {
                File imageFile = new File(filePath);
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
            progressBar.setVisibility(View.GONE);
        }

        return result;
    }

    private void showSnackBar(int messageId) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                messageId, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .start(this);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                startCropImageActivity(mCropImageUri);
            } else {
                Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show();
            }
        }
    }

//    private void chooseImage() {
//        chooserType = ChooserType.REQUEST_PICK_PICTURE;
//        imageChooserManager = new ImageChooserManager(this,
//                ChooserType.REQUEST_PICK_PICTURE, true);
//        Bundle bundle = new Bundle();
//        imageChooserManager.setExtras(bundle);
//        imageChooserManager.setImageChooserListener(createImageChooserListener());
//        imageChooserManager.clearOldFiles();
//        try {
//            filePath = imageChooserManager.choose();
//        } catch (Exception e) {
//            LogUtil.logError(TAG, "chooseImage()", e);
//        }
//    }
//
//    private boolean hasPermissionForTakePhoto() {
//        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
//        return rc == PackageManager.PERMISSION_GRANTED && hasStoragePermission();
//    }
//
//    private boolean hasStoragePermission() {
//        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        return rc == PackageManager.PERMISSION_GRANTED;
//    }
//
//    private void requestWriteExternalStoragePermission() {
//        LogUtil.logDebug(TAG, "WriteExternalStoragePermission permission is not granted. Requesting permission");
//
//        final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
//        final Activity thisActivity = this;
//        ActivityCompat.requestPermissions(thisActivity, permissions,
//                WRITE_EXTERNAL_STORAGE_PERM);
//    }
//
//    private void requestCameraPermission() {
//        LogUtil.logDebug(TAG, "Camera permission is not granted. Requesting permission");
//
//        final String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
//        ActivityCompat.requestPermissions(this, permissions,
//                HANDLE_CAMERA_PERM);
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case WRITE_EXTERNAL_STORAGE_PERM: {
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    chooseImage();
//                }
//                break;
//            }
//
//            case HANDLE_CAMERA_PERM: {
//                if (grantResults.length == permissions.length) {
//                    for (int i = 0; i < grantResults.length; i++) {
//                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
//                            return;
//                        }
//                    }
//
//                    takePicture();
//                }
//                break;
//            }
//        }
//    }
//
//    private void takePicture() {
//        chooserType = ChooserType.REQUEST_CAPTURE_PICTURE;
//        imageChooserManager = new ImageChooserManager(this,
//                ChooserType.REQUEST_CAPTURE_PICTURE, true);
//        imageChooserManager.setImageChooserListener(createImageChooserListener());
//        try {
//            filePath = imageChooserManager.choose();
//        } catch (Exception e) {
//            LogUtil.logError(TAG, "takePicture()", e);
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        LogUtil.logDebug(TAG, "OnActivityResult");
//        LogUtil.logDebug(TAG, "File Path : " + filePath);
//        if (resultCode == RESULT_OK
//                && (requestCode == ChooserType.REQUEST_PICK_PICTURE
//                || requestCode == ChooserType.REQUEST_CAPTURE_PICTURE)) {
//            if (imageChooserManager == null) {
//                reinitializeImageChooser();
//            }
//            progressBar.setVisibility(View.VISIBLE);
//            imageChooserManager.submit(requestCode, data);
//        } else {
//            progressBar.setVisibility(View.GONE);
//        }
//
//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//            if (resultCode == RESULT_OK) {
//                filePath = result.getUri().getPath();
//                loadImage(imageView, filePath);
//            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
//                Exception error = result.getError();
//            }
//        }
//    }
//
//    private void startCropImageActivity(Uri imageUri) {
//        CropImage.activity(imageUri)
//                .setGuidelines(CropImageView.Guidelines.ON)
//                .setFixAspectRatio(true)
//                .setMinCropResultSize(100,100)
//                .setRequestedSize(1280, 1280)
//                .start(this);
//    }
//
//    private ImageChooserListener createImageChooserListener() {
//
//        return new ImageChooserListener() {
//            @Override
//            public void onImageChosen(final ChosenImage image) {
//                runOnUiThread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        if (image != null) {
//                            LogUtil.logDebug(TAG, "Chosen Image: O - " + image.getFilePathOriginal());
//                            isActivityResultOver = true;
//                            filePath = image.getFilePathOriginal();
//                            startCropImageActivity(Uri.fromFile(new File(filePath)));
//
//                            LogUtil.logDebug(TAG, "Chosen Image: Is not null");
////                            loadImage(imageView, filePath);
//                        } else {
//                            LogUtil.logDebug(TAG, "Chosen Image: Is null");
//                        }
//                    }
//                });
//            }
//
//            @Override
//            public void onError(final String reason) {
//                runOnUiThread(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        LogUtil.logDebug(TAG, "OnError: " + reason);
//                        progressBar.setVisibility(View.GONE);
//
//                        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
//                                R.string.error_fail_load_image, Snackbar.LENGTH_LONG);
//                        snackbar.show();
//                    }
//                });
//            }
//
//            @Override
//            public void onImagesChosen(ChosenImages chosenImages) {
//
//            }
//        };
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
//    private void loadImage(ImageView iv, final String path) {
//        if (isImageFileValid(path)) {
//            Picasso.with(RegistrationActivity.this)
//                    .load(Uri.fromFile(new File(path)))
//                    .fit()
//                    .centerInside()
//                    .into(iv, new Callback() {
//                        @Override
//                        public void onSuccess() {
//                            LogUtil.logDebug(TAG, "Picasso Success Loading image - " + path);
//                            progressBar.setVisibility(View.GONE);
//                        }
//
//                        @Override
//                        public void onError() {
//                            LogUtil.logDebug(TAG, "Picasso Error Loading image - " + path);
//                            progressBar.setVisibility(View.GONE);
//                            imageView.setImageResource(R.drawable.ic_stub);
//
//                            showSnackBar(R.string.error_fail_load_image);
//                        }
//                    });
//        }
//    }
//
//    // Should be called if for some reason the ImageChooserManager is null (Due
//    // to destroying of activity for low memory situations)
//    private void reinitializeImageChooser() {
//        imageChooserManager = new ImageChooserManager(this, chooserType, true);
//        Bundle bundle = new Bundle();
//        imageChooserManager.setExtras(bundle);
//        imageChooserManager.setImageChooserListener(createImageChooserListener());
//        imageChooserManager.reinitialize(filePath);
//    }
//
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        outState.putBoolean(SAVED_STATE_ACTIVITY_RESULT_OVER, isActivityResultOver);
//        outState.putInt(SAVED_STATE_CHOOSER_TYPE, chooserType);
//        outState.putString(SAVED_STATE_FILE_PATH, filePath);
//        super.onSaveInstanceState(outState);
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        if (savedInstanceState != null) {
//            if (savedInstanceState.containsKey(SAVED_STATE_CHOOSER_TYPE)) {
//                chooserType = savedInstanceState.getInt(SAVED_STATE_CHOOSER_TYPE);
//            }
//            if (savedInstanceState.containsKey(SAVED_STATE_FILE_PATH)) {
//                filePath = savedInstanceState.getString(SAVED_STATE_FILE_PATH);
//            }
//
//            if (savedInstanceState.containsKey(SAVED_STATE_ACTIVITY_RESULT_OVER)) {
//                isActivityResultOver = savedInstanceState.getBoolean(SAVED_STATE_ACTIVITY_RESULT_OVER);
//            }
//        }
//
//        if (isActivityResultOver) {
//            populateData();
//        }
//        super.onRestoreInstanceState(savedInstanceState);
//    }
//
//    private void populateData() {
//        LogUtil.logDebug(TAG, "Populating Data");
//        loadImage(imageView, filePath);
//    }
//
//
//    private void showSnackBar(int messageId) {
//        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
//                messageId, Snackbar.LENGTH_LONG);
//        snackbar.show();
//    }
//
//    private void showWarningDialog(int messageId) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage(messageId);
//        builder.setPositiveButton(R.string.button_ok, null);
//        builder.show();
//    }
//
//    @Override
//    public void onChooseWayLoadImage(TakePictureMenu choice) {
//        switch (choice) {
//            case TAKE_PHOTO:
//                if (hasPermissionForTakePhoto()) {
//                    takePicture();
//                } else {
//                    requestCameraPermission();
//                }
//                break;
//            case CHOOSE_PHOTO:
//                if (hasStoragePermission()) {
//                    chooseImage();
//                } else {
//                    requestWriteExternalStoragePermission();
//                }
//                break;
//        }
//    }

}

