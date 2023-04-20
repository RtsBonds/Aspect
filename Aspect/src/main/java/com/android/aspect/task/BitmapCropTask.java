package com.android.aspect.task;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;

import com.android.aspect.ImageViewCrop;
import com.android.aspect.utils.BitmapUtils;

import java.lang.ref.WeakReference;

@SuppressWarnings("ALL")
public final class BitmapCropTask
        extends AsyncTask<Void, Void, BitmapCropTask.Result> {
    private final WeakReference<ImageViewCrop> reference;

    private final Bitmap bitmap;

    private final Uri uri;

    @SuppressLint("StaticFieldLeak")
    private final Context context;

    private final float[] points;

    private final int anInt;

    private final int originalWidth;

    private final int originalHeight;

    private final boolean fixAspectRatio;

    private final int aspectRatioX;

    private final int aspectRatioY;

    private final int requestWidth;

    private final int requestHeight;

    private final boolean aBoolean;

    private final boolean aBoolean1;

    private final ImageViewCrop.RequestSizeOptions options;

    private final Uri uri1;

    private final Bitmap.CompressFormat format;

    private final int saveQuality;

    public BitmapCropTask(
            ImageViewCrop cropImageView,
            Bitmap bitmap,
            float[] cropPoints,
            int degreesRotated,
            boolean fixAspectRatio,
            int aspectRatioX,
            int aspectRatioY,
            int reqWidth,
            int reqHeight,
            boolean flipHorizontally,
            boolean flipVertically,
            ImageViewCrop.RequestSizeOptions options,
            Uri saveUri,
            Bitmap.CompressFormat saveCompressFormat,
            int saveCompressQuality) {

        reference = new WeakReference<>(cropImageView);
        context = cropImageView.getContext();
        this.bitmap = bitmap;
        points = cropPoints;
        uri = null;
        anInt = degreesRotated;
        this.fixAspectRatio = fixAspectRatio;
        this.aspectRatioX = aspectRatioX;
        this.aspectRatioY = aspectRatioY;
        requestWidth = reqWidth;
        requestHeight = reqHeight;
        aBoolean = flipHorizontally;
        aBoolean1 = flipVertically;
        this.options = options;
        uri1 = saveUri;
        format = saveCompressFormat;
        saveQuality = saveCompressQuality;
        originalWidth = 0;
        originalHeight = 0;
    }

    public BitmapCropTask(
            ImageViewCrop cropImageView,
            Uri uri,
            float[] cropPoints,
            int degreesRotated,
            int orgWidth,
            int orgHeight,
            boolean fixAspectRatio,
            int aspectRatioX,
            int aspectRatioY,
            int reqWidth,
            int reqHeight,
            boolean flipHorizontally,
            boolean flipVertically,
            ImageViewCrop.RequestSizeOptions options,
            Uri saveUri,
            Bitmap.CompressFormat saveCompressFormat,
            int saveCompressQuality) {

        reference = new WeakReference<>(cropImageView);
        context = cropImageView.getContext();
        this.uri = uri;
        points = cropPoints;
        anInt = degreesRotated;
        this.fixAspectRatio = fixAspectRatio;
        this.aspectRatioX = aspectRatioX;
        this.aspectRatioY = aspectRatioY;
        originalWidth = orgWidth;
        originalHeight = orgHeight;
        requestWidth = reqWidth;
        requestHeight = reqHeight;
        aBoolean = flipHorizontally;
        aBoolean1 = flipVertically;
        this.options = options;
        uri1 = saveUri;
        format = saveCompressFormat;
        saveQuality = saveCompressQuality;
        bitmap = null;
    }

    @Override
    protected Result doInBackground(Void... params) {
        try {
            if (!isCancelled()) {
                BitmapUtils.BitmapSampled bitmapSampled;
                if (uri != null) {
                    bitmapSampled =
                            BitmapUtils.bitmap(
                                    context,
                                    uri,
                                    points,
                                    anInt,
                                    originalWidth,
                                    originalHeight,
                                    fixAspectRatio,
                                    aspectRatioX,
                                    aspectRatioY,
                                    requestWidth,
                                    requestHeight,
                                    aBoolean,
                                    aBoolean1);
                } else if (bitmap != null) {
                    bitmapSampled =
                            BitmapUtils.bitmapHandler(
                                    bitmap,
                                    points,
                                    anInt,
                                    fixAspectRatio,
                                    aspectRatioX,
                                    aspectRatioY,
                                    aBoolean,
                                    aBoolean1);
                } else {
                    return new Result((Bitmap) null, 1);
                }
                Bitmap bitmap =
                        BitmapUtils.resizeBitmap(bitmapSampled.bitmap, requestWidth, requestHeight, options);
                if (uri1 == null) {
                    return new Result(bitmap, bitmapSampled.sampleSize);
                } else {
                    BitmapUtils.writeBitmapToUri(
                            context, bitmap, uri1, format, saveQuality);
                    bitmap.recycle();
                    return new Result(uri1, bitmapSampled.sampleSize);
                }
            }
            return null;
        } catch (Exception e) {
            return new Result(e, uri1 != null);
        }
    }

    @Override
    protected void onPostExecute(Result result) {
        if (result != null) {
            boolean completeCalled = false;
            if (!isCancelled()) {
                ImageViewCrop cropImageView = reference.get();
                if (cropImageView != null) {
                    completeCalled = true;
                    cropImageView.onImageCropping(result);
                }
            }
            if (!completeCalled && result.bitmap != null) {
                result.bitmap.recycle();
            }
        }
    }

    public static final class Result {
        public final Bitmap bitmap;
        public final Uri uri;
        public final Exception exception;
        final boolean save;
        public final int size;

        Result(Bitmap bitmap, int sampleSize) {
            this.bitmap = bitmap;
            this.uri = null;
            this.exception = null;
            this.save = false;
            this.size = sampleSize;
        }

        Result(Uri uri, int sampleSize) {
            this.bitmap = null;
            this.uri = uri;
            this.exception = null;
            this.save = true;
            this.size = sampleSize;
        }

        Result(Exception error, boolean isSave) {
            this.bitmap = null;
            this.uri = null;
            this.exception = error;
            this.save = isSave;
            this.size = 1;
        }
    }
}
