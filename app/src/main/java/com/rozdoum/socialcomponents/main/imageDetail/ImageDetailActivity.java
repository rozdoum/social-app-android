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

package com.rozdoum.socialcomponents.main.imageDetail;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.main.base.BaseActivity;
import com.rozdoum.socialcomponents.utils.GlideApp;
import com.rozdoum.socialcomponents.utils.ImageUtil;
import com.rozdoum.socialcomponents.views.TouchImageView;

public class ImageDetailActivity extends BaseActivity<ImageDetailView, ImageDetailPresenter> implements ImageDetailView {

    private static final String TAG = ImageDetailActivity.class.getSimpleName();

    public static final String IMAGE_URL_EXTRA_KEY = "ImageDetailActivity.IMAGE_URL_EXTRA_KEY";
    private ViewGroup viewGroup;
    private TouchImageView touchImageView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        touchImageView = findViewById(R.id.touchImageView);
        progressBar = findViewById(R.id.progressBar);
        viewGroup = findViewById(R.id.image_detail_container);

        initActionBar();

        String imageUrl = getIntent().getStringExtra(IMAGE_URL_EXTRA_KEY);
        loadImage(imageUrl);

        touchImageView.setOnClickListener(v -> {
            final int vis = viewGroup.getSystemUiVisibility();
            if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) {
                viewGroup.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            } else {
                viewGroup.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
            }
        });
    }

    @NonNull
    @Override
    public ImageDetailPresenter createPresenter() {
        if (presenter == null) {
            return new ImageDetailPresenter(this);
        }
        return presenter;
    }

    private void initActionBar() {
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);

            viewGroup.setOnSystemUiVisibilityChangeListener(
                    vis -> {
                        if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) {
                            actionBar.hide();
                        } else {
                            actionBar.show();
                        }
                    });

            // Start low profile mode and hide ActionBar
            viewGroup.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
            actionBar.hide();
        }
    }

    private void loadImage(String imageUrl) {
        int maxImageSide = presenter.calcMaxImageSide();

        ImageUtil.loadImageWithSimpleTarget(GlideApp.with(this), imageUrl, new SimpleTarget<Bitmap>(maxImageSide, maxImageSide) {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                progressBar.setVisibility(View.GONE);
                touchImageView.setImageBitmap(resource);
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                super.onLoadFailed(errorDrawable);
                progressBar.setVisibility(View.GONE);
                touchImageView.setImageResource(R.drawable.ic_stub);
            }
        });
    }
}
