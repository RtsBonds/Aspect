package com.android.aspect.handler;

import android.graphics.RectF;

import com.android.aspect.CropOptions;
import com.android.aspect.ImageViewCrop;

public final class CropHandler {

    private final RectF rectF = new RectF();

    private final RectF f = new RectF();

    private float minCroPWidth;

    private float minCropHeight;

    private float maxCropWidth;

    private float maxCropHeight;

    private float minResultWidth;

    private float minResultHeight;

    private float maxResultWidth;

    private float maxResultHeight;

    private float scaleWidth = 1;

    private float scaleHeight = 1;

    public RectF getRect() {
        f.set(rectF);
        return f;
    }

    public float getMinWidth() {
        return Math.max(minCroPWidth, minResultWidth / scaleWidth);
    }

    public float getMinHeight() {
        return Math.max(minCropHeight, minResultHeight / scaleHeight);
    }

    public float getMaxWidth() {
        return Math.min(maxCropWidth, maxResultWidth / scaleWidth);
    }

    public float getMaxHeight() {
        return Math.min(maxCropHeight, maxResultHeight / scaleHeight);
    }

    public float getWidth() {
        return scaleWidth;
    }

    public float getHeight() {
        return scaleHeight;
    }

    public void setCropLimits(
            float maxWidth, float maxHeight, float scaleFactorWidth, float scaleFactorHeight) {
        maxCropWidth = maxWidth;
        maxCropHeight = maxHeight;
        scaleWidth = scaleFactorWidth;
        scaleHeight = scaleFactorHeight;
    }

    public void setValues(CropOptions options) {
        minCroPWidth = options.minCropWindowWidth;
        minCropHeight = options.minCropWindowHeight;
        minResultWidth = options.minCropResultWidth;
        minResultHeight = options.minCropResultHeight;
        maxResultWidth = options.maxCropResultWidth;
        maxResultHeight = options.maxCropResultHeight;
    }

    public void rect(RectF rect) {
        rectF.set(rect);
    }

    public boolean guidelines() {
        return !(rectF.width() < 100 || rectF.height() < 100);
    }

    public MoveHandler moveHandler(
            float x, float y, float targetRadius, ImageViewCrop.CropShape cropShape) {
        MoveHandler.Type type =
                cropShape == ImageViewCrop.CropShape.OVAL
                        ? moveType(x, y)
                        : getMoveType(x, y, targetRadius);
        return type != null ? new MoveHandler(type, this, x, y) : null;
    }


    private MoveHandler.Type getMoveType(
            float x, float y, float targetRadius) {
        MoveHandler.Type moveType = null;
        if (CropHandler.corner(x, y, rectF.left, rectF.top, targetRadius)) {
            moveType = MoveHandler.Type.TOP_LEFT;
        } else if (CropHandler.corner(
                x, y, rectF.right, rectF.top, targetRadius)) {
            moveType = MoveHandler.Type.TOP_RIGHT;
        } else if (CropHandler.corner(
                x, y, rectF.left, rectF.bottom, targetRadius)) {
            moveType = MoveHandler.Type.BOTTOM_LEFT;
        } else if (CropHandler.corner(
                x, y, rectF.right, rectF.bottom, targetRadius)) {
            moveType = MoveHandler.Type.BOTTOM_RIGHT;
        } else if (CropHandler.centerTar(
                x, y, rectF.left, rectF.top, rectF.right, rectF.bottom)
                && center()) {
            moveType = MoveHandler.Type.CENTER;
        } else if (CropHandler.horizontal(
                x, y, rectF.left, rectF.right, rectF.top, targetRadius)) {
            moveType = MoveHandler.Type.TOP;
        } else if (CropHandler.horizontal(
                x, y, rectF.left, rectF.right, rectF.bottom, targetRadius)) {
            moveType = MoveHandler.Type.BOTTOM;
        } else if (CropHandler.vertical(
                x, y, rectF.left, rectF.top, rectF.bottom, targetRadius)) {
            moveType = MoveHandler.Type.LEFT;
        } else if (CropHandler.vertical(
                x, y, rectF.right, rectF.top, rectF.bottom, targetRadius)) {
            moveType = MoveHandler.Type.RIGHT;
        } else if (CropHandler.centerTar(
                x, y, rectF.left, rectF.top, rectF.right, rectF.bottom)
                && !center()) {
            moveType = MoveHandler.Type.CENTER;
        }

        return moveType;
    }

    private MoveHandler.Type moveType(float x, float y) {
        float cellLength = rectF.width() / 6;
        float leftCenter = rectF.left + cellLength;
        float rightCenter = rectF.left + (5 * cellLength);

        float cellHeight = rectF.height() / 6;
        float topCenter = rectF.top + cellHeight;
        float bottomCenter = rectF.top + 5 * cellHeight;

        MoveHandler.Type moveType;
        if (x < leftCenter) {
            if (y < topCenter) {
                moveType = MoveHandler.Type.TOP_LEFT;
            } else if (y < bottomCenter) {
                moveType = MoveHandler.Type.LEFT;
            } else {
                moveType = MoveHandler.Type.BOTTOM_LEFT;
            }
        } else if (x < rightCenter) {
            if (y < topCenter) {
                moveType = MoveHandler.Type.TOP;
            } else if (y < bottomCenter) {
                moveType = MoveHandler.Type.CENTER;
            } else {
                moveType = MoveHandler.Type.BOTTOM;
            }
        } else {
            if (y < topCenter) {
                moveType = MoveHandler.Type.TOP_RIGHT;
            } else if (y < bottomCenter) {
                moveType = MoveHandler.Type.RIGHT;
            } else {
                moveType = MoveHandler.Type.BOTTOM_RIGHT;
            }
        }

        return moveType;
    }

    private static boolean corner(
            float x, float y, float handleX, float handleY, float targetRadius) {
        return Math.abs(x - handleX) <= targetRadius && Math.abs(y - handleY) <= targetRadius;
    }

    private static boolean horizontal(
            float x, float y, float handleXStart, float handleXEnd, float handleY, float targetRadius) {
        return x > handleXStart && x < handleXEnd && Math.abs(y - handleY) <= targetRadius;
    }

    private static boolean vertical(
            float x, float y, float handleX, float handleYStart, float handleYEnd, float targetRadius) {
        return Math.abs(x - handleX) <= targetRadius && y > handleYStart && y < handleYEnd;
    }

    private static boolean centerTar(
            float x, float y, float left, float top, float right, float bottom) {
        return x > left && x < right && y > top && y < bottom;
    }

    private boolean center() {
        return !guidelines();
    }
}
