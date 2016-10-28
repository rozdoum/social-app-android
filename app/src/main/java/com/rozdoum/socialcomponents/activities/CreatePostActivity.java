package com.rozdoum.socialcomponents.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.kbeanie.imagechooser.api.ChooserType;
import com.kbeanie.imagechooser.api.ChosenImage;
import com.kbeanie.imagechooser.api.ChosenImages;
import com.kbeanie.imagechooser.api.ImageChooserListener;
import com.kbeanie.imagechooser.api.ImageChooserManager;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.utils.LogUtil;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;

public class CreatePostActivity extends AppCompatActivity {

    private static final String TAG = CreatePostActivity.class.getSimpleName();

    private static final String SAVED_STATE_ACTIVITY_RESULT_OVER = "MainActivity.activity_result_over";
    private static final String SAVED_STATE_CHOOSER_TYPE = "MainActivity.chooserType";
    private static final String SAVED_STATE_FILE_PATH = "MainActivity.filePath";
    private static final String SAVED_STATE_ORIGIN_FILE_PASS = "MainActivity.originalFilePath";

    private ImageView imageView;
    private int chooserType;
    private String originalFilePath;

    private ImageChooserManager imageChooserManager;

    private ProgressBar progressBar;
    private String filePath;

    private boolean isActivityResultOver = false;

    private String UPLOAD_URL = "http://simplifiedcoding.16mb.com/VolleyUpload/upload.php";

    private String KEY_IMAGE = "image";
    private String KEY_NAME = "name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_post_activity);

        Button chooseImageButton = (Button) findViewById(R.id.chooseImageButton);
        Button takePhotoButton = (Button) findViewById(R.id.takePhotoButton);
        Button buttonUpload = (Button) findViewById(R.id.buttonUpload);

        EditText titleEditText = (EditText) findViewById(R.id.titleEditText);
        EditText descriptionEditText = (EditText) findViewById(R.id.descriptionEditText);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        imageView = (ImageView) findViewById(R.id.imageView);

        chooseImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
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
            progressBar.setVisibility(View.VISIBLE);
            filePath = imageChooserManager.choose();
        } catch (Exception e) {
            LogUtil.logError(TAG, "chooseImage()", e);
        }
    }

    private void takePicture() {
        chooserType = ChooserType.REQUEST_CAPTURE_PICTURE;
        imageChooserManager = new ImageChooserManager(this,
                ChooserType.REQUEST_CAPTURE_PICTURE, true);
        imageChooserManager.setImageChooserListener(createImageChooserListener());
        try {
            progressBar.setVisibility(View.VISIBLE);
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
                            originalFilePath = image.getFilePathOriginal();
                            progressBar.setVisibility(View.GONE);

                            LogUtil.logDebug(TAG, "Chosen Image: Is not null");
                            loadImage(imageView, image.getFilePathOriginal());
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
                        Toast.makeText(CreatePostActivity.this, reason,
                                Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onImagesChosen(ChosenImages chosenImages) {

            }
        };
    }

    private void loadImage(ImageView iv, final String path) {
        Picasso.with(CreatePostActivity.this)
                .load(Uri.fromFile(new File(path)))
                .fit()
                .centerInside()
                .into(iv, new Callback() {
                    @Override
                    public void onSuccess() {
                        LogUtil.logDebug(TAG, "Picasso Success Loading image - " + path);
                    }

                    @Override
                    public void onError() {
                        LogUtil.logDebug(TAG, "Picasso Error Loading image - " + path);
                    }
                });
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
        outState.putString(SAVED_STATE_ORIGIN_FILE_PASS, originalFilePath);
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
                originalFilePath = savedInstanceState.getString(SAVED_STATE_ORIGIN_FILE_PASS);
            }
        }

        if (isActivityResultOver) {
            populateData();
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void populateData() {
        LogUtil.logDebug(TAG, "Populating Data");
        loadImage(imageView, originalFilePath);
    }
}
