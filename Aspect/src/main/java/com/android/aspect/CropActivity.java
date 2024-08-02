package com.android.aspect;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.android.aspect.ImageCrop;
import com.android.aspect.ImageViewCrop;
import java.io.File;
import java.io.IOException;

public class CropActivity extends AppCompatActivity
        implements ImageViewCrop.OnSetImageUriCompleteListener,
        ImageViewCrop.OnCropImageCompleteListener {
    private ImageViewCrop viewCrop;
    private Uri uri;
    private CropOptions options;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.crop_activity);

        findViewById(R.id.close).setOnClickListener(v -> setResultCancel());
        findViewById(R.id.next).setOnClickListener(view -> cropImage());

        viewCrop = findViewById(R.id.cropImageView);
        Bundle bundle = getIntent().getBundleExtra(ImageCrop.BUNDLE);
        assert bundle != null;
        uri = bundle.getParcelable(ImageCrop.SOURCE);
        options = bundle.getParcelable(ImageCrop.OPTIONS);
        if (savedInstanceState == null) {
            if (uri == null || uri.equals(Uri.EMPTY)) {
                ImageCrop.getImageActivity(this);
            } else {
                viewCrop.setImageUriAsync(uri);
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        viewCrop.setOnSetImageUriCompleteListener(this);
        viewCrop.setOnCropImageCompleteListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        viewCrop.setOnSetImageUriCompleteListener(null);
        viewCrop.setOnCropImageCompleteListener(null);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResultCancel();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ImageCrop.REQUEST_CODE) {
            if (resultCode == Activity.RESULT_CANCELED) {
                setResultCancel();
            }
            if (resultCode == Activity.RESULT_OK) {
                uri = ImageCrop.getImageResult(this, data);
                viewCrop.setImageUriAsync(uri);
            }
        }
    }

    @Override
    public void onSetImageUriComplete(ImageViewCrop view, Uri uri, Exception error) {
        if (error == null) {
            if (options.rect != null) {
                viewCrop.setCropRect(options.rect);
            }
        } else {
            setResult(null, error, 1);
        }
    }

    @Override
    public void onCropImageComplete(ImageViewCrop view, ImageViewCrop.CropResult result) {
        setResult(result.getUri(), result.getError(), result.getSampleSize());
    }

    protected void cropImage() {
        if (options.noOutputImage) {
            setResult(null, null, 1);
        } else {
            Uri outputUri = getOutputUri();
            viewCrop.saveCroppedImage(
                    outputUri,
                    options.compressFormat,
                    options.outputQuality,
                    options.outputRequestWidth,
                    options.outputRequestHeight,
                    options.sizeOptions);
        }
    }

    protected Uri getOutputUri() {
        Uri outputUri = options.outputUri;
        if (outputUri == null || outputUri.equals(Uri.EMPTY)) {
            try {
                String ext =
                        options.compressFormat == Bitmap.CompressFormat.JPEG
                                ? ".jpg"
                                : options.compressFormat == Bitmap.CompressFormat.PNG ? ".png" : ".webp";
                outputUri = Uri.fromFile(File.createTempFile("cropped", ext, getCacheDir()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return outputUri;
    }

    protected void setResult(Uri uri, Exception error, int sampleSize) {
        int resultCode = error == null ? RESULT_OK : ImageCrop.RESULT_ERROR_CODE;
        setResult(resultCode, getResultIntent(uri, error, sampleSize));
        finish();
    }

    protected void setResultCancel() {
        setResult(RESULT_CANCELED);
        finish();
    }

    protected Intent getResultIntent(Uri uri, Exception error, int sampleSize) {
        ImageCrop.ActivityResult result =
                new ImageCrop.ActivityResult(
                        viewCrop.getImageUri(),
                        uri,
                        error,
                        viewCrop.getCropPoints(),
                        viewCrop.getCropRect(),
                        viewCrop.getRotatedDegrees(),
                        viewCrop.getWholeImageRect(),
                        sampleSize);
        Intent intent = new Intent();
        intent.putExtras(getIntent());
        intent.putExtra(ImageCrop.EXTRA_RESULT, result);
        return intent;
    }
}
