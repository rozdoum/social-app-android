package com.rozdoum.socialcomponents.utils;

/**
 * Created by tymaks
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageRequest;

import java.util.Calendar;


public class ImageUtil {

    public static final int MAX_CACHE_SIZE = 157286400;   //150 Mb
    private static final int CACHE_VALID_DAYS = 30;
    private static final float DEFAULT_TOUCH_IMAGE_ZOOM = 1f;

    private static final float IMAGE_SIZE_FACTOR_BY_SCREEN = 2f;
    private static final float IMAGE_SIZE_FACTOR_BY_VIEW = 1.5f;

    public static final String TAG = ImageUtil.class.getSimpleName();
    public static final int N_THREADS = 3;
    private static ImageUtil instance;
    private final RequestQueue mRequestQueue;
    private Context context;

    private ImageUtil(final Context context) {
        this.context = context;

        Cache cache = new DiskBasedCache(ImagesDir.getTempImagesDir(context), MAX_CACHE_SIZE);
        Network network = new BasicNetwork(new HurlStack());
        mRequestQueue = new RequestQueue(cache, network, N_THREADS);
        mRequestQueue.start();
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

    public Request downloadImage(String url, String hash,
                                 Response.ErrorListener errorListener,
                                 Response.Listener<Bitmap> listener) {
        return new DownloadImageThumbnailRequest(url, hash, errorListener, listener, false);
    }

    public Request getImageThumbnail(String url, String hash,
                                     Response.ErrorListener errorListener,
                                     Response.Listener<Bitmap> listener) {
        return new DownloadImageThumbnailRequest(url, hash, errorListener, listener, true);
    }

    public Cache.Entry getCashedThumbnailEntry(String hash) {
        return mRequestQueue.getCache().get(hash);
    }

    public Request getImage(final String imageUrl, final ImageView imageView, int defaultImageResId, final int errorImageResId) {
        return getImage(imageUrl, imageView, null, defaultImageResId, errorImageResId);
    }

    public Request getImage(final String imageUrl, final ImageView imageView, final ProgressBar progressBarView, int defaultImageResId, final int errorImageResId) {
//        imageView.setImageResource(defaultImageResId);
//        String imageUrl = String.format(HttpURL.GET_IMAGES.getStringURL(context), imageId);
        if (progressBarView != null) {
            showProgressBar(imageView, progressBarView, true);
        }

        Request request = getImageThumbnail(imageUrl, imageUrl, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        LogUtil.logError(TAG, "Failed load image " + imageUrl, error);
                        imageView.setImageResource(errorImageResId);
                    }
                },
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        LogUtil.logInfo(TAG, "Successfully loaded image " + imageUrl);
                        imageView.setImageBitmap(createScaledBitmap(response, calcMaxImageSize(imageView)));

                        if (progressBarView != null) {
                            showProgressBar(imageView, progressBarView, false);
                        }
                    }
                });
        addToRequestQueue(request);
        return request;
    }

    private void showProgressBar(ImageView imageView, ProgressBar progressBar, boolean visible) {
        progressBar.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        imageView.setVisibility(visible ? View.INVISIBLE : View.VISIBLE);
    }

//    public Request getImageForTouchImageView(final String imageId, final TouchImageView imageView,
//                                             final ProgressBar progressBar, final int errorImageResId) {
//        String imageUrl = String.format(HttpURL.GET_IMAGES.getStringURL(context), imageId);
//
//        Request request = getImageThumbnail(imageUrl, imageId, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        LogUtil.logError(TAG, "Failed load image " + imageId, error);
//                        imageView.setImageResource(errorImageResId);
//                    }
//                },
//                new Response.Listener<Bitmap>() {
//                    @Override
//                    public void onResponse(Bitmap response) {
//                        LogUtil.logInfo(TAG, "Successfully loaded image " + imageId);
//                        imageView.setImageBitmap(response);
//                        imageView.setZoom(DEFAULT_TOUCH_IMAGE_ZOOM);
//                        progressBar.setVisibility(View.GONE);
//                    }
//                });
//        addToRequestQueue(request);
//        return request;
//    }

//    public void loadImageInBackground(final String imageId, final String objectType) {
//        String imageUrl = String.format(HttpURL.GET_IMAGES.getStringURL(context), imageId);
//
//        addToRequestQueue(downloadImage(imageUrl, imageId, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        LogUtil.logError(TAG, "Failed image loading for " + objectType, error);
//
//                    }
//                },
//                new Response.Listener<Bitmap>() {
//                    @Override
//                    public void onResponse(Bitmap response) {
//                        LogUtil.logInfo(TAG, "Image loaded for " + objectType);
//                    }
//                }));
//    }

    private int calcMaxImageSize(ImageView imageView) {
        int maxImageSide;

        if (imageView.getWidth() == 0
                || (imageView.getParent() != null && ((View)imageView.getParent()).getWidth() == 0)) {
            DisplayMetrics displaymetrics = new DisplayMetrics();
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            windowManager.getDefaultDisplay().getMetrics(displaymetrics);
            int displayWidth = displaymetrics.widthPixels;

            maxImageSide = (int) (displayWidth * IMAGE_SIZE_FACTOR_BY_SCREEN);
            LogUtil.logDebug(TAG, "by screen, size: " + maxImageSide);
        } else {
            maxImageSide = (int) (imageView.getWidth() * IMAGE_SIZE_FACTOR_BY_VIEW);
            LogUtil.logDebug(TAG, "by view, size: " + maxImageSide);
        }

        return maxImageSide;
    }

    private Bitmap createScaledBitmap(Bitmap src, int dstSideLength) {
        return createScaledBitmap(src, dstSideLength, dstSideLength);
    }

    private Bitmap createScaledBitmap(Bitmap src, int dstWidth, int dstHeight) {
        float originHeight = src.getHeight();
        float originWidth = src.getWidth();

        if (originHeight > dstHeight || originWidth > dstWidth) {

            if (originHeight > dstHeight) {
                float scale = originHeight / dstHeight;
                originHeight = originHeight / scale;
                originWidth = originWidth / scale;
            }

            if (originWidth > dstWidth) {
                float scale = originWidth / dstWidth;
                originHeight = originHeight / scale;
                originWidth = originWidth / scale;
            }

            return Bitmap.createScaledBitmap(src, (int) originWidth, (int) originHeight, false);

        } else {
            return src;
        }
    }

    class DownloadImageThumbnailRequest extends Request<Bitmap> {

        private final String hash;
        private final Response.Listener<Bitmap> listener;
        private final int maxWidth = 0;
        private final int maxHeight = 0;
        private final boolean isBitmapNeeded;
        private final MyImageRequest imageRequest;
        private ImageView.ScaleType scaleType = ImageView.ScaleType.FIT_CENTER;
        private Bitmap.Config decodeConfig = Bitmap.Config.RGB_565;

        public DownloadImageThumbnailRequest(String url, String hash,
                                             Response.ErrorListener errorListener,
                                             Response.Listener<Bitmap> listener, boolean isBitmapNeeded) {
            super(Request.Method.GET, url, errorListener);
            this.hash = hash;
            this.listener = listener;
            this.isBitmapNeeded = isBitmapNeeded;

            imageRequest = new MyImageRequest(url, listener, errorListener);
        }

        @Override
        protected Response<Bitmap> parseNetworkResponse(NetworkResponse response) {
            if (isBitmapNeeded) {
                return imageRequest.parseNetworkResponse(response);
            } else {
                if (response.data == null || response.data.length == 0) {
                    return Response.error(new ParseError(response));
                } else {
                    return Response.success(null, createCashEntry(response));
                }
            }
        }

        @Override
        protected void deliverResponse(@Nullable Bitmap response) {
            if (isBitmapNeeded) {
                imageRequest.deliverResponse(response);
            } else {
                listener.onResponse(response);
            }
        }

        @Override
        public String getCacheKey() {
            return hash;
        }

        private Cache.Entry createCashEntry(NetworkResponse response) {

            Cache.Entry entry = new Cache.Entry();
            entry.data = response.data;
            final Calendar instance = Calendar.getInstance();
            instance.add(Calendar.DAY_OF_YEAR, CACHE_VALID_DAYS);
            entry.ttl = instance.getTimeInMillis();
            entry.softTtl = entry.ttl;

            return entry;
        }

        class MyImageRequest extends ImageRequest {

            public MyImageRequest(String url,
                                  Response.Listener<Bitmap> listener,
                                  Response.ErrorListener errorListener) {
                super(url, listener, maxWidth, maxHeight, scaleType, decodeConfig, errorListener);
            }

            @Override
            protected Response<Bitmap> parseNetworkResponse(NetworkResponse response) {
                return super.parseNetworkResponse(response);
            }

            @Override
            protected void deliverResponse(Bitmap response) {
                super.deliverResponse(response);
            }
        }
    }
}
