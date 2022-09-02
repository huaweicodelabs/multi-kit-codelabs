package com.huawei.discovertourismapp.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.huawei.discovertourismapp.MainActivity;
import com.huawei.discovertourismapp.R;
import com.huawei.discovertourismapp.utils.Constants;
import com.huawei.discovertourismapp.utils.TourismSharedPref;

public class SplashScreenActivity extends AppCompatActivity {
    private final int SPLASH_DISPLAY_LENGTH = 1000;
    private static final int REQUEST_CODE = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_homedecor_splah);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        if (!checkPermissions_Camera()) {
            verifyPermissions();
        } else {
            navigatetonext();
        }


    }

    private void navigatetonext() {
        new Handler().postDelayed(() -> proceedtoNext(), SPLASH_DISPLAY_LENGTH);
    }

    private void proceedtoNext() {
        TourismSharedPref.initializeInstance(SplashScreenActivity.this);
        if (TourismSharedPref.getInstance().getString(Constants.ALREADY_LOGIN, "").equalsIgnoreCase("1")) {
            startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
        } else {
            startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
        }
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {//2909: {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                navigatetonext();
            } else {
            }
            return;
        }
    }

    private boolean checkPermissions_Camera() {
        int permissionState = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    public void verifyPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && getApplicationContext().checkSelfPermission(
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED

                && getApplicationContext().checkSelfPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED

                && getApplicationContext().checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED

                && getApplicationContext().checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED

                && getApplicationContext().checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
                            , Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE);
        }
    }


}
