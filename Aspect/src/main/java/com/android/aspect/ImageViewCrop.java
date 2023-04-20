package com.android.aspect;

import static com.android.aspect.utils.BitmapUtils.RECT;
import static com.android.aspect.utils.BitmapUtils.pair;
import static com.android.aspect.utils.BitmapUtils.rectBottom;
import static com.android.aspect.utils.BitmapUtils.rectCenterX;
import static com.android.aspect.utils.BitmapUtils.rectCenterY;
import static com.android.aspect.utils.BitmapUtils.rectHeight;
import static com.android.aspect.utils.BitmapUtils.rectLeft;
import static com.android.aspect.utils.BitmapUtils.rectPoints;
import static com.android.aspect.utils.BitmapUtils.rectRight;
import static com.android.aspect.utils.BitmapUtils.rectTop;
import static com.android.aspect.utils.BitmapUtils.rectWidth;
import static com.android.aspect.utils.BitmapUtils.stateStoreBitmap;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.android.aspect.task.BitmapCropTask;
import com.android.aspect.task.BitmapLoadTask;
import java.lang.ref.WeakReference;
import java.util.UUID;

public class ImageViewCrop extends FrameLayout {

    private final android.widget.ImageView imageView;

    private final OverlayView view;

    private final Matrix matrix = new Matrix();

    private final Matrix InverseMatrix = new Matrix();

    private final float[] points = new float[8];

    private final float[] ImagePoints = new float[8];

    private Bitmap bitmap;

    private int anInt;

    private int anInt1;

    private boolean aBoolean;

    private boolean aBoolean1;

    private int layoutWidth;

    private int layoutHeight;

    private int resource;

    private final ScaleType type;


    private boolean saveBitmap = false;


    private boolean showCropOverlay = true;

    private int anInt2;

    private OnSetImageUriCompleteListener completeListener;

    private OnCropImageCompleteListener listener;

    private Uri mLoadedImageUri;

    private int mLoadedSampleSize = 1;

    private float mZoom = 1;

    private float zoomX;

    private float zoomY;

    private RectF rectF;

    private int restore;

    private boolean mSizeChanged;

    private Uri mSaveInstanceStateBitmapUri;

    private WeakReference<BitmapLoadTask> taskWeakReference;

    private WeakReference<BitmapCropTask> reference;

    public ImageViewCrop(Context context) {
        this(context, null);
    }

    public ImageViewCrop(Context context, AttributeSet attrs) {
        super(context, attrs);

        CropOptions options = null;
        Intent intent = context instanceof Activity ? ((Activity) context).getIntent() : null;
        if (intent != null) {
            Bundle bundle = intent.getBundleExtra(ImageCrop.BUNDLE);
            if (bundle != null) {
                options = bundle.getParcelable(ImageCrop.OPTIONS);
            }
        }

        if (options == null) {

            options = new CropOptions();

            if (attrs != null) {
                @SuppressLint("CustomViewStyleable") TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CropImageView, 0, 0);
                try {
                    options.fixAspectRatio =
                            ta.getBoolean(R.styleable.CropImageView_FixAspectRatio, options.fixAspectRatio);
                    options.aspectRatioX =
                            ta.getInteger(R.styleable.CropImageView_AspectRatioX, options.aspectRatioX);
                    options.aspectRatioY =
                            ta.getInteger(R.styleable.CropImageView_AspectRatioY, options.aspectRatioY);
                    options.scaleType =
                            ScaleType.values()[
                                    ta.getInt(R.styleable.CropImageView_scaleType, options.scaleType.ordinal())];
                    options.shape =
                            CropShape.values()[
                                    ta.getInt(R.styleable.CropImageView_shape, options.shape.ordinal())];
                    options.guidelines =
                            Guidelines.values()[
                                    ta.getInt(
                                            R.styleable.CropImageView_guidelines, options.guidelines.ordinal())];
                    options.radius =
                            ta.getDimension(R.styleable.CropImageView_snapRadius, options.radius);
                    options.touchRadius =
                            ta.getDimension(R.styleable.CropImageView_touchRadius, options.touchRadius);
                    options.CropRatio =
                            ta.getFloat(
                                    R.styleable.CropImageView_paddingRatio,
                                    options.CropRatio);
                    options.borderLineThickness =
                            ta.getDimension(
                                    R.styleable.CropImageView_borderLineThickness, options.borderLineThickness);
                    options.borderLineColor =
                            ta.getInteger(R.styleable.CropImageView_borderLineColor, options.borderLineColor);
                    options.borderCornerThickness =
                            ta.getDimension(
                                    R.styleable.CropImageView_borderCornerThickness,
                                    options.borderCornerThickness);
                    options.borderCornerOffset =
                            ta.getDimension(
                                    R.styleable.CropImageView_borderCornerOffset, options.borderCornerOffset);
                    options.borderCornerLength =
                            ta.getDimension(
                                    R.styleable.CropImageView_borderCornerLength, options.borderCornerLength);
                    options.borderCornerColor =
                            ta.getInteger(
                                    R.styleable.CropImageView_borderCornerColor, options.borderCornerColor);
                    options.guidelinesThickness =
                            ta.getDimension(
                                    R.styleable.CropImageView_guidelinesThickness, options.guidelinesThickness);
                    options.guidelinesColor =
                            ta.getInteger(R.styleable.CropImageView_guidelinesColor, options.guidelinesColor);
                    options.backgroundColor =
                            ta.getInteger(R.styleable.CropImageView_backgroundColor, options.backgroundColor);
                    options.Overlay =
                            ta.getBoolean(R.styleable.CropImageView_cropOverlay, showCropOverlay);
                    options.borderCornerThickness =
                            ta.getDimension(
                                    R.styleable.CropImageView_borderCornerThickness,
                                    options.borderCornerThickness);
                    options.minCropWindowWidth =
                            (int)
                                    ta.getDimension(
                                            R.styleable.CropImageView_minCropWindowWidth, options.minCropWindowWidth);
                    options.minCropWindowHeight =
                            (int)
                                    ta.getDimension(
                                            R.styleable.CropImageView_minCropWindowHeight,
                                            options.minCropWindowHeight);
                    options.minCropResultWidth =
                            (int)
                                    ta.getFloat(
                                            R.styleable.CropImageView_minCropResultWidthPX,
                                            options.minCropResultWidth);
                    options.minCropResultHeight =
                            (int)
                                    ta.getFloat(
                                            R.styleable.CropImageView_minCropResultHeightPX,
                                            options.minCropResultHeight);
                    options.maxCropResultWidth =
                            (int)
                                    ta.getFloat(
                                            R.styleable.CropImageView_maxCropResultWidthPX,
                                            options.maxCropResultWidth);
                    options.maxCropResultHeight =
                            (int)
                                    ta.getFloat(
                                            R.styleable.CropImageView_maxCropResultHeightPX,
                                            options.maxCropResultHeight);
                    saveBitmap =
                            ta.getBoolean(
                                    R.styleable.CropImageView_saveBitmap,
                                    saveBitmap);

                    if (ta.hasValue(R.styleable.CropImageView_AspectRatioX)
                            && ta.hasValue(R.styleable.CropImageView_AspectRatioX)
                            && !ta.hasValue(R.styleable.CropImageView_FixAspectRatio)) {
                        options.fixAspectRatio = true;
                    }
                } finally {
                    ta.recycle();
                }
            }
        }


        type = options.scaleType;
        showCropOverlay = options.Overlay;
        aBoolean = options.aBoolean3;
        aBoolean1 = options.vertically;

        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.image_view, this, true);

        imageView = v.findViewById(R.id.ImageView_image);
        imageView.setScaleType(android.widget.ImageView.ScaleType.MATRIX);

        view = v.findViewById(R.id.CropOverlayView);
        view.setCropListener(
                this::handleCrop);
        view.setValues(options);
    }

    public int getRotatedDegrees() {
        return anInt1;
    }

    public void setFixedAspectRatio(boolean fixAspectRatio) {
        view.setFixedAspectRatio(fixAspectRatio);
    }

    public Pair<Integer, Integer> getAspectRatio() {
        return new Pair<>(view.getAspectRatioX(), view.getAspectRatioY());
    }

    public void setAspectRatio(int aspectRatioX, int aspectRatioY) {
        view.setAspectRatioX(aspectRatioX);
        view.setAspectRatioY(aspectRatioY);
        setFixedAspectRatio(true);
    }

    public Uri getImageUri() {
        return mLoadedImageUri;
    }

    public Rect getWholeImageRect() {
        int loadedSampleSize = mLoadedSampleSize;
        Bitmap bitmap = this.bitmap;
        if (bitmap == null) {
            return null;
        }

        int orgWidth = bitmap.getWidth() * loadedSampleSize;
        int orgHeight = bitmap.getHeight() * loadedSampleSize;
        return new Rect(0, 0, orgWidth, orgHeight);
    }

    public Rect getCropRect() {
        int loadedSampleSize = mLoadedSampleSize;
        Bitmap bitmap = this.bitmap;
        if (bitmap == null) {
            return null;
        }

        float[] points = getCropPoints();

        int orgWidth = bitmap.getWidth() * loadedSampleSize;
        int orgHeight = bitmap.getHeight() * loadedSampleSize;

        return rectPoints(
                points,
                orgWidth,
                orgHeight,
                view.isFixAspectRatio(),
                view.getAspectRatioX(),
                view.getAspectRatioY());
    }


    public float[] getCropPoints() {

        RectF cropWindowRect = view.getCropWindowRect();

        float[] points =
                new float[]{
                        cropWindowRect.left,
                        cropWindowRect.top,
                        cropWindowRect.right,
                        cropWindowRect.top,
                        cropWindowRect.right,
                        cropWindowRect.bottom,
                        cropWindowRect.left,
                        cropWindowRect.bottom
                };

        matrix.invert(InverseMatrix);
        InverseMatrix.mapPoints(points);

        for (int i = 0; i < points.length; i++) {
            points[i] *= mLoadedSampleSize;
        }

        return points;
    }

    public void setCropRect(Rect rect) {
        view.cropRect(rect);
    }

    public void saveCroppedImage(
            Uri saveUri,
            Bitmap.CompressFormat saveCompressFormat,
            int saveCompressQuality,
            int reqWidth,
            int reqHeight,
            RequestSizeOptions options) {
        if (listener == null) {
            throw new IllegalArgumentException("listener is not set");
        }
        startCropWorkerTask(
                reqWidth, reqHeight, options, saveUri, saveCompressFormat, saveCompressQuality);
    }

    public void setOnSetImageUriCompleteListener(OnSetImageUriCompleteListener listener) {
        completeListener = listener;
    }

    public void setOnCropImageCompleteListener(OnCropImageCompleteListener listener) {
        this.listener = listener;
    }

    public void setImageResource(int resId) {
        if (resId != 0) {
            view.cropRect(null);
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), resId);
            setBitmap(bitmap, resId, null, 1, 0);
        }
    }

    public void setImageUriAsync(Uri uri) {
        if (uri != null) {
            BitmapLoadTask currentTask =
                    taskWeakReference != null ? taskWeakReference.get() : null;
            if (currentTask != null) {
                currentTask.cancel(true);
            }
            clearImageInt();
            rectF = null;
            restore = 0;
            view.cropRect(null);
            taskWeakReference = new WeakReference<>(new BitmapLoadTask(this, uri));
            taskWeakReference.get().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void onSetImageUri(BitmapLoadTask.Result result) {

        taskWeakReference = null;

        if (result.exception == null) {
            anInt = result.anInt;
            setBitmap(result.bitmap, 0, result.uri, result.loadSampleSize, result.anInt);
        }

        OnSetImageUriCompleteListener listener = completeListener;
        if (listener != null) {
            listener.onSetImageUriComplete(this, result.uri, result.exception);
        }
    }

    public void onImageCropping(BitmapCropTask.Result result) {
        reference = null;
        OnCropImageCompleteListener listener = this.listener;
        if (listener != null) {
            CropResult cropResult =
                    new CropResult(
                            mLoadedImageUri,
                            result.uri,
                            result.exception,
                            getCropPoints(),
                            getCropRect(),
                            getWholeImageRect(),
                            getRotatedDegrees(),
                            result.size);
            listener.onCropImageComplete(this, cropResult);
        }
    }

    private void setBitmap(
            Bitmap bitmap, int imageResource, Uri imageUri, int loadSampleSize, int degreesRotated) {
        if (this.bitmap == null || !this.bitmap.equals(bitmap)) {

            imageView.clearAnimation();

            clearImageInt();

            this.bitmap = bitmap;
            imageView.setImageBitmap(this.bitmap);

            mLoadedImageUri = imageUri;
            resource = imageResource;
            mLoadedSampleSize = loadSampleSize;
            anInt1 = degreesRotated;

            applyImageMatrix(getWidth(), getHeight(), true);

            if (view != null) {
                view.resetCropView();
                setCropVisibility();
            }
        }
    }

    private void clearImageInt() {

        if (bitmap != null && (resource > 0 || mLoadedImageUri != null)) {
            bitmap.recycle();
        }
        bitmap = null;
        resource = 0;
        mLoadedImageUri = null;
        mLoadedSampleSize = 1;
        anInt1 = 0;
        mZoom = 1;
        zoomX = 0;
        zoomY = 0;
        matrix.reset();
        mSaveInstanceStateBitmapUri = null;
        imageView.setImageBitmap(null);
        setCropVisibility();
    }

    public void startCropWorkerTask(
            int reqWidth,
            int reqHeight,
            RequestSizeOptions options,
            Uri saveUri,
            Bitmap.CompressFormat saveCompressFormat,
            int saveCompressQuality) {
        Bitmap bitmap = this.bitmap;
        if (bitmap != null) {
            imageView.clearAnimation();

            BitmapCropTask currentTask =
                    reference != null ? reference.get() : null;
            if (currentTask != null) {
                currentTask.cancel(true);
            }

            reqWidth = options != RequestSizeOptions.NONE ? reqWidth : 0;
            reqHeight = options != RequestSizeOptions.NONE ? reqHeight : 0;

            int orgWidth = bitmap.getWidth() * mLoadedSampleSize;
            int orgHeight = bitmap.getHeight() * mLoadedSampleSize;
            if (mLoadedImageUri != null
                    && (mLoadedSampleSize > 1 || options == RequestSizeOptions.SAMPLING)) {
                reference =
                        new WeakReference<>(
                                new BitmapCropTask(
                                        this,
                                        mLoadedImageUri,
                                        getCropPoints(),
                                        anInt1,
                                        orgWidth,
                                        orgHeight,
                                        view.isFixAspectRatio(),
                                        view.getAspectRatioX(),
                                        view.getAspectRatioY(),
                                        reqWidth,
                                        reqHeight,
                                        aBoolean,
                                        aBoolean1,
                                        options,
                                        saveUri,
                                        saveCompressFormat,
                                        saveCompressQuality));
            } else {
                reference =
                        new WeakReference<>(
                                new BitmapCropTask(
                                        this,
                                        bitmap,
                                        getCropPoints(),
                                        anInt1,
                                        view.isFixAspectRatio(),
                                        view.getAspectRatioX(),
                                        view.getAspectRatioY(),
                                        reqWidth,
                                        reqHeight,
                                        aBoolean,
                                        aBoolean1,
                                        options,
                                        saveUri,
                                        saveCompressFormat,
                                        saveCompressQuality));
            }
            reference.get().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public Parcelable onSaveInstanceState() {
        if (mLoadedImageUri == null && bitmap == null && resource < 1) {
            return super.onSaveInstanceState();
        }

        Bundle bundle = new Bundle();
        Uri imageUri = mLoadedImageUri;
        if (saveBitmap && imageUri == null && resource < 1) {
            mSaveInstanceStateBitmapUri =
                    imageUri =
                            stateStoreBitmap(
                                    getContext(), bitmap, mSaveInstanceStateBitmapUri);
        }
        if (imageUri != null && bitmap != null) {
            String key = UUID.randomUUID().toString();
            pair = new Pair<>(key, new WeakReference<>(bitmap));
            bundle.putString("imageKey", key);
        }
        if (taskWeakReference != null) {
            BitmapLoadTask task = taskWeakReference.get();
            if (task != null) {
                bundle.putParcelable("imageUri", task.getUri());
            }
        }
        bundle.putParcelable("instanceState", super.onSaveInstanceState());
        bundle.putParcelable("Uri", imageUri);
        bundle.putInt("resource", resource);
        bundle.putInt("size", mLoadedSampleSize);
        bundle.putParcelable("rect", view.getCropRect());

        RECT.set(view.getCropWindowRect());

        matrix.invert(InverseMatrix);
        InverseMatrix.mapRect(RECT);

        bundle.putParcelable("rect", RECT);
        bundle.putString("shape", view.getCropShape().name());
        bundle.putInt("zoom", anInt2);
        bundle.putBoolean("a", aBoolean);
        bundle.putBoolean("b", aBoolean1);

        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {

        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;

            if (taskWeakReference == null
                    && mLoadedImageUri == null
                    && bitmap == null
                    && resource == 0) {

                Uri uri = bundle.getParcelable("ImageUri");
                if (uri != null) {
                    String key = bundle.getString("imageKey");
                    if (key != null) {
                        Bitmap stateBitmap =
                                pair != null && pair.first.equals(key)
                                        ? pair.second.get()
                                        : null;
                        pair = null;
                        if (stateBitmap != null && !stateBitmap.isRecycled()) {
                            setBitmap(stateBitmap, 0, uri, bundle.getInt("size"), 0);
                        }
                    }
                    if (mLoadedImageUri == null) {
                        setImageUriAsync(uri);
                    }
                } else {
                    int resId = bundle.getInt("resource");
                    if (resId > 0) {
                        setImageResource(resId);
                    } else {
                        uri = bundle.getParcelable("Uri");
                        if (uri != null) {
                            setImageUriAsync(uri);
                        }
                    }
                }

                anInt1 = restore = bundle.getInt("DEGREES_ROTATED");

                Rect initialCropRect = bundle.getParcelable("rect");
                if (initialCropRect != null
                        && (initialCropRect.width() > 0 || initialCropRect.height() > 0)) {
                    view.cropRect(initialCropRect);
                }

                RectF cropWindowRect = bundle.getParcelable("rect");
                if (cropWindowRect != null && (cropWindowRect.width() > 0 || cropWindowRect.height() > 0)) {
                    rectF = cropWindowRect;
                }

                view.setCropShape(CropShape.valueOf(bundle.getString("shape")));

                anInt2 = bundle.getInt("zoom");

                aBoolean = bundle.getBoolean("a");
                aBoolean1 = bundle.getBoolean("b");
            }

            super.onRestoreInstanceState(bundle.getParcelable("instanceState"));
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (bitmap != null) {

            if (heightSize == 0) {
                heightSize = bitmap.getHeight();
            }

            int desiredWidth;
            int desiredHeight;

            double viewToBitmapWidthRatio = Double.POSITIVE_INFINITY;
            double viewToBitmapHeightRatio = Double.POSITIVE_INFINITY;

            if (widthSize < bitmap.getWidth()) {
                viewToBitmapWidthRatio = (double) widthSize / (double) bitmap.getWidth();
            }
            if (heightSize < bitmap.getHeight()) {
                viewToBitmapHeightRatio = (double) heightSize / (double) bitmap.getHeight();
            }

            if (viewToBitmapWidthRatio != Double.POSITIVE_INFINITY
                    || viewToBitmapHeightRatio != Double.POSITIVE_INFINITY) {
                if (viewToBitmapWidthRatio <= viewToBitmapHeightRatio) {
                    desiredWidth = widthSize;
                    desiredHeight = (int) (bitmap.getHeight() * viewToBitmapWidthRatio);
                } else {
                    desiredHeight = heightSize;
                    desiredWidth = (int) (bitmap.getWidth() * viewToBitmapHeightRatio);
                }
            } else {
                desiredWidth = bitmap.getWidth();
                desiredHeight = bitmap.getHeight();
            }

            int width = getOnMeasureSpec(widthMode, widthSize, desiredWidth);
            int height = getOnMeasureSpec(heightMode, heightSize, desiredHeight);

            layoutWidth = width;
            layoutHeight = height;

            setMeasuredDimension(layoutWidth, layoutHeight);

        } else {
            setMeasuredDimension(widthSize, heightSize);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        super.onLayout(changed, l, t, r, b);

        if (layoutWidth > 0 && layoutHeight > 0) {
            ViewGroup.LayoutParams origParams = this.getLayoutParams();
            origParams.width = layoutWidth;
            origParams.height = layoutHeight;
            setLayoutParams(origParams);

            if (bitmap != null) {
                applyImageMatrix(r - l, b - t, true);

                if (rectF != null) {
                    if (restore != anInt) {
                        anInt1 = restore;
                        applyImageMatrix(r - l, b - t, true);
                    }
                    matrix.mapRect(rectF);
                    view.setCropWindowRect(rectF);
                    handleCrop(false);
                    view.fixCurrentCropRect();
                    rectF = null;
                } else if (mSizeChanged) {
                    mSizeChanged = false;
                    handleCrop(false);
                }
            } else {
                ImageBounds(true);
            }
        } else {
            ImageBounds(true);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        mSizeChanged = oldW > 0 && oldH > 0;
    }

    private void handleCrop(boolean inProgress) {
        int width = getWidth();
        int height = getHeight();
        if (bitmap != null && width > 0 && height > 0) {
            RectF cropRect = view.getCropWindowRect();
            if (inProgress) {
                if (cropRect.left < 0
                        || cropRect.top < 0
                        || cropRect.right > width
                        || cropRect.bottom > height) {
                    applyImageMatrix(width, height, false);
                }
            }
        }
    }

    private void applyImageMatrix(float width, float height, boolean center) {
        if (bitmap != null && width > 0 && height > 0) {

            matrix.invert(InverseMatrix);
            RectF cropRect = view.getCropWindowRect();
            InverseMatrix.mapRect(cropRect);

            matrix.reset();

            matrix.postTranslate(
                    (width - bitmap.getWidth()) / 2, (height - bitmap.getHeight()) / 2);
            mapImagePoints();


            float scale =
                    Math.min(
                            width / rectWidth(points),
                            height / rectHeight(points));
            if (type == ScaleType.FIT_CENTER
                    || (type == ScaleType.CENTER_INSIDE && scale < 1)) {
                matrix.postScale(
                        scale,
                        scale,
                        rectCenterX(points),
                        rectCenterY(points));
                mapImagePoints();
            }

            float scaleX = aBoolean ? -mZoom : mZoom;
            float scaleY = aBoolean1 ? -mZoom : mZoom;
            matrix.postScale(
                    scaleX,
                    scaleY,
                    rectCenterX(points),
                    rectCenterY(points));
            mapImagePoints();

            matrix.mapRect(cropRect);

            if (center) {
                zoomX =
                        width > rectWidth(points)
                                ? 0
                                : Math.max(
                                Math.min(
                                        width / 2 - cropRect.centerX(), -rectLeft(points)),
                                getWidth() - rectRight(points))
                                / scaleX;
                zoomY =
                        height > rectHeight(points)
                                ? 0
                                : Math.max(
                                Math.min(
                                        height / 2 - cropRect.centerY(), -rectTop(points)),
                                getHeight() - rectBottom(points))
                                / scaleY;
            } else {
                zoomX =
                        Math.min(Math.max(zoomX * scaleX, -cropRect.left), -cropRect.right + width)
                                / scaleX;
                zoomY =
                        Math.min(Math.max(zoomY * scaleY, -cropRect.top), -cropRect.bottom + height)
                                / scaleY;
            }

            matrix.postTranslate(zoomX * scaleX, zoomY * scaleY);
            cropRect.offset(zoomX * scaleX, zoomY * scaleY);
            view.setCropWindowRect(cropRect);
            mapImagePoints();
            view.invalidate();

            imageView.setImageMatrix(matrix);
            ImageBounds(false);
        }
    }

    private void mapImagePoints() {
        points[0] = 0;
        points[1] = 0;
        points[2] = bitmap.getWidth();
        points[3] = 0;
        points[4] = bitmap.getWidth();
        points[5] = bitmap.getHeight();
        points[6] = 0;
        points[7] = bitmap.getHeight();
        matrix.mapPoints(points);
        ImagePoints[0] = 0;
        ImagePoints[1] = 0;
        ImagePoints[2] = 100;
        ImagePoints[3] = 0;
        ImagePoints[4] = 100;
        ImagePoints[5] = 100;
        ImagePoints[6] = 0;
        ImagePoints[7] = 100;
        matrix.mapPoints(ImagePoints);
    }

    private static int getOnMeasureSpec(int measureSpecMode, int measureSpecSize, int desiredSize) {

        int spec;
        if (measureSpecMode == MeasureSpec.EXACTLY) {
            spec = measureSpecSize;
        } else if (measureSpecMode == MeasureSpec.AT_MOST) {
            spec = Math.min(desiredSize, measureSpecSize);
        } else {
            spec = desiredSize;
        }

        return spec;
    }

    private void setCropVisibility() {
        if (view != null) {
            view.setVisibility(showCropOverlay && bitmap != null ? VISIBLE : INVISIBLE);
        }
    }

    private void ImageBounds(boolean clear) {
        if (bitmap != null && !clear) {

            float scaleFactorWidth =
                    100f * mLoadedSampleSize / rectWidth(ImagePoints);
            float scaleFactorHeight =
                    100f * mLoadedSampleSize / rectHeight(ImagePoints);
            view.setCropLimits(
                    getWidth(), getHeight(), scaleFactorWidth, scaleFactorHeight);
        }
        view.setBounds(clear ? null : points, getWidth(), getHeight());
    }

    public enum CropShape {
        RECTANGLE,
        OVAL
    }

    public enum ScaleType {
        FIT_CENTER,
        CENTER,
        CENTER_CROP,
        CENTER_INSIDE
    }

    public enum Guidelines {
        OFF,
        ON_TOUCH,
        ON
    }

    public enum RequestSizeOptions {
        NONE,
        SAMPLING,
        RESIZE_INSIDE,
        RESIZE_FIT,
        RESIZE_EXACT
    }

    public interface OnSetImageUriCompleteListener {
        void onSetImageUriComplete(ImageViewCrop view, Uri uri, Exception error);
    }

    public interface OnCropImageCompleteListener {
        void onCropImageComplete(ImageViewCrop view, CropResult result);
    }

    public static class CropResult {

        private final Uri uri1;

        private final Uri uri;
        private final Exception exception;

        private final float[] floats;

        private final Rect rect1;

        private final Rect rect;

        private final int anInt3;

        private final int size;

        CropResult(
                Uri originalUri,
                Uri uri,
                Exception error,
                float[] cropPoints,
                Rect cropRect,
                Rect wholeImageRect,
                int rotation,
                int sampleSize) {
            uri1 = originalUri;
            this.uri = uri;
            exception = error;
            floats = cropPoints;
            rect1 = cropRect;
            rect = wholeImageRect;
            anInt3 = rotation;
            size = sampleSize;
        }


        public Uri getOriginalUri() {
            return uri1;
        }

        public Uri getUri() {
            return uri;
        }

        public Exception getError() {
            return exception;
        }

        public float[] getCropPoints() {
            return floats;
        }

        public Rect getCropRect() {
            return rect1;
        }

        public Rect getWholeImageRect() {
            return rect;
        }

        public int getRotation() {
            return anInt3;
        }

        public int getSampleSize() {
            return size;
        }
    }
}
