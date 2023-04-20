package com.android.aspect.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Pair;
import com.android.aspect.ImageViewCrop;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

@SuppressWarnings("ALL")
public final class BitmapUtils {
    public static final Rect RECT_E = new Rect();
    public static final RectF RECT_F = new RectF();
    public static final RectF RECT = new RectF();
    private static int maxTextureSize;
    public static Pair<String, WeakReference<Bitmap>> pair;

    public static result bitmapExif(Bitmap bitmap, Context context, Uri uri) {
        ExifInterface ei = null;
        try {
            InputStream is = context.getContentResolver().openInputStream(uri);
            if (is != null) {
                ei = new ExifInterface(is);
                is.close();
            }
        } catch (Exception ignored) {
        }
        return ei != null ? bitmapExif(bitmap) : new result(bitmap);
    }

    static result bitmapExif(Bitmap bitmap) {
        return new result(bitmap);
    }

    public static BitmapSampled decodeBitmap(Context context, Uri uri, int reqWidth, int reqHeight) {

        try {
            ContentResolver resolver = context.getContentResolver();

            BitmapFactory.Options options = decodeImage(resolver, uri);

            if (options.outWidth == -1 && options.outHeight == -1)
                throw new RuntimeException("picture not");

            options.inSampleSize =
                    Math.max(
                            requestedSize(
                                    options.outWidth, options.outHeight, reqWidth, reqHeight),
                            textureSize(options.outWidth, options.outHeight));

            Bitmap bitmap = decodeImage(resolver, uri, options);

            return new BitmapSampled(bitmap, options.inSampleSize);

        } catch (Exception e) {
            throw new RuntimeException(
                    "load bitmap failed: " + uri + "\r\n" + e.getMessage(), e);
        }
    }

    public static BitmapSampled bitmapHandler(
            Bitmap bitmap,
            float[] points,
            int degreesRotated,
            boolean fixAspectRatio,
            int aspectRatioX,
            int aspectRatioY,
            boolean flipHorizontally,
            boolean flipVertically) {
        int scale = 1;
        while (true) {
            try {
                Bitmap cropBitmap =
                        bitmapObjectAndScale(
                                bitmap,
                                points,
                                degreesRotated,
                                fixAspectRatio,
                                aspectRatioX,
                                aspectRatioY,
                                1 / (float) scale,
                                flipHorizontally,
                                flipVertically);
                return new BitmapSampled(cropBitmap, scale);
            } catch (OutOfMemoryError e) {
                scale *= 2;
                if (scale > 8) {
                    throw e;
                }
            }
        }
    }

    private static Bitmap bitmapObjectAndScale(
            Bitmap bitmap,
            float[] points,
            int degreesRotated,
            boolean fixAspectRatio,
            int aspectRatioX,
            int aspectRatioY,
            float scale,
            boolean flipHorizontally,
            boolean flipVertically) {

        Rect rect =
                rectPoints(
                        points,
                        bitmap.getWidth(),
                        bitmap.getHeight(),
                        fixAspectRatio,
                        aspectRatioX,
                        aspectRatioY);

        Matrix matrix = new Matrix();
        matrix.setRotate(degreesRotated, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
        matrix.postScale(flipHorizontally ? -scale : scale, flipVertically ? -scale : scale);
        Bitmap result =
                Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height(), matrix, true);

        if (result == bitmap) {
            result = bitmap.copy(bitmap.getConfig(), false);
        }

        if (degreesRotated % 90 != 0) {

            result =
                    Image(
                            result, points, rect, degreesRotated, fixAspectRatio, aspectRatioX, aspectRatioY);
        }

        return result;
    }

    public static BitmapSampled bitmap(
            Context context,
            Uri loadedImageUri,
            float[] points,
            int degreesRotated,
            int orgWidth,
            int orgHeight,
            boolean fixAspectRatio,
            int aspectRatioX,
            int aspectRatioY,
            int reqWidth,
            int reqHeight,
            boolean flipHorizontally,
            boolean flipVertically) {
        int sampleMulti = 1;
        while (true) {
            try {
                return bitmap(
                        context,
                        loadedImageUri,
                        points,
                        degreesRotated,
                        orgWidth,
                        orgHeight,
                        fixAspectRatio,
                        aspectRatioX,
                        aspectRatioY,
                        reqWidth,
                        reqHeight,
                        flipHorizontally,
                        flipVertically,
                        sampleMulti);
            } catch (OutOfMemoryError e) {
                sampleMulti *= 2;
                if (sampleMulti > 16) {
                    throw new RuntimeException(
                            ""
                                    + sampleMulti
                                    + "): "
                                    + loadedImageUri
                                    + "\r\n"
                                    + e.getMessage(),
                            e);
                }
            }
        }
    }

    public static float rectLeft(float[] points) {
        return Math.min(Math.min(Math.min(points[0], points[2]), points[4]), points[6]);
    }

    public static float rectTop(float[] points) {
        return Math.min(Math.min(Math.min(points[1], points[3]), points[5]), points[7]);
    }

    public static float rectRight(float[] points) {
        return Math.max(Math.max(Math.max(points[0], points[2]), points[4]), points[6]);
    }

    public static float rectBottom(float[] points) {
        return Math.max(Math.max(Math.max(points[1], points[3]), points[5]), points[7]);
    }

    public static float rectWidth(float[] points) {
        return rectRight(points) - rectLeft(points);
    }

    public static float rectHeight(float[] points) {
        return rectBottom(points) - rectTop(points);
    }

    public static float rectCenterX(float[] points) {
        return (rectRight(points) + rectLeft(points)) / 2f;
    }

    public static float rectCenterY(float[] points) {
        return (rectBottom(points) + rectTop(points)) / 2f;
    }

    public static Rect rectPoints(
            float[] points,
            int imageWidth,
            int imageHeight,
            boolean fixAspectRatio,
            int aspectRatioX,
            int aspectRatioY) {
        int left = Math.round(Math.max(0, rectLeft(points)));
        int top = Math.round(Math.max(0, rectTop(points)));
        int right = Math.round(Math.min(imageWidth, rectRight(points)));
        int bottom = Math.round(Math.min(imageHeight, rectBottom(points)));

        Rect rect = new Rect(left, top, right, bottom);
        if (fixAspectRatio) {
            rectAspectRatio(rect, aspectRatioX, aspectRatioY);
        }

        return rect;
    }

    private static void rectAspectRatio(Rect rect, int aspectRatioX, int aspectRatioY) {
        if (aspectRatioX == aspectRatioY && rect.width() != rect.height()) {
            if (rect.height() > rect.width()) {
                rect.bottom -= rect.height() - rect.width();
            } else {
                rect.right -= rect.width() - rect.height();
            }
        }
    }

    public static Uri stateStoreBitmap(Context context, Bitmap bitmap, Uri uri) {
        try {
            boolean needSave = true;
            if (uri == null) {
                uri =
                        Uri.fromFile(
                                File.createTempFile("", ".jpg", context.getCacheDir()));
            } else if (new File(uri.getPath()).exists()) {
                needSave = false;
            }
            if (needSave) {
                writeBitmapToUri(context, bitmap, uri, Bitmap.CompressFormat.JPEG, 95);
            }
            return uri;
        } catch (Exception e) {
            return null;
        }
    }

    public static void writeBitmapToUri(
            Context context,
            Bitmap bitmap,
            Uri uri,
            Bitmap.CompressFormat compressFormat,
            int compressQuality)
            throws FileNotFoundException {
        OutputStream outputStream = null;
        try {
            outputStream = context.getContentResolver().openOutputStream(uri);
            bitmap.compress(compressFormat, compressQuality, outputStream);
        } finally {
            close(outputStream);
        }
    }

    public static Bitmap resizeBitmap(
            Bitmap bitmap, int reqWidth, int reqHeight, ImageViewCrop.RequestSizeOptions options) {
        try {
            if (reqWidth > 0
                    && reqHeight > 0
                    && (options == ImageViewCrop.RequestSizeOptions.RESIZE_FIT
                    || options == ImageViewCrop.RequestSizeOptions.RESIZE_INSIDE
                    || options == ImageViewCrop.RequestSizeOptions.RESIZE_EXACT)) {

                Bitmap resized = null;
                if (options == ImageViewCrop.RequestSizeOptions.RESIZE_EXACT) {
                    resized = Bitmap.createScaledBitmap(bitmap, reqWidth, reqHeight, false);
                } else {
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    float scale = Math.max(width / (float) reqWidth, height / (float) reqHeight);
                    if (scale > 1 || options == ImageViewCrop.RequestSizeOptions.RESIZE_FIT) {
                        resized =
                                Bitmap.createScaledBitmap(
                                        bitmap, (int) (width / scale), (int) (height / scale), false);
                    }
                }
                if (resized != null) {
                    if (resized != bitmap) {
                        bitmap.recycle();
                    }
                    return resized;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return bitmap;
    }

    private static BitmapSampled bitmap(
            Context context,
            Uri loadedImageUri,
            float[] points,
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
            int sampleMulti) {

        Rect rect =
                rectPoints(points, orgWidth, orgHeight, fixAspectRatio, aspectRatioX, aspectRatioY);

        int width = reqWidth > 0 ? reqWidth : rect.width();
        int height = reqHeight > 0 ? reqHeight : rect.height();

        Bitmap result = null;
        int sampleSize = 1;
        try {
            BitmapSampled bitmapSampled =
                    bitmapRegion(context, loadedImageUri, rect, width, height, sampleMulti);
            result = bitmapSampled.bitmap;
            sampleSize = bitmapSampled.sampleSize;
        } catch (Exception ignored) {
        }

        if (result != null) {
            try {
                result = bitmapInt(result);

                if (degreesRotated % 90 != 0) {

                    result =
                            Image(
                                    result, points, rect, degreesRotated, fixAspectRatio, aspectRatioX, aspectRatioY);
                }
            } catch (OutOfMemoryError e) {
                if (result != null) {
                    result.recycle();
                }
                throw e;
            }
            return new BitmapSampled(result, sampleSize);
        } else {
            return bitmap(
                    context,
                    loadedImageUri,
                    points,
                    degreesRotated,
                    fixAspectRatio,
                    aspectRatioX,
                    aspectRatioY,
                    sampleMulti,
                    rect,
                    width,
                    height,
                    flipHorizontally,
                    flipVertically);
        }
    }

    private static BitmapSampled bitmap(
            Context context,
            Uri loadedImageUri,
            float[] points,
            int degreesRotated,
            boolean fixAspectRatio,
            int aspectRatioX,
            int aspectRatioY,
            int sampleMulti,
            Rect rect,
            int width,
            int height,
            boolean flipHorizontally,
            boolean flipVertically) {
        Bitmap result = null;
        int sampleSize;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize =
                    sampleSize =
                            sampleMulti
                                    * requestedSize(rect.width(), rect.height(), width, height);

            Bitmap fullBitmap = decodeImage(context.getContentResolver(), loadedImageUri, options);
            if (fullBitmap != null) {
                try {
                    float[] points2 = new float[points.length];
                    System.arraycopy(points, 0, points2, 0, points.length);
                    for (int i = 0; i < points2.length; i++) {
                        points2[i] = points2[i] / options.inSampleSize;
                    }

                    result =
                            bitmapObjectAndScale(
                                    fullBitmap,
                                    points2,
                                    degreesRotated,
                                    fixAspectRatio,
                                    aspectRatioX,
                                    aspectRatioY,
                                    1,
                                    flipHorizontally,
                                    flipVertically);
                } finally {
                    if (result != fullBitmap) {
                        fullBitmap.recycle();
                    }
                }
            }
        } catch (OutOfMemoryError e) {
            if (result != null) {
                result.recycle();
            }
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to load bitmap: " + loadedImageUri + "\r\n" + e.getMessage(), e);
        }
        return new BitmapSampled(result, sampleSize);
    }

    private static BitmapFactory.Options decodeImage(ContentResolver resolver, Uri uri)
            throws FileNotFoundException {
        InputStream stream = null;
        try {
            stream = resolver.openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(stream, RECT_E, options);
            options.inJustDecodeBounds = false;
            return options;
        } finally {
            close(stream);
        }
    }

    private static Bitmap decodeImage(
            ContentResolver resolver, Uri uri, BitmapFactory.Options options)
            throws FileNotFoundException {
        do {
            InputStream stream = null;
            try {
                stream = resolver.openInputStream(uri);
                return BitmapFactory.decodeStream(stream, RECT_E, options);
            } catch (OutOfMemoryError e) {
                options.inSampleSize *= 2;
            } finally {
                close(stream);
            }
        } while (options.inSampleSize <= 512);
        throw new RuntimeException("Failed to decode image: " + uri);
    }

    private static BitmapSampled bitmapRegion(
            Context context, Uri uri, Rect rect, int reqWidth, int reqHeight, int sampleMulti) {
        InputStream stream = null;
        BitmapRegionDecoder decoder = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize =
                    sampleMulti
                            * requestedSize(
                            rect.width(), rect.height(), reqWidth, reqHeight);

            stream = context.getContentResolver().openInputStream(uri);
            decoder = BitmapRegionDecoder.newInstance(stream, false);
            do {
                try {
                    return new BitmapSampled(decoder.decodeRegion(rect, options), options.inSampleSize);
                } catch (OutOfMemoryError e) {
                    options.inSampleSize *= 2;
                }
            } while (options.inSampleSize <= 512);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to load sampled bitmap: " + uri + "\r\n" + e.getMessage(), e);
        } finally {
            close(stream);
            if (decoder != null) {
                decoder.recycle();
            }
        }
        return new BitmapSampled(null, 1);
    }

    private static Bitmap Image(
            Bitmap bitmap,
            float[] points,
            Rect rect,
            int degreesRotated,
            boolean fixAspectRatio,
            int aspectRatioX,
            int aspectRatioY) {
        if (degreesRotated % 90 != 0) {

            int adjLeft = 0, adjTop = 0, width = 0, height = 0;
            double rads = Math.toRadians(degreesRotated);
            int compareTo =
                    degreesRotated < 90 || (degreesRotated > 180 && degreesRotated < 270)
                            ? rect.left
                            : rect.right;
            for (int i = 0; i < points.length; i += 2) {
                if (points[i] >= compareTo - 1 && points[i] <= compareTo + 1) {
                    adjLeft = (int) Math.abs(Math.sin(rads) * (rect.bottom - points[i + 1]));
                    adjTop = (int) Math.abs(Math.cos(rads) * (points[i + 1] - rect.top));
                    width = (int) Math.abs((points[i + 1] - rect.top) / Math.sin(rads));
                    height = (int) Math.abs((rect.bottom - points[i + 1]) / Math.cos(rads));
                    break;
                }
            }

            rect.set(adjLeft, adjTop, adjLeft + width, adjTop + height);
            if (fixAspectRatio) {
                rectAspectRatio(rect, aspectRatioX, aspectRatioY);
            }

            Bitmap bitmapTmp = bitmap;
            bitmap = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height());
            if (bitmapTmp != bitmap) {
                bitmapTmp.recycle();
            }
        }
        return bitmap;
    }

    private static int requestedSize(
            int width, int height, int reqWidth, int reqHeight) {
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            while ((height / 2 / inSampleSize) > reqHeight && (width / 2 / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private static int textureSize(int width, int height) {
        int inSampleSize = 1;
        if (maxTextureSize == 0) {
            maxTextureSize = maxTextSize();
        }
        if (maxTextureSize > 0) {
            while ((height / inSampleSize) > maxTextureSize
                    || (width / inSampleSize) > maxTextureSize) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private static Bitmap bitmapInt(
            Bitmap bitmap) {
        Matrix matrix = new Matrix();
        Bitmap newBitmap =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
        if (newBitmap != bitmap) {
            bitmap.recycle();
        }
        return newBitmap;
    }

    private static int maxTextSize() {
        final int IMAGE_MAX_BITMAP_DIMENSION = 2048;

        try {
            EGL10 egl = (EGL10) EGLContext.getEGL();
            EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

            int[] version = new int[2];
            egl.eglInitialize(display, version);

            int[] totalConfigurations = new int[1];
            egl.eglGetConfigs(display, null, 0, totalConfigurations);

            EGLConfig[] configurationsList = new EGLConfig[totalConfigurations[0]];
            egl.eglGetConfigs(display, configurationsList, totalConfigurations[0], totalConfigurations);

            int[] textureSize = new int[1];
            int maximumTextureSize = 0;

            for (int i = 0; i < totalConfigurations[0]; i++) {
                egl.eglGetConfigAttrib(
                        display, configurationsList[i], EGL10.EGL_MAX_PBUFFER_WIDTH, textureSize);

                if (maximumTextureSize < textureSize[0]) {
                    maximumTextureSize = textureSize[0];
                }
            }

            egl.eglTerminate(display);

            return Math.max(maximumTextureSize, IMAGE_MAX_BITMAP_DIMENSION);
        } catch (Exception e) {
            return IMAGE_MAX_BITMAP_DIMENSION;
        }
    }

    private static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }

    public static final class BitmapSampled {

        public final Bitmap bitmap;

        public final int sampleSize;

        BitmapSampled(Bitmap bitmap, int sampleSize) {
            this.bitmap = bitmap;
            this.sampleSize = sampleSize;
        }
    }

    public static final class result {
        public final Bitmap bitmap;

        result(Bitmap bitmap) {
            this.bitmap = bitmap;
        }
    }
}