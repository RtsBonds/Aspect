package com.android.aspect;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class CropOptions implements Parcelable {
    public static final Creator<CropOptions> CREATOR =
            new Creator<CropOptions>() {
                @Override
                public CropOptions createFromParcel(Parcel in) {
                    return new CropOptions(in);
                }
                @Override
                public CropOptions[] newArray(int size) {
                    return new CropOptions[size];
                }
            };

    public ImageViewCrop.CropShape shape;

    public float radius;

    public float touchRadius;

    public ImageViewCrop.Guidelines guidelines;

    public ImageViewCrop.ScaleType scaleType;

    public boolean Overlay;

    public float CropRatio;

    public boolean fixAspectRatio;

    public int aspectRatioX;

    public int aspectRatioY;

    public float borderLineThickness;

    public int borderLineColor;

    public float borderCornerThickness;

    public float borderCornerOffset;

    public float borderCornerLength;

    public int borderCornerColor;

    public float guidelinesThickness;

    public int guidelinesColor;

    public int backgroundColor;

    public int minCropWindowWidth;

    public int minCropWindowHeight;

    public int minCropResultWidth;


    public int minCropResultHeight;

    public int maxCropResultWidth;


    public int maxCropResultHeight;

    public Uri outputUri;

    public Bitmap.CompressFormat compressFormat;

    public int outputQuality;

    public int outputRequestWidth;

    public int outputRequestHeight;

    public ImageViewCrop.RequestSizeOptions sizeOptions;

    public boolean noOutputImage;

    public Rect rect;

    public int anInt;

    public boolean aBoolean;

    public boolean aBoolean1;

    public boolean aBoolean2;

    public int anInt1;

    public boolean aBoolean3;

    public boolean vertically;

    public CharSequence sequence;

    public CropOptions() {

        DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();

        shape = ImageViewCrop.CropShape.RECTANGLE;
        radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, dm);
        touchRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, dm);
        guidelines = ImageViewCrop.Guidelines.ON;
        scaleType = ImageViewCrop.ScaleType.FIT_CENTER;
        Overlay = true;
        CropRatio = 0.1f;

        fixAspectRatio = true;
        aspectRatioX = 1;
        aspectRatioY = 1;

        borderLineThickness = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0.6F, dm);
        borderLineColor = Color.WHITE;
        borderCornerThickness = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.5F, dm);
        borderCornerOffset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, dm);
        borderCornerLength = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, dm);
        borderCornerColor = Color.WHITE;

        guidelinesThickness = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0.5f, dm);
        guidelinesColor = Color.WHITE;
        backgroundColor = Color.argb(119, 0, 0, 0);

        minCropWindowWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 42, dm);
        minCropWindowHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 42, dm);
        minCropResultWidth = 40;
        minCropResultHeight = 40;
        maxCropResultWidth = 99999;
        maxCropResultHeight = 99999;

        outputUri = Uri.EMPTY;
        compressFormat = Bitmap.CompressFormat.JPEG;
        outputQuality = 90;
        outputRequestWidth = 0;
        outputRequestHeight = 0;
        sizeOptions = ImageViewCrop.RequestSizeOptions.NONE;
        noOutputImage = false;

        rect = null;
        anInt = -1;
        aBoolean = true;
        aBoolean1 = true;
        aBoolean2 = false;
        anInt1 = 90;
        aBoolean3 = false;
        vertically = false;
        sequence = null;
    }

    protected CropOptions(Parcel in) {
        shape = ImageViewCrop.CropShape.values()[in.readInt()];
        radius = in.readFloat();
        touchRadius = in.readFloat();
        guidelines = ImageViewCrop.Guidelines.values()[in.readInt()];
        scaleType = ImageViewCrop.ScaleType.values()[in.readInt()];
        Overlay = in.readByte() != 0;
        CropRatio = in.readFloat();
        fixAspectRatio = in.readByte() != 0;
        aspectRatioX = in.readInt();
        aspectRatioY = in.readInt();
        borderLineThickness = in.readFloat();
        borderLineColor = in.readInt();
        borderCornerThickness = in.readFloat();
        borderCornerOffset = in.readFloat();
        borderCornerLength = in.readFloat();
        borderCornerColor = in.readInt();
        guidelinesThickness = in.readFloat();
        guidelinesColor = in.readInt();
        backgroundColor = in.readInt();
        minCropWindowWidth = in.readInt();
        minCropWindowHeight = in.readInt();
        minCropResultWidth = in.readInt();
        minCropResultHeight = in.readInt();
        maxCropResultWidth = in.readInt();
        maxCropResultHeight = in.readInt();
        outputUri = in.readParcelable(Uri.class.getClassLoader());
        compressFormat = Bitmap.CompressFormat.valueOf(in.readString());
        outputQuality = in.readInt();
        outputRequestWidth = in.readInt();
        outputRequestHeight = in.readInt();
        sizeOptions = ImageViewCrop.RequestSizeOptions.values()[in.readInt()];
        noOutputImage = in.readByte() != 0;
        rect = in.readParcelable(Rect.class.getClassLoader());
        anInt = in.readInt();
        aBoolean = in.readByte() != 0;
        aBoolean1 = in.readByte() != 0;
        aBoolean2 = in.readByte() != 0;
        anInt1 = in.readInt();
        aBoolean3 = in.readByte() != 0;
        vertically = in.readByte() != 0;
        sequence = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(shape.ordinal());
        dest.writeFloat(radius);
        dest.writeFloat(touchRadius);
        dest.writeInt(guidelines.ordinal());
        dest.writeInt(scaleType.ordinal());
        dest.writeByte((byte) (Overlay ? 1 : 0));
        dest.writeFloat(CropRatio);
        dest.writeByte((byte) (fixAspectRatio ? 1 : 0));
        dest.writeInt(aspectRatioX);
        dest.writeInt(aspectRatioY);
        dest.writeFloat(borderLineThickness);
        dest.writeInt(borderLineColor);
        dest.writeFloat(borderCornerThickness);
        dest.writeFloat(borderCornerOffset);
        dest.writeFloat(borderCornerLength);
        dest.writeInt(borderCornerColor);
        dest.writeFloat(guidelinesThickness);
        dest.writeInt(guidelinesColor);
        dest.writeInt(backgroundColor);
        dest.writeInt(minCropWindowWidth);
        dest.writeInt(minCropWindowHeight);
        dest.writeInt(minCropResultWidth);
        dest.writeInt(minCropResultHeight);
        dest.writeInt(maxCropResultWidth);
        dest.writeInt(maxCropResultHeight);
        dest.writeParcelable(outputUri, flags);
        dest.writeString(compressFormat.name());
        dest.writeInt(outputQuality);
        dest.writeInt(outputRequestWidth);
        dest.writeInt(outputRequestHeight);
        dest.writeInt(sizeOptions.ordinal());
        dest.writeInt(noOutputImage ? 1 : 0);
        dest.writeParcelable(rect, flags);
        dest.writeInt(anInt);
        dest.writeByte((byte) (aBoolean ? 1 : 0));
        dest.writeByte((byte) (aBoolean1 ? 1 : 0));
        dest.writeByte((byte) (aBoolean2 ? 1 : 0));
        dest.writeInt(anInt1);
        dest.writeByte((byte) (aBoolean3 ? 1 : 0));
        dest.writeByte((byte) (vertically ? 1 : 0));
        TextUtils.writeToParcel(sequence, dest, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

}
