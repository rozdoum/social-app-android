package com.rozdoum.socialcomponents.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;

public class RoundImage extends Drawable {
    private final static float BORDER_WIDTH = 12; //px
    private final static String BORDER_COLOR = "#ffffff";

    private final Bitmap mBitmap;
    private final Paint mPaintBitmap;
    private final Paint mPaintBorder;
    private final RectF mRectF;
    private final int mBitmapWidth;
    private final int mBitmapHeight;

    public RoundImage(Bitmap bitmap) {
        mBitmap = centerCropBitmap(bitmap);
        mRectF = new RectF();
        mPaintBitmap = new Paint();
        mPaintBitmap.setAntiAlias(true);
        mPaintBitmap.setDither(true);
        final BitmapShader shader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        mPaintBitmap.setShader(shader);

        mPaintBorder = new Paint();
        mPaintBorder.setStyle(Paint.Style.STROKE);
        mPaintBorder.setAntiAlias(true);
        mPaintBorder.setColor(Color.parseColor(BORDER_COLOR));
        mPaintBorder.setStrokeWidth(BORDER_WIDTH);

        mBitmapWidth = mBitmap.getWidth();
        mBitmapHeight = mBitmap.getHeight();
    }

    private Bitmap centerCropBitmap(Bitmap bm) {
        if (bm.getWidth() != bm.getHeight()) {
            int dimension = getSquareCropDimensionForBitmap(bm);
            return ThumbnailUtils.extractThumbnail(bm, dimension, dimension);
        } else {
            return bm;
        }
    }

    private int getSquareCropDimensionForBitmap(Bitmap bm) {
        if (bm.getWidth() >= bm.getHeight()) return bm.getHeight();
        return bm.getWidth();
    }

    @Override
    public void draw(Canvas canvas) {
        float borderRadius = mRectF.height() / 2 - BORDER_WIDTH / 2;
        float bitmapRadius = mRectF.height() / 2 - BORDER_WIDTH;
        canvas.drawCircle(mRectF.centerX(), mRectF.centerY(), bitmapRadius, mPaintBitmap);
        canvas.drawCircle(mRectF.centerX(), mRectF.centerY(), borderRadius, mPaintBorder);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mRectF.set(bounds);
    }

    @Override
    public void setAlpha(int alpha) {
        if (mPaintBitmap.getAlpha() != alpha) {
            mPaintBitmap.setAlpha(alpha);
            invalidateSelf();
        }
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mPaintBitmap.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public int getIntrinsicWidth() {
        return mBitmapWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return mBitmapHeight;
    }

    public void setAntiAlias(boolean aa) {
        mPaintBitmap.setAntiAlias(aa);
        invalidateSelf();
    }

    @Override
    public void setFilterBitmap(boolean filter) {
        mPaintBitmap.setFilterBitmap(filter);
        invalidateSelf();
    }

    @Override
    public void setDither(boolean dither) {
        mPaintBitmap.setDither(dither);
        invalidateSelf();
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

}

