package com.rozdoum.socialcomponents.activities;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.utils.ImageUtil;
import com.rozdoum.socialcomponents.views.TouchImageView;

public class ImageDetailActivity extends BaseActivity {

    private static final String TAG = ImageDetailActivity.class.getSimpleName();

    public static final String IMAGE_URL_EXTRA_KEY = "ImageDetailActivity.IMAGE_URL_EXTRA_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        TouchImageView touchImageView = (TouchImageView) findViewById(R.id.touchImageView);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        final ViewGroup viewGroup = (ViewGroup) findViewById(R.id.image_detail_container);

        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);

            viewGroup.setOnSystemUiVisibilityChangeListener(
                    new View.OnSystemUiVisibilityChangeListener() {
                        @Override
                        public void onSystemUiVisibilityChange(int vis) {
                            if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) {
                                actionBar.hide();
                            } else {
                                actionBar.show();
                            }
                        }
                    });

            // Start low profile mode and hide ActionBar
            viewGroup.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
            actionBar.hide();
        }

        String imageUrl = getIntent().getStringExtra(IMAGE_URL_EXTRA_KEY);

        ImageUtil imageUtil = ImageUtil.getInstance(this);
        imageUtil.getImage(imageUrl, touchImageView, progressBar, R.drawable.ic_stub, R.drawable.ic_stub);

        touchImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int vis = viewGroup.getSystemUiVisibility();
                if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) {
                    viewGroup.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                } else {
                    viewGroup.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                }
            }
        });
    }
}
