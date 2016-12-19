package com.rozdoum.socialcomponents.utils;

/**
 * Created by tymaks
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;


public class ImageUtil {

    public static final int MAX_CACHE_SIZE = 157286400;   //150 Mb
    private static final float IMPROVE_IMAGE_QUALITY_FACTOR = 1.2f;

    public static final String TAG = ImageUtil.class.getSimpleName();
    public static final int N_THREADS = 3;
    private static ImageUtil instance;
    private final RequestQueue mRequestQueue;
    private Context context;
    private ImageLoader imageLoader;

    private ImageUtil(final Context context) {
        this.context = context;

        Cache cache = new DiskBasedCache(ImagesDir.getTempImagesDir(context), MAX_CACHE_SIZE);
        Network network = new BasicNetwork(new HurlStack());
        mRequestQueue = new RequestQueue(cache, network, N_THREADS);
        mRequestQueue.start();
        imageLoader = new ImageLoader(mRequestQueue, new LruBitmapCache(LruBitmapCache.getCacheSize(context)));
    }

    public static ImageUtil getInstance(Context context) {
        if (instance == null) {
            instance = new ImageUtil(context);
        }

        return instance;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public Cache.Entry getCashedThumbnailEntry(String hash) {
        return mRequestQueue.getCache().get(hash);
    }

    private DisplayMetrics getDisplayMetrics() {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics;
    }

    public ImageLoader.ImageContainer getImageThumb(final String imageUrl, final ImageView imageView,
                                                    int defaultImageResId, final int errorImageResId) {
        return getImageThumb(imageUrl, imageView, defaultImageResId, errorImageResId, false);
    }

    public ImageLoader.ImageContainer getImageThumb(final String imageUrl, final ImageView imageView,
                                                    int defaultImageResId, final int errorImageResId, boolean rounded) {
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), defaultImageResId);
        setBitmapToImageView(imageView, bitmap, rounded);
        imageView.setImageResource(defaultImageResId);
        return getImage(imageUrl, imageView, null, errorImageResId, rounded);
    }

    public ImageLoader.ImageContainer getFullImage(final String imageUrl, final ImageView imageView,
                                                   final ProgressBar progressBarView, final int errorImageResId) {
        return getFullImage(imageUrl, imageView, progressBarView, errorImageResId, false);
    }

    public ImageLoader.ImageContainer getFullImage(final String imageUrl, final ImageView imageView,
                                                   final ProgressBar progressBarView, final int errorImageResId, boolean rounded) {

        if (progressBarView != null) {
            setProgressBarVisible(imageView, progressBarView, true);
        }

        return getImage(imageUrl, imageView, progressBarView, errorImageResId, rounded);
    }

    /* *
    * load image to disc cash and memory cash
    * set to image view scaled bitmap with max width and max height
    * */
    private ImageLoader.ImageContainer getImage(final String imageUrl, final ImageView imageView,
                                                final ProgressBar progressBarView, final int errorImageResId,
                                                boolean rounded) {
        int maxImageWidth = (int) (calcMaxWidth(imageView) * IMPROVE_IMAGE_QUALITY_FACTOR);
        int maxImageHeight = (int) (calcMaxHeight(imageView) * IMPROVE_IMAGE_QUALITY_FACTOR);

        return getImage(imageUrl, imageView, progressBarView, errorImageResId, maxImageWidth, maxImageHeight, rounded);
    }

    private ImageLoader.ImageContainer getImage(final String imageUrl, final ImageView imageView,
                                                final ProgressBar progressBarView, final int errorImageResId,
                                                int maxImageWidth, int maxImageHeight, final boolean rounded) {

        ImageLoader.ImageListener listener = new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                setBitmapToImageView(imageView, response.getBitmap(), rounded);
                if (progressBarView != null) {
                    setProgressBarVisible(imageView, progressBarView, false);
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), errorImageResId);
                setBitmapToImageView(imageView, bitmap, rounded);
                imageView.setImageResource(errorImageResId);
                if (progressBarView != null) {
                    setProgressBarVisible(imageView, progressBarView, false);
                }
                LogUtil.logError(TAG, "Failed load image " + imageUrl, error);
            }
        };

        return imageLoader.get(imageUrl, listener, maxImageWidth, maxImageHeight);
    }

    private void setBitmapToImageView(ImageView imageView, Bitmap bitmap, boolean rounded) {
        if (bitmap != null) {
            if (rounded) {
                imageView.setImageDrawable(new RoundImage(bitmap));
            } else {
                imageView.setImageBitmap(bitmap);
            }
        }
    }

    private void setProgressBarVisible(ImageView imageView, ProgressBar progressBar, boolean visible) {
        progressBar.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        imageView.setVisibility(visible ? View.INVISIBLE : View.VISIBLE);
    }

    private int calcMaxHeight(ImageView imageView) {
        int imageViewHeight = imageView.getLayoutParams().height;

        if (imageViewHeight > 0) {
            return imageViewHeight;
        } else {
            return getDisplayMetrics().heightPixels;
        }
    }

    private int calcMaxWidth(ImageView imageView) {
        int imageViewWidth = imageView.getLayoutParams().width;

        if (imageViewWidth > 0) {
            return imageViewWidth;
        } else {
            return getDisplayMetrics().widthPixels;
        }
    }

}
