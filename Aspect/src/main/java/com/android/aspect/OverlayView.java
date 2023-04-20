package com.android.aspect;

import static com.android.aspect.utils.BitmapUtils.RECT_E;
import static com.android.aspect.utils.BitmapUtils.RECT_F;
import static com.android.aspect.utils.BitmapUtils.rectBottom;
import static com.android.aspect.utils.BitmapUtils.rectLeft;
import static com.android.aspect.utils.BitmapUtils.rectRight;
import static com.android.aspect.utils.BitmapUtils.rectTop;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.android.aspect.handler.CropHandler;
import com.android.aspect.handler.MoveHandler;
import java.util.Arrays;

public class OverlayView extends View {

    private final CropHandler handlerRect = new CropHandler();

    private CropWindowChangeListener listener;

    private final RectF rectF = new RectF();

    private Paint paint;

    private Paint paint1;

    private Paint guidelinePaint;

    private Paint backgroundPaint;

    private final Path path = new Path();

    private final float[] boundsPoints = new float[8];

    private final RectF f = new RectF();

    private int viewWidth;

    private int viewHeight;

    private float aFloat;

    private float borderCornerLength;

    private float cropPaddingRatio;

    private float touchRadius;


    private float radius;

    private MoveHandler handler;

    private boolean aBoolean;

    private int aspectRatioX;

    private int aspectRatioY;

    private float aspectRatio = ((float) aspectRatioX) / aspectRatioY;

    private ImageViewCrop.Guidelines guidelines;

    private ImageViewCrop.CropShape shape;

    private final Rect rect = new Rect();

    private boolean aBoolean1;

    public OverlayView(Context context) {
        this(context, null);
    }

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCropListener(CropWindowChangeListener listener) {
        this.listener = listener;
    }

    public RectF getCropWindowRect() {
        return handlerRect.getRect();
    }

    public void setCropWindowRect(RectF rect) {
        handlerRect.rect(rect);
    }

    public void fixCurrentCropRect() {
        RectF rect = getCropWindowRect();
        fixCropRect(rect);
        handlerRect.rect(rect);
    }

    public void setBounds(float[] boundsPoints, int viewWidth, int viewHeight) {
        if (boundsPoints == null || !Arrays.equals(this.boundsPoints, boundsPoints)) {
            if (boundsPoints == null) {
                Arrays.fill(this.boundsPoints, 0);
            } else {
                System.arraycopy(boundsPoints, 0, this.boundsPoints, 0, boundsPoints.length);
            }
            this.viewWidth = viewWidth;
            this.viewHeight = viewHeight;
            RectF cropRect = handlerRect.getRect();
            if (cropRect.width() == 0 || cropRect.height() == 0) {
                initCrop();
            }
        }
    }

    public void resetCropView() {
        if (aBoolean1) {
            setCropWindowRect(RECT_F);
            initCrop();
            invalidate();
        }
    }

    public ImageViewCrop.CropShape getCropShape() {
        return shape;
    }

    public void setCropShape(ImageViewCrop.CropShape cropShape) {
        if (shape != cropShape) {
            shape = cropShape;
            invalidate();
        }
    }

    public void setGuidelines(ImageViewCrop.Guidelines guidelines) {
        if (this.guidelines != guidelines) {
            this.guidelines = guidelines;
            if (aBoolean1) {
                invalidate();
            }
        }
    }

    public boolean isFixAspectRatio() {
        return aBoolean;
    }

    public void setFixedAspectRatio(boolean fixAspectRatio) {
        if (aBoolean != fixAspectRatio) {
            aBoolean = fixAspectRatio;
            if (aBoolean1) {
                initCrop();
                invalidate();
            }
        }
    }

    public int getAspectRatioX() {
        return aspectRatioX;
    }

    public void setAspectRatioX(int aspectRatioX) {
        if (aspectRatioX <= 0) {
            throw new IllegalArgumentException(
                    "");
        } else if (this.aspectRatioX != aspectRatioX) {
            this.aspectRatioX = aspectRatioX;
            aspectRatio = ((float) this.aspectRatioX) / aspectRatioY;

            if (aBoolean1) {
                initCrop();
                invalidate();
            }
        }
    }

    public int getAspectRatioY() {
        return aspectRatioY;
    }

    public void setAspectRatioY(int aspectRatioY) {
        if (aspectRatioY <= 0) {
            throw new IllegalArgumentException(
                    "");
        } else if (this.aspectRatioY != aspectRatioY) {
            this.aspectRatioY = aspectRatioY;
            aspectRatio = ((float) aspectRatioX) / this.aspectRatioY;

            if (aBoolean1) {
                initCrop();
                invalidate();
            }
        }
    }

    public void setSnapRadius(float snapRadius) {
        radius = snapRadius;
    }

    public void setCropLimits(
            float maxWidth, float maxHeight, float scaleFactorWidth, float scaleFactorHeight) {
        handlerRect.setCropLimits(
                maxWidth, maxHeight, scaleFactorWidth, scaleFactorHeight);
    }

    public Rect getCropRect() {
        return rect;
    }

    public void cropRect(Rect rect) {
        this.rect.set(rect != null ? rect : RECT_E);
        if (aBoolean1) {
            initCrop();
            invalidate();
            cropChanged(false);
        }
    }

    public void setValues(CropOptions options) {

        handlerRect.setValues(options);

        setCropShape(options.shape);

        setSnapRadius(options.radius);

        setGuidelines(options.guidelines);

        setFixedAspectRatio(options.fixAspectRatio);

        setAspectRatioX(options.aspectRatioX);

        setAspectRatioY(options.aspectRatioY);

        touchRadius = options.touchRadius;

        cropPaddingRatio = options.CropRatio;

        paint = getNewPaint(options.borderLineThickness, options.borderLineColor);

        aFloat = options.borderCornerOffset;
        borderCornerLength = options.borderCornerLength;
        paint1 =
                getNewPaint(options.borderCornerThickness, options.borderCornerColor);

        guidelinePaint = getNewPaint(options.guidelinesThickness, options.guidelinesColor);

        backgroundPaint = getNewPaint(options.backgroundColor);
    }

    private void initCrop() {

        float leftLimit = Math.max(rectLeft(boundsPoints), 0);
        float topLimit = Math.max(rectTop(boundsPoints), 0);
        float rightLimit = Math.min(rectRight(boundsPoints), getWidth());
        float bottomLimit = Math.min(rectBottom(boundsPoints), getHeight());

        if (rightLimit <= leftLimit || bottomLimit <= topLimit) {
            return;
        }

        RectF rect = new RectF();

        aBoolean1 = true;

        float horizontalPadding = cropPaddingRatio * (rightLimit - leftLimit);
        float verticalPadding = cropPaddingRatio * (bottomLimit - topLimit);

        if (this.rect.width() > 0 && this.rect.height() > 0) {
            rect.left =
                    leftLimit + this.rect.left / handlerRect.getWidth();
            rect.top = topLimit + this.rect.top / handlerRect.getHeight();
            rect.right =
                    rect.left + this.rect.width() / handlerRect.getWidth();
            rect.bottom =
                    rect.top + this.rect.height() / handlerRect.getHeight();

            rect.left = Math.max(leftLimit, rect.left);
            rect.top = Math.max(topLimit, rect.top);
            rect.right = Math.min(rightLimit, rect.right);
            rect.bottom = Math.min(bottomLimit, rect.bottom);

        } else if (aBoolean) {

            float bitmapAspectRatio = (rightLimit - leftLimit) / (bottomLimit - topLimit);
            if (bitmapAspectRatio > aspectRatio) {

                rect.top = topLimit + verticalPadding;
                rect.bottom = bottomLimit - verticalPadding;

                float centerX = getWidth() / 2f;

                aspectRatio = (float) aspectRatioX / aspectRatioY;

                float cropWidth =
                        Math.max(handlerRect.getMinWidth(), rect.height() * aspectRatio);

                float halfCropWidth = cropWidth / 2f;
                rect.left = centerX - halfCropWidth;
                rect.right = centerX + halfCropWidth;

            } else {

                rect.left = leftLimit + horizontalPadding;
                rect.right = rightLimit - horizontalPadding;

                float centerY = getHeight() / 2f;

                float cropHeight =
                        Math.max(handlerRect.getMinHeight(), rect.width() / aspectRatio);

                float halfCropHeight = cropHeight / 2f;
                rect.top = centerY - halfCropHeight;
                rect.bottom = centerY + halfCropHeight;
            }
        } else {
            rect.left = leftLimit + horizontalPadding;
            rect.top = topLimit + verticalPadding;
            rect.right = rightLimit - horizontalPadding;
            rect.bottom = bottomLimit - verticalPadding;
        }
        fixCropRect(rect);
        handlerRect.rect(rect);
    }

    private void fixCropRect
            (RectF rect) {
        if (rect.width() < handlerRect.getMinWidth()) {
            float adj = (handlerRect.getMinWidth() - rect.width()) / 2;
            rect.left -= adj;
            rect.right += adj;
        }
        if (rect.height() < handlerRect.getMinHeight()) {
            float adj = (handlerRect.getMinHeight() - rect.height()) / 2;
            rect.top -= adj;
            rect.bottom += adj;
        }
        if (rect.width() > handlerRect.getMaxWidth()) {
            float adj = (rect.width() - handlerRect.getMaxWidth()) / 2;
            rect.left += adj;
            rect.right -= adj;
        }
        if (rect.height() > handlerRect.getMaxHeight()) {
            float adj = (rect.height() - handlerRect.getMaxHeight()) / 2;
            rect.top += adj;
            rect.bottom -= adj;
        }

        bounds(rect);
        if (f.width() > 0 && f.height() > 0) {
            float leftLimit = Math.max(f.left, 0);
            float topLimit = Math.max(f.top, 0);
            float rightLimit = Math.min(f.right, getWidth());
            float bottomLimit = Math.min(f.bottom, getHeight());
            if (rect.left < leftLimit) {
                rect.left = leftLimit;
            }
            if (rect.top < topLimit) {
                rect.top = topLimit;
            }
            if (rect.right > rightLimit) {
                rect.right = rightLimit;
            }
            if (rect.bottom > bottomLimit) {
                rect.bottom = bottomLimit;
            }
        }
        if (aBoolean && Math.abs(rect.width() - rect.height() * aspectRatio) > 0.1) {
            if (rect.width() > rect.height() * aspectRatio) {
                float adj = Math.abs(rect.height() * aspectRatio - rect.width()) / 2;
                rect.left += adj;
                rect.right -= adj;
            } else {
                float adj = Math.abs(rect.width() / aspectRatio - rect.height()) / 2;
                rect.top += adj;
                rect.bottom -= adj;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBackground(canvas);
        if (handlerRect.guidelines()) {
            if (guidelines == ImageViewCrop.Guidelines.ON) {
                drawGuidelines(canvas);
            } else if (guidelines == ImageViewCrop.Guidelines.ON_TOUCH && handler != null) {
                drawGuidelines(canvas);
            }
        }
        drawBorders(canvas);
        drawCorners(canvas);
    }

    private void drawBackground(Canvas canvas) {

        RectF rect = handlerRect.getRect();

        float left = Math.max(rectLeft(boundsPoints), 0);
        float top = Math.max(rectTop(boundsPoints), 0);
        float right = Math.min(rectRight(boundsPoints), getWidth());
        float bottom = Math.min(rectBottom(boundsPoints), getHeight());

        if (shape == ImageViewCrop.CropShape.RECTANGLE) {
            if (angle()) {
                canvas.drawRect(left, top, right, rect.top, backgroundPaint);
                canvas.drawRect(left, rect.bottom, right, bottom, backgroundPaint);
                canvas.drawRect(left, rect.top, rect.left, rect.bottom, backgroundPaint);
                canvas.drawRect(rect.right, rect.top, right, rect.bottom, backgroundPaint);
            } else {
                path.reset();
                path.moveTo(boundsPoints[0], boundsPoints[1]);
                path.lineTo(boundsPoints[2], boundsPoints[3]);
                path.lineTo(boundsPoints[4], boundsPoints[5]);
                path.lineTo(boundsPoints[6], boundsPoints[7]);
                path.close();

                canvas.save();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    canvas.clipOutPath(path);
                } else {
                    canvas.clipPath(path, Region.Op.INTERSECT);
                }
                canvas.clipRect(rect, Region.Op.XOR);
                canvas.drawRect(left, top, right, bottom, backgroundPaint);
                canvas.restore();
            }
        } else {
            path.reset();
            rectF.set(rect.left, rect.top, rect.right, rect.bottom);
            path.addOval(rectF, Path.Direction.CW);
            canvas.save();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                canvas.clipOutPath(path);
            } else {
                canvas.clipPath(path, Region.Op.XOR);
            }
            canvas.drawRect(left, top, right, bottom, backgroundPaint);
            canvas.restore();
        }
    }

    private void drawGuidelines(Canvas canvas) {
        if (guidelinePaint != null) {
            float sw = paint != null ? paint.getStrokeWidth() : 0;
            RectF rect = handlerRect.getRect();
            rect.inset(sw, sw);

            float oneThirdCropWidth = rect.width() / 3;
            float oneThirdCropHeight = rect.height() / 3;

            if (shape == ImageViewCrop.CropShape.OVAL) {

                float w = rect.width() / 2 - sw;
                float h = rect.height() / 2 - sw;

                float x1 = rect.left + oneThirdCropWidth;
                float x2 = rect.right - oneThirdCropWidth;
                float yv = (float) (h * Math.sin(Math.acos((w - oneThirdCropWidth) / w)));
                canvas.drawLine(x1, rect.top + h - yv, x1, rect.bottom - h + yv, guidelinePaint);
                canvas.drawLine(x2, rect.top + h - yv, x2, rect.bottom - h + yv, guidelinePaint);

                float y1 = rect.top + oneThirdCropHeight;
                float y2 = rect.bottom - oneThirdCropHeight;
                float xv = (float) (w * Math.cos(Math.asin((h - oneThirdCropHeight) / h)));
                canvas.drawLine(rect.left + w - xv, y1, rect.right - w + xv, y1, guidelinePaint);
                canvas.drawLine(rect.left + w - xv, y2, rect.right - w + xv, y2, guidelinePaint);
            } else {

                float x1 = rect.left + oneThirdCropWidth;
                float x2 = rect.right - oneThirdCropWidth;
                canvas.drawLine(x1, rect.top, x1, rect.bottom, guidelinePaint);
                canvas.drawLine(x2, rect.top, x2, rect.bottom, guidelinePaint);

                float y1 = rect.top + oneThirdCropHeight;
                float y2 = rect.bottom - oneThirdCropHeight;
                canvas.drawLine(rect.left, y1, rect.right, y1, guidelinePaint);
                canvas.drawLine(rect.left, y2, rect.right, y2, guidelinePaint);
            }
        }
    }

    private void drawBorders(Canvas canvas) {
        if (paint != null) {
            float w = paint.getStrokeWidth();
            RectF rect = handlerRect.getRect();
            rect.inset(w / 2, w / 2);

            if (shape == ImageViewCrop.CropShape.RECTANGLE) {
                canvas.drawRect(rect, paint);
            } else {
                canvas.drawOval(rect, paint);
            }
        }
    }

    private void drawCorners(Canvas canvas) {
        if (paint1 != null) {

            float lineWidth = paint != null ? paint.getStrokeWidth() : 0;
            float cornerWidth = paint1.getStrokeWidth();

            float w =
                    cornerWidth / 2
                            + (shape == ImageViewCrop.CropShape.RECTANGLE ? aFloat : 0);

            RectF rect = handlerRect.getRect();
            rect.inset(w, w);

            float cornerOffset = (cornerWidth - lineWidth) / 2;
            float cornerExtension = cornerWidth / 2 + cornerOffset;

            canvas.drawLine(
                    rect.left - cornerOffset,
                    rect.top - cornerExtension,
                    rect.left - cornerOffset,
                    rect.top + borderCornerLength,
                    paint1);
            canvas.drawLine(
                    rect.left - cornerExtension,
                    rect.top - cornerOffset,
                    rect.left + borderCornerLength,
                    rect.top - cornerOffset,
                    paint1);

            // Top right
            canvas.drawLine(
                    rect.right + cornerOffset,
                    rect.top - cornerExtension,
                    rect.right + cornerOffset,
                    rect.top + borderCornerLength,
                    paint1);
            canvas.drawLine(
                    rect.right + cornerExtension,
                    rect.top - cornerOffset,
                    rect.right - borderCornerLength,
                    rect.top - cornerOffset,
                    paint1);

            canvas.drawLine(
                    rect.left - cornerOffset,
                    rect.bottom + cornerExtension,
                    rect.left - cornerOffset,
                    rect.bottom - borderCornerLength,
                    paint1);
            canvas.drawLine(
                    rect.left - cornerExtension,
                    rect.bottom + cornerOffset,
                    rect.left + borderCornerLength,
                    rect.bottom + cornerOffset,
                    paint1);

            canvas.drawLine(
                    rect.right + cornerOffset,
                    rect.bottom + cornerExtension,
                    rect.right + cornerOffset,
                    rect.bottom - borderCornerLength,
                    paint1);
            canvas.drawLine(
                    rect.right + cornerExtension,
                    rect.bottom + cornerOffset,
                    rect.right - borderCornerLength,
                    rect.bottom + cornerOffset,
                    paint1);
        }
    }

    private static Paint getNewPaint(int color) {
        Paint paint = new Paint();
        paint.setColor(color);
        return paint;
    }

    private static Paint getNewPaint(float thickness, int color) {
        if (thickness > 0) {
            Paint borderPaint = new Paint();
            borderPaint.setColor(color);
            borderPaint.setStrokeWidth(thickness);
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setAntiAlias(true);
            return borderPaint;
        } else {
            return null;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isEnabled()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    onActionDown(event.getX(), event.getY());
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    getParent().requestDisallowInterceptTouchEvent(false);
                    onActionUp();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    onActionMove(event.getX(), event.getY());
                    getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    private void onActionDown(float x, float y) {
        handler = handlerRect.moveHandler(x, y, touchRadius, shape);
        if (handler != null) {
            invalidate();
        }
    }

    private void onActionUp() {
        if (handler != null) {
            handler = null;
            cropChanged(false);
            invalidate();
        }
    }

    private void onActionMove(float x, float y) {
        if (handler != null) {
            float snapRadius = radius;
            RectF rect = handlerRect.getRect();

            if (bounds(rect)) {
                snapRadius = 0;
            }

            handler.move(
                    rect,
                    x,
                    y,
                    f,
                    viewWidth,
                    viewHeight,
                    snapRadius,
                    aBoolean,
                    aspectRatio);
            handlerRect.rect(rect);
            cropChanged(true);
            invalidate();
        }
    }

    private boolean bounds(RectF rect) {

        float left = rectLeft(boundsPoints);
        float top = rectTop(boundsPoints);
        float right = rectRight(boundsPoints);
        float bottom = rectBottom(boundsPoints);

        if (angle()) {
            f.set(left, top, right, bottom);
            return false;
        } else {
            float x0 = boundsPoints[0];
            float y0 = boundsPoints[1];
            float x2 = boundsPoints[4];
            float y2 = boundsPoints[5];
            float x3 = boundsPoints[6];
            float y3 = boundsPoints[7];

            if (boundsPoints[7] < boundsPoints[1]) {
                if (boundsPoints[1] < boundsPoints[3]) {
                    x0 = boundsPoints[6];
                    y0 = boundsPoints[7];
                    x2 = boundsPoints[2];
                    y2 = boundsPoints[3];
                    x3 = boundsPoints[4];
                    y3 = boundsPoints[5];
                } else {
                    x0 = boundsPoints[4];
                    y0 = boundsPoints[5];
                    x2 = boundsPoints[0];
                    y2 = boundsPoints[1];
                    x3 = boundsPoints[2];
                    y3 = boundsPoints[3];
                }
            } else if (boundsPoints[1] > boundsPoints[3]) {
                x0 = boundsPoints[2];
                y0 = boundsPoints[3];
                x2 = boundsPoints[6];
                y2 = boundsPoints[7];
                x3 = boundsPoints[0];
                y3 = boundsPoints[1];
            }

            float a0 = (y3 - y0) / (x3 - x0);
            float a1 = -1f / a0;
            float b0 = y0 - a0 * x0;
            float b1 = y0 - a1 * x0;
            float b2 = y2 - a0 * x2;
            float b3 = y2 - a1 * x2;

            float c0 = (rect.centerY() - rect.top) / (rect.centerX() - rect.left);
            float c1 = -c0;
            float d0 = rect.top - c0 * rect.left;
            float d1 = rect.top - c1 * rect.right;

            left = Math.max(left, (d0 - b0) / (a0 - c0) < rect.right ? (d0 - b0) / (a0 - c0) : left);
            left = Math.max(left, (d0 - b1) / (a1 - c0) < rect.right ? (d0 - b1) / (a1 - c0) : left);
            left = Math.max(left, (d1 - b3) / (a1 - c1) < rect.right ? (d1 - b3) / (a1 - c1) : left);
            right = Math.min(right, (d1 - b1) / (a1 - c1) > rect.left ? (d1 - b1) / (a1 - c1) : right);
            right = Math.min(right, (d1 - b2) / (a0 - c1) > rect.left ? (d1 - b2) / (a0 - c1) : right);
            right = Math.min(right, (d0 - b2) / (a0 - c0) > rect.left ? (d0 - b2) / (a0 - c0) : right);

            top = Math.max(top, Math.max(a0 * left + b0, a1 * right + b1));
            bottom = Math.min(bottom, Math.min(a1 * left + b3, a0 * right + b2));

            f.left = left;
            f.top = top;
            f.right = right;
            f.bottom = bottom;
            return true;
        }
    }

    private boolean angle() {
        return boundsPoints[0] == boundsPoints[6] || boundsPoints[1] == boundsPoints[7];
    }

    private void cropChanged(boolean inProgress) {
        try {
            if (listener != null) {
                listener.onCropWindowChanged(inProgress);
            }
        } catch (Exception ignored) {
        }
    }

    public interface CropWindowChangeListener {
        void onCropWindowChanged(boolean inProgress);
    }
}
