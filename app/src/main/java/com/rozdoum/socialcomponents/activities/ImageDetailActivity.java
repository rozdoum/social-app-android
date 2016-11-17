package com.rozdoum.socialcomponents.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ProgressBar;

import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.utils.ImageUtil;
import com.rozdoum.socialcomponents.views.TouchImageView;

public class ImageDetailActivity extends AppCompatActivity {

    private static final String TAG = ImageDetailActivity.class.getSimpleName();

    public static final String IMAGE_URL_EXTRA_KEY = "ImageDetailActivity.IMAGE_URL_EXTRA_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        TouchImageView touchImageView = (TouchImageView) findViewById(R.id.touchImageView);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

        String imageUrl = getIntent().getStringExtra(IMAGE_URL_EXTRA_KEY);

        ImageUtil imageUtil = ImageUtil.getInstance(this);
        imageUtil.getImage(imageUrl, touchImageView, progressBar, R.drawable.ic_stub, R.drawable.ic_stub);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
        }
        return (super.onOptionsItemSelected(menuItem));
    }
}
