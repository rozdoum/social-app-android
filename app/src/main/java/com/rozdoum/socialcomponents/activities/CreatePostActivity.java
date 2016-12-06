package com.rozdoum.socialcomponents.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ChosenImages;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.dialogs.ChoseWayLoadImageDialog;
import com.rozdoum.socialcomponents.enums.TakePictureMenu;
import com.rozdoum.socialcomponents.managers.PostManager;
import com.rozdoum.socialcomponents.managers.listeners.OnPostCreatedListener;
import com.rozdoum.socialcomponents.model.Post;
import com.rozdoum.socialcomponents.utils.LogUtil;
import com.rozdoum.socialcomponents.utils.ValidationUtil;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;

public class CreatePostActivity extends BaseActivity implements OnPostCreatedListener, ChoseWayLoadImageDialog.OnChooseWayLoadImageListener {

    private static final String TAG = CreatePostActivity.class.getSimpleName();

    private static final int HANDLE_CAMERA_PERM = 2;
    private static final int WRITE_EXTERNAL_STORAGE_PERM = 3;

    private static final String SAVED_STATE_ACTIVITY_RESULT_OVER = "MainActivity.activity_result_over";
    private static final String SAVED_STATE_CHOOSER_TYPE = "MainActivity.chooserType";
    private static final String SAVED_STATE_FILE_PATH = "MainActivity.filePath";

    public static final int MAX_FILE_SIZE_IN_BYTES = 10485760;   //10 Mb

    private ImageView imageView;
    private int chooserType;

    private ImageChooserManager imageChooserManager;

    private ProgressBar progressBar;
    private String filePath;

    private EditText titleEditText;
    private EditText descriptionEditText;

    private boolean isActivityResultOver = false;

    private PostManager postManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_post_activity);
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        postManager = PostManager.getInstance(CreatePostActivity.this);

        titleEditText = (EditText) findViewById(R.id.titleEditText);
        descriptionEditText = (EditText) findViewById(R.id.descriptionEditText);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        imageView = (ImageView) findViewById(R.id.imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChoseWayLoadImageDialog chooserDialog = new ChoseWayLoadImageDialog();
                chooserDialog.show(getFragmentManager(), TAG);
            }
        });
    }

    private void attemptCreatePost() {
        int warningMessageRes = 0;
        String title = titleEditText.getText().toString();
        String description = descriptionEditText.getText().toString();

        if (filePath == null || !(new File(filePath).exists())) {
            warningMessageRes = R.string.warning_empty_image;
        } else if (TextUtils.isEmpty(title)) {
            warningMessageRes = R.string.warning_empty_title;
        } else if (TextUtils.isEmpty(description)) {
            warningMessageRes = R.string.warning_empty_description;
        }

        if (warningMessageRes == 0) {
            showProgress(R.string.message_creating_post);
            hideKeyboard();

            Post post = new Post();
            post.setTitle(title);
            post.setDescription(description);
            postManager.createPostWithImage(filePath, CreatePostActivity.this, post);
        } else {
            showWarningDialog(warningMessageRes);
        }
    }

    private void chooseImage() {
        chooserType = ChooserType.REQUEST_PICK_PICTURE;
        imageChooserManager = new ImageChooserManager(this,
                ChooserType.REQUEST_PICK_PICTURE, true);
        Bundle bundle = new Bundle();
        imageChooserManager.setExtras(bundle);
        imageChooserManager.setImageChooserListener(createImageChooserListener());
        imageChooserManager.clearOldFiles();
        try {
            filePath = imageChooserManager.choose();
        } catch (Exception e) {
            LogUtil.logError(TAG, "chooseImage()", e);
        }
    }

    private boolean hasPermissionForTakePhoto() {
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        return rc == PackageManager.PERMISSION_GRANTED && hasStoragePermission();
    }

    private boolean hasStoragePermission() {
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return rc == PackageManager.PERMISSION_GRANTED;
    }

    private void requestWriteExternalStoragePermission() {
        LogUtil.logDebug(TAG, "WriteExternalStoragePermission permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        final Activity thisActivity = this;
        ActivityCompat.requestPermissions(thisActivity, permissions,
                WRITE_EXTERNAL_STORAGE_PERM);
    }

    private void requestCameraPermission() {
        LogUtil.logDebug(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(this, permissions,
                HANDLE_CAMERA_PERM);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case WRITE_EXTERNAL_STORAGE_PERM: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    chooseImage();
                }
                break;
            }

            case HANDLE_CAMERA_PERM: {
                if (grantResults.length == permissions.length) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                    }

                    takePicture();
                }
                break;
            }
        }
    }

    private void takePicture() {
        chooserType = ChooserType.REQUEST_CAPTURE_PICTURE;
        imageChooserManager = new ImageChooserManager(this,
                ChooserType.REQUEST_CAPTURE_PICTURE, true);
        imageChooserManager.setImageChooserListener(createImageChooserListener());
        try {
            filePath = imageChooserManager.choose();
        } catch (Exception e) {
            LogUtil.logError(TAG, "takePicture()", e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtil.logDebug(TAG, "OnActivityResult");
        LogUtil.logDebug(TAG, "File Path : " + filePath);
        if (resultCode == RESULT_OK
                && (requestCode == ChooserType.REQUEST_PICK_PICTURE
                || requestCode == ChooserType.REQUEST_CAPTURE_PICTURE)) {
            if (imageChooserManager == null) {
                reinitializeImageChooser();
            }
            progressBar.setVisibility(View.VISIBLE);
            imageChooserManager.submit(requestCode, data);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private ImageChooserListener createImageChooserListener() {

        return new ImageChooserListener() {
            @Override
            public void onImageChosen(final ChosenImage image) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (image != null) {
                            LogUtil.logDebug(TAG, "Chosen Image: O - " + image.getFilePathOriginal());
                            isActivityResultOver = true;
                            filePath = image.getFilePathOriginal();


                            LogUtil.logDebug(TAG, "Chosen Image: Is not null");
                            loadImage(imageView, filePath);
                        } else {
                            LogUtil.logDebug(TAG, "Chosen Image: Is null");
                        }
                    }
                });
            }

            @Override
            public void onError(final String reason) {
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        LogUtil.logDebug(TAG, "OnError: " + reason);
                        progressBar.setVisibility(View.GONE);

                        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                                R.string.error_fail_load_image, Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                });
            }

            @Override
            public void onImagesChosen(ChosenImages chosenImages) {

            }
        };
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

    private void loadImage(ImageView iv, final String path) {
        if (isImageFileValid(path)) {
            Picasso.with(CreatePostActivity.this)
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

    // Should be called if for some reason the ImageChooserManager is null (Due
    // to destroying of activity for low memory situations)
    private void reinitializeImageChooser() {
        imageChooserManager = new ImageChooserManager(this, chooserType, true);
        Bundle bundle = new Bundle();
        imageChooserManager.setExtras(bundle);
        imageChooserManager.setImageChooserListener(createImageChooserListener());
        imageChooserManager.reinitialize(filePath);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(SAVED_STATE_ACTIVITY_RESULT_OVER, isActivityResultOver);
        outState.putInt(SAVED_STATE_CHOOSER_TYPE, chooserType);
        outState.putString(SAVED_STATE_FILE_PATH, filePath);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SAVED_STATE_CHOOSER_TYPE)) {
                chooserType = savedInstanceState.getInt(SAVED_STATE_CHOOSER_TYPE);
            }
            if (savedInstanceState.containsKey(SAVED_STATE_FILE_PATH)) {
                filePath = savedInstanceState.getString(SAVED_STATE_FILE_PATH);
            }

            if (savedInstanceState.containsKey(SAVED_STATE_ACTIVITY_RESULT_OVER)) {
                isActivityResultOver = savedInstanceState.getBoolean(SAVED_STATE_ACTIVITY_RESULT_OVER);
            }
        }

        if (isActivityResultOver) {
            populateData();
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void populateData() {
        LogUtil.logDebug(TAG, "Populating Data");
        loadImage(imageView, filePath);
    }

    @Override
    public void onPostCreated(boolean success) {
        hideProgress();
        Snackbar snackbar;

        if (success) {
            snackbar = Snackbar
                    .make(findViewById(android.R.id.content), R.string.message_post_was_created, Snackbar.LENGTH_LONG)
                    .setCallback(new Snackbar.Callback() {
                        @Override
                        public void onDismissed(Snackbar snackbar, int event) {
                            super.onDismissed(snackbar, event);
                            CreatePostActivity.this.finish();
                        }
                    });

        } else {
            snackbar = Snackbar.make(findViewById(android.R.id.content),
                    R.string.error_fail_create_post, Snackbar.LENGTH_LONG);
        }

        snackbar.show();
    }

    @Override
    public void onChooseWayLoadImage(TakePictureMenu choice) {
        switch (choice) {
            case TAKE_PHOTO:
                if (hasPermissionForTakePhoto()) {
                    takePicture();
                } else {
                    requestCameraPermission();
                }
                break;
            case CHOOSE_PHOTO:
                if (hasStoragePermission()) {
                    chooseImage();
                } else {
                    requestWriteExternalStoragePermission();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.create_post_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.post:
                attemptCreatePost();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
