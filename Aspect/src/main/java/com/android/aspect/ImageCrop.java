package com.android.aspect;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("ALL")
public final class ImageCrop {

    public static final String SOURCE = "SOURCE";

    public static final String OPTIONS = "OPTIONS";

    public static final String BUNDLE = "BUNDLE";

    public static final String EXTRA_RESULT = "EXTRA_RESULT";

    public static final int REQUEST_CODE = 2;

    public static final int PICK_IMAGE_PERMISSIONS_REQUEST_CODE = 10;

    public static final int CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE = 8;

    public static final int ACTIVITY_REQUEST_CODE = 4;

    public static final int RESULT_ERROR_CODE = 6;

    private ImageCrop() {
    }

    public static Bitmap bitmap(@NonNull Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);

        int color = 0xff424242;
        Paint paint = new Paint();

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);

        RectF rect = new RectF(0, 0, width, height);
        canvas.drawOval(rect, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, 0, 0, paint);

        bitmap.recycle();

        return output;
    }

    public static void getImageActivity(@NonNull Activity activity) {
        activity.startActivityForResult(
                getImageIntent(activity), REQUEST_CODE);
    }

    public static void getImageActivity(@NonNull Context context, @NonNull Fragment fragment) {
        fragment.startActivityForResult(
                getImageIntent(context), REQUEST_CODE);
    }


    public static Intent getImageIntent(@NonNull Context context) {
        return getImageIntent(
                context, context.getString(R.string.image_intent), false, true);
    }

    public static Intent getImageIntent(
            @NonNull Context context,
            CharSequence title,
            boolean includeDocuments,
            boolean includeCamera) {
        PackageManager packageManager = context.getPackageManager();
        List<Intent> galleryIntents =
                getGalleryIntents(packageManager, Intent.ACTION_GET_CONTENT, includeDocuments);
        if (galleryIntents.size() == 0) {
            galleryIntents = getGalleryIntents(packageManager, Intent.ACTION_GET_CONTENT, includeDocuments);
        }
        List<Intent> allIntents = new ArrayList<>(galleryIntents);
        Intent target;
        if (allIntents.isEmpty()) {
            target = new Intent();
        } else {
            target = allIntents.get(allIntents.size() - 1);
            allIntents.remove(allIntents.size() - 1);
        }
        Intent chooserIntent = Intent.createChooser(target, title);
        chooserIntent.putExtra(
                Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[0]));
        return chooserIntent;
    }

    public static List<Intent> getGalleryIntents(
            @NonNull PackageManager packageManager, String action, boolean includeDocuments) {
        List<Intent> intents = new ArrayList<>();
        Intent galleryIntent =
                Objects.equals(action, Intent.ACTION_PICK)
                        ? new Intent(action)
                        : new Intent(action, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            intents.add(intent);
        }

        if (!includeDocuments) {
            for (Intent intent : intents) {
                if (intent
                        .getComponent()
                        .getClassName()
                        .equals("document")) {
                    intents.remove(intent);
                    break;
                }
            }
        }
        return intents;
    }

    public static Uri getCaptureImageOutput(@NonNull Context context) {
        Uri outputFileUri = null;
        File getImage = context.getExternalCacheDir();
        if (getImage != null) {
            outputFileUri = Uri.fromFile(new File(getImage.getPath(), "pickImageResult.jpeg"));
        }
        return outputFileUri;
    }

    public static Uri getImageResult(@NonNull Context context, @Nullable Intent data) {
        boolean isCamera = true;
        if (data != null && data.getData() != null) {
            String action = data.getAction();
            isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
        }
        return isCamera || data.getData() == null ? getCaptureImageOutput(context) : data.getData();
    }

    public static ActivityBuilder activity() {
        return new ActivityBuilder(null);
    }

    public static ActivityBuilder activity(@Nullable Uri uri) {
        return new ActivityBuilder(uri);
    }

    public static ActivityResult getActivityResult(@Nullable Intent data) {
        return data != null ? (ActivityResult) data.getParcelableExtra(EXTRA_RESULT) : null;
    }

    public static final class ActivityBuilder {

        @Nullable
        private final Uri mSource;
        private final CropOptions mOptions;

        private ActivityBuilder(@Nullable Uri source) {
            mSource = source;
            mOptions = new CropOptions();
        }

        public Intent getIntent(@NonNull Context context) {
            return getIntent(context, CropActivity.class);
        }

        public Intent getIntent(@NonNull Context context, @Nullable Class<?> cls) {

            Intent intent = new Intent();
            intent.setClass(context, cls);
            Bundle bundle = new Bundle();
            bundle.putParcelable(SOURCE, mSource);
            bundle.putParcelable(OPTIONS, mOptions);
            intent.putExtra(ImageCrop.BUNDLE, bundle);
            return intent;
        }

        public void start(@NonNull Activity activity) {
            activity.startActivityForResult(getIntent(activity), ACTIVITY_REQUEST_CODE);
        }


        public void start(@NonNull Activity activity, @Nullable Class<?> cls) {
            activity.startActivityForResult(getIntent(activity, cls), ACTIVITY_REQUEST_CODE);
        }


        public void start(@NonNull Context context, @NonNull android.app.Fragment fragment) {
            fragment.startActivityForResult(getIntent(context), ACTIVITY_REQUEST_CODE);
        }

        public void start(
                @NonNull Context context, @NonNull Fragment fragment, @Nullable Class<?> cls) {
            fragment.startActivityForResult(getIntent(context, cls), ACTIVITY_REQUEST_CODE);
        }


        public void start(
                @NonNull Context context, @NonNull android.app.Fragment fragment, @Nullable Class<?> cls) {
            fragment.startActivityForResult(getIntent(context, cls), ACTIVITY_REQUEST_CODE);
        }


        public ActivityBuilder setCropShape(@NonNull ImageViewCrop.CropShape cropShape) {
            mOptions.shape = cropShape;
            return this;
        }


        public ActivityBuilder setSnapRadius(float snapRadius) {
            mOptions.radius = snapRadius;
            return this;
        }

        public ActivityBuilder setTouchRadius(float touchRadius) {
            mOptions.touchRadius = touchRadius;
            return this;
        }

        public ActivityBuilder setGuidelines(@NonNull ImageViewCrop.Guidelines guidelines) {
            mOptions.guidelines = guidelines;
            return this;
        }


        public ActivityBuilder setScaleType(@NonNull ImageViewCrop.ScaleType scaleType) {
            mOptions.scaleType = scaleType;
            return this;
        }


        public ActivityBuilder setShowOverlay(boolean showCropOverlay) {
            mOptions.Overlay = showCropOverlay;
            return this;
        }

        public ActivityBuilder setPaddingRatio(float initialCropWindowPaddingRatio) {
            mOptions.CropRatio = initialCropWindowPaddingRatio;
            return this;
        }


        public ActivityBuilder setFixAspectRatio(boolean fixAspectRatio) {
            mOptions.fixAspectRatio = fixAspectRatio;
            return this;
        }

        public ActivityBuilder setAspectRatio(int aspectRatioX, int aspectRatioY) {
            mOptions.aspectRatioX = aspectRatioX;
            mOptions.aspectRatioY = aspectRatioY;
            mOptions.fixAspectRatio = true;
            return this;
        }

        public ActivityBuilder setBorderLineThickness(float borderLineThickness) {
            mOptions.borderLineThickness = borderLineThickness;
            return this;
        }


        public ActivityBuilder setBorderLineColor(int borderLineColor) {
            mOptions.borderLineColor = borderLineColor;
            return this;
        }


        public ActivityBuilder setBorderCornerThickness(float borderCornerThickness) {
            mOptions.borderCornerThickness = borderCornerThickness;
            return this;
        }

        public ActivityBuilder setBorderCornerOffset(float borderCornerOffset) {
            mOptions.borderCornerOffset = borderCornerOffset;
            return this;
        }

        public ActivityBuilder setBorderCornerLength(float borderCornerLength) {
            mOptions.borderCornerLength = borderCornerLength;
            return this;
        }

        public ActivityBuilder setBorderCornerColor(int borderCornerColor) {
            mOptions.borderCornerColor = borderCornerColor;
            return this;
        }


        public ActivityBuilder setGuidelinesThickness(float guidelinesThickness) {
            mOptions.guidelinesThickness = guidelinesThickness;
            return this;
        }


        public ActivityBuilder setGuidelinesColor(int guidelinesColor) {
            mOptions.guidelinesColor = guidelinesColor;
            return this;
        }

        public ActivityBuilder setBackgroundColor(int backgroundColor) {
            mOptions.backgroundColor = backgroundColor;
            return this;
        }

        public ActivityBuilder setMinCropWindowSize(int minCropWindowWidth, int minCropWindowHeight) {
            mOptions.minCropWindowWidth = minCropWindowWidth;
            mOptions.minCropWindowHeight = minCropWindowHeight;
            return this;
        }

        public ActivityBuilder setMinCropResultSize(int minCropResultWidth, int minCropResultHeight) {
            mOptions.minCropResultWidth = minCropResultWidth;
            mOptions.minCropResultHeight = minCropResultHeight;
            return this;
        }

        public ActivityBuilder setMaxCropResultSize(int maxCropResultWidth, int maxCropResultHeight) {
            mOptions.maxCropResultWidth = maxCropResultWidth;
            mOptions.maxCropResultHeight = maxCropResultHeight;
            return this;
        }

        public ActivityBuilder setOutputUri(Uri outputUri) {
            mOptions.outputUri = outputUri;
            return this;
        }


        public ActivityBuilder setOutputCompressFormat(Bitmap.CompressFormat outputCompressFormat) {
            mOptions.compressFormat = outputCompressFormat;
            return this;
        }


        public ActivityBuilder setOutputCompressQuality(int outputCompressQuality) {
            mOptions.outputQuality = outputCompressQuality;
            return this;
        }

        public ActivityBuilder setRequestedSize(int reqWidth, int reqHeight) {
            return setRequestedSize(reqWidth, reqHeight, ImageViewCrop.RequestSizeOptions.RESIZE_INSIDE);
        }


        public ActivityBuilder setRequestedSize(
                int reqWidth, int reqHeight, ImageViewCrop.RequestSizeOptions options) {
            mOptions.outputRequestWidth = reqWidth;
            mOptions.outputRequestHeight = reqHeight;
            mOptions.sizeOptions = options;
            return this;
        }

    }

    public static final class ActivityResult extends ImageViewCrop.CropResult implements Parcelable {

        public static final Creator<ActivityResult> CREATOR =
                new Creator<ActivityResult>() {
                    @Override
                    public ActivityResult createFromParcel(Parcel in) {
                        return new ActivityResult(in);
                    }

                    @Override
                    public ActivityResult[] newArray(int size) {
                        return new ActivityResult[size];
                    }
                };

        public ActivityResult(
                Uri originalUri,
                Uri uri,
                Exception error,
                float[] cropPoints,
                Rect cropRect,
                int rotation,
                Rect wholeImageRect,
                int sampleSize) {
            super(
                    originalUri,
                    uri,
                    error,
                    cropPoints,
                    cropRect,
                    wholeImageRect,
                    rotation,
                    sampleSize);
        }

        private ActivityResult(Parcel in) {
            super(
                    in.readParcelable(Uri.class.getClassLoader()),
                    in.readParcelable(Uri.class.getClassLoader()),
                    (Exception) in.readSerializable(),
                    in.createFloatArray(),
                    in.readParcelable(Rect.class.getClassLoader()),
                    in.readParcelable(Rect.class.getClassLoader()),
                    in.readInt(),
                    in.readInt());
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(getOriginalUri(), flags);
            dest.writeParcelable(getUri(), flags);
            dest.writeSerializable(getError());
            dest.writeFloatArray(getCropPoints());
            dest.writeParcelable(getCropRect(), flags);
            dest.writeParcelable(getWholeImageRect(), flags);
            dest.writeInt(getRotation());
            dest.writeInt(getSampleSize());
        }

        @Override
        public int describeContents() {
            return 0;
        }
    }
}
