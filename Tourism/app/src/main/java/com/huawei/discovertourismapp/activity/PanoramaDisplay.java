package com.huawei.discovertourismapp.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.discovertourismapp.R;
import com.huawei.hms.panorama.Panorama;
import com.huawei.hms.panorama.PanoramaInterface;
import com.huawei.hms.support.api.client.ResultCallback;

import java.io.ByteArrayOutputStream;

public class PanoramaDisplay extends AppCompatActivity {
    byte[] byteArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panorama);
        byteArray = getIntent().getByteArrayExtra("bitmap");
        Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        Panorama.getInstance().loadImageInfoWithPermission(
                        this, getImageUri(this, bmp), PanoramaInterface.IMAGE_TYPE_SPHERICAL)
                .setResultCallback(new ResultCallbackImpl());

    }

    private class ResultCallbackImpl implements ResultCallback<PanoramaInterface.ImageInfoResult> {
        @Override
        public void onResult(PanoramaInterface.ImageInfoResult panoramaResult) {
            if (panoramaResult == null) {
                logAndToast("panoramaResult is null");
                return;
            }

            if (panoramaResult.getStatus().isSuccess()) {
                Intent intent = panoramaResult.getImageDisplayIntent();
                if (intent != null) {
                    startActivity(intent);
                } else {
                    logAndToast("unknown error, view intent is null");
                }
            } else {
                logAndToast("error status : " + panoramaResult.getStatus());
            }
        }
    }

    private void logAndToast(String message) {
        Toast.makeText(PanoramaDisplay.this, message, Toast.LENGTH_LONG).show();
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}
