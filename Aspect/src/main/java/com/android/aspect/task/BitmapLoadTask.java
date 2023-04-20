package com.android.aspect.task;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import com.android.aspect.ImageViewCrop;
import com.android.aspect.utils.BitmapUtils;

import java.lang.ref.WeakReference;

@SuppressWarnings("ALL")
public final class BitmapLoadTask extends AsyncTask<Void, Void, BitmapLoadTask.Result> {

    private final WeakReference<ImageViewCrop> reference;
    private final Uri uri;
    private final Context context;
    private final int width;
    private final int height;

    // endregion
    public BitmapLoadTask(ImageViewCrop cropImageView, Uri uri) {
        this.uri = uri;
        reference = new WeakReference<>(cropImageView);

        context = cropImageView.getContext();

        DisplayMetrics metrics = cropImageView.getResources().getDisplayMetrics();
        double densityAdj = metrics.density > 1 ? 1 / metrics.density : 1;
        width = (int) (metrics.widthPixels * densityAdj);
        height = (int) (metrics.heightPixels * densityAdj);
    }

    public Uri getUri() {
        return uri;
    }

    @Override
    protected Result doInBackground(Void... params) {
        try {
            if (!isCancelled()) {
                BitmapUtils.BitmapSampled decodeResult =
                        BitmapUtils.decodeBitmap(context, uri, width, height);
                if (!isCancelled()) {
                    BitmapUtils.result rotateResult =
                            BitmapUtils.bitmapExif(decodeResult.bitmap, context, uri);
                    return new Result(
                            uri, rotateResult.bitmap, decodeResult.sampleSize, 0);
                }
            }
            return null;
        } catch (Exception e) {
            return new Result(uri, e);
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
                    cropImageView.onSetImageUri(result);
                }
            }
            if (!completeCalled && result.bitmap != null) {
                result.bitmap.recycle();
            }
        }
    }

    public static final class Result {
        public final Uri uri;
        public final Bitmap bitmap;
        public final int loadSampleSize;
        public final int anInt;
        public final Exception exception;

        Result(Uri uri, Bitmap bitmap, int loadSampleSize, int degreesRotated) {
            this.uri = uri;
            this.bitmap = bitmap;
            this.loadSampleSize = loadSampleSize;
            this.anInt = degreesRotated;
            this.exception = null;
        }

        Result(Uri uri, Exception error) {
            this.uri = uri;
            this.bitmap = null;
            this.loadSampleSize = 0;
            this.anInt = 0;
            this.exception = error;
        }
    }
}
