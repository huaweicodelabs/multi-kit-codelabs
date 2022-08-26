/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.tiktoksample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hms.image.vision.crop.CropLayoutView;

import java.io.ByteArrayOutputStream;

/**
 * The type Crop activity.
 *
 * @author huawei
 * @since 1.0.3.300
 */
public class EditImageActivity extends AppCompatActivity implements View.OnClickListener {
    private Button btnDone;
    private CropLayoutView cropLayoutView;
    private RadioButton rbCircular;
    public static final String MY_PREFS_NAME = "MyPrefsFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        cropLayoutView = findViewById(R.id.cropImageView);
        Button cropImage = findViewById(R.id.btn_crop_image);
        Button rotate = findViewById(R.id.btn_rotate);
        Button flipH = findViewById(R.id.btn_flip_horizontally);
        Button flipV = findViewById(R.id.btn_flip_vertically);
        btnDone = (Button) findViewById(R.id.btnDone);
        btnDone.setEnabled(false);
        cropLayoutView.setAutoZoomEnabled(true);
        cropLayoutView.setCropShape(CropLayoutView.CropShape.RECTANGLE);
        cropImage.setOnClickListener(this);
        rotate.setOnClickListener(this);
        flipH.setOnClickListener(this);
        flipV.setOnClickListener(this);
        rbCircular = findViewById(R.id.rb_circular);
        RadioGroup rgCrop = findViewById(R.id.rb_crop);
        Intent intent = getIntent();
        Bitmap bitmap = (Bitmap) intent.getParcelableExtra("BitmapImage");
        rgCrop.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                RadioButton radioButton = radioGroup.findViewById(i);
                if (radioButton.equals(rbCircular)) {
                    cropLayoutView.setCropShape(CropLayoutView.CropShape.OVAL);
                } else {
                    cropLayoutView.setCropShape(CropLayoutView.CropShape.RECTANGLE);
                }
            }
        });
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap croppedImage = cropLayoutView.getCroppedImage();


                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                croppedImage.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream .toByteArray();
                String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
                SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString("image", encoded);
                editor.apply();
                if(LoginActivity.profileImagePage==1){
                    Intent myIntent = new Intent(EditImageActivity.this, LoginActivity.class);
                    myIntent.putExtra("NewBitmapImage", croppedImage);
                    startActivity(myIntent);
                }
                else {
                    Intent myIntent = new Intent(EditImageActivity.this, MainActivity.class);
                    myIntent.putExtra("NewBitmapImage", croppedImage);
                    startActivity(myIntent);
                }
            }
        });
        Spinner spinner = (Spinner) findViewById(R.id.spinner1);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String[] ratios = getResources().getStringArray(R.array.ratios);
                try {
                    int ratioX = Integer.parseInt(ratios[pos].split(":")[0]);
                    int ratioY = Integer.parseInt(ratios[pos].split(":")[1]);
                    cropLayoutView.setAspectRatio(ratioX, ratioY);
                } catch (Exception e) {
                    cropLayoutView.setFixedAspectRatio(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        RadioButton rbRectangle = findViewById(R.id.rb_rectangle);
        cropLayoutView.setImageBitmap(bitmap);


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_crop_image:
                btnDone.setEnabled(true);
                Bitmap croppedImage = cropLayoutView.getCroppedImage();
                cropLayoutView.setImageBitmap(croppedImage);
                break;
            case R.id.btn_rotate:
                btnDone.setEnabled(true);
                cropLayoutView.rotateClockwise();
                break;
            case R.id.btn_flip_horizontally:
                btnDone.setEnabled(true);
                cropLayoutView.flipImageHorizontally();
                break;
            case R.id.btn_flip_vertically:
                btnDone.setEnabled(true);
                cropLayoutView.flipImageVertically();
                break;
        }
    }
}
