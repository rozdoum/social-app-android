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

package com.rozdoum.socialcomponents.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.SimpleTarget;
import com.rozdoum.socialcomponents.R;
import com.rozdoum.socialcomponents.enums.UploadImagePrefix;

import java.util.Date;


public class ImageUtil {

    public static final String TAG = ImageUtil.class.getSimpleName();

    public static String generateImageTitle(UploadImagePrefix prefix, String parentId) {
        if (parentId != null) {
            return prefix.toString() + parentId;
        }

        return prefix.toString() + new Date().getTime();
    }

    public static void loadImage(GlideRequests glideRequests, String url, ImageView imageView) {
        loadImage(glideRequests, url, imageView, DiskCacheStrategy.ALL);
    }

    public static void loadImage(GlideRequests glideRequests, String url, ImageView imageView, DiskCacheStrategy diskCacheStrategy) {
        glideRequests.load(url)
                .diskCacheStrategy(diskCacheStrategy)
                .error(R.drawable.ic_stub)
                .into(imageView);
    }

    public static void loadImage(GlideRequests glideRequests, String url, ImageView imageView,
                                 RequestListener<Drawable> listener) {
        glideRequests.load(url)
                .error(R.drawable.ic_stub)
                .listener(listener)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(imageView);
    }

    public static void loadImageCenterCrop(GlideRequests glideRequests, String url, ImageView imageView,
                                           int width, int height) {
        glideRequests.load(url)
                .centerCrop()
                .override(width, height)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_stub)
                .into(imageView);
    }

    public static void loadImageCenterCrop(GlideRequests glideRequests, String url, ImageView imageView,
                                           int width, int height, RequestListener<Drawable> listener) {
        glideRequests.load(url)
                .centerCrop()
                .override(width, height)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_stub)
                .listener(listener)
                .into(imageView);
    }

    public static void loadImageCenterCrop(GlideRequests glideRequests, String url, ImageView imageView,
                                           RequestListener<Drawable> listener) {
        glideRequests.load(url)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .error(R.drawable.ic_stub)
                .listener(listener)
                .into(imageView);
    }


    @Nullable
    public static Bitmap loadBitmap(GlideRequests glideRequests, String url, int width, int height) {
        try {
            return glideRequests.asBitmap()
                    .load(url)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .submit(width, height)
                    .get();
        } catch (Exception e) {
            LogUtil.logError(TAG, "getBitmapfromUrl", e);
            return null;
        }
    }

    public static void loadImageWithSimpleTarget(GlideRequests glideRequests, String url, SimpleTarget<Bitmap> simpleTarget) {
        glideRequests.asBitmap()
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter()
                .into(simpleTarget);
    }

    public static void loadLocalImage(GlideRequests glideRequests, Uri uri, ImageView imageView,
                                      RequestListener<Drawable> listener) {
        glideRequests.load(uri)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .fitCenter()
                .listener(listener)
                .into(imageView);
    }
}
