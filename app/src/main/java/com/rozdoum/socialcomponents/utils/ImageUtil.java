package com.rozdoum.socialcomponents.utils;

/**
 * Created by tymaks
 */

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

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
    private Cache cache;
    private LruBitmapCache lruCache;

    private ImageUtil(final Context context) {
        this.context = context;

        cache = new DiskBasedCache(ImagesDir.getTempImagesDir(context), MAX_CACHE_SIZE);
        Network network = new BasicNetwork(new HurlStack());
        mRequestQueue = new RequestQueue(cache, network, N_THREADS);
        mRequestQueue.start();
        lruCache = new LruBitmapCache(LruBitmapCache.getCacheSize(context));
        imageLoader = new ImageLoader(mRequestQueue, lruCache);
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

    public ImageLoader.ImageContainer getImageThumb(final String imageUrl, final ImageView imageView, int defaultImageResId, final int errorImageResId) {
        imageView.setImageResource(defaultImageResId);
        return getImage(imageUrl, imageView, errorImageResId);
    }

    public ImageLoader.ImageContainer getFullImage(final String imageUrl, final ImageView imageView, final int errorImageResId) {
        return getImage(imageUrl, imageView, errorImageResId);
    }

    /* *
    * load image to disc cash and memory cash
    * set to image view scaled bitmap with max width and max height
    * */
    private ImageLoader.ImageContainer getImage(final String imageUrl, final ImageView imageView, final int errorImageResId) {
        int maxImageWidth = (int) (calcMaxWidth(imageView) * IMPROVE_IMAGE_QUALITY_FACTOR);
        int maxImageHeight = (int) (calcMaxHeight(imageView) * IMPROVE_IMAGE_QUALITY_FACTOR);

        return getImage(imageUrl, imageView, errorImageResId, maxImageWidth, maxImageHeight);
    }

    private ImageLoader.ImageContainer getImage(final String imageUrl, final ImageView imageView, final int errorImageResId,
                                                int maxImageWidth, int maxImageHeight) {

        ImageLoader.ImageListener listener = new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                imageView.setImageBitmap(response.getBitmap());
                imageView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                imageView.setImageResource(errorImageResId);
                imageView.setVisibility(View.VISIBLE);
                LogUtil.logError(TAG, "Failed load image " + imageUrl, error);
            }
        };

        return imageLoader.get(imageUrl, listener, maxImageWidth, maxImageHeight);
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

    public void clearCache() {
        cache.clear();
        lruCache.evictAll();
    }

}
