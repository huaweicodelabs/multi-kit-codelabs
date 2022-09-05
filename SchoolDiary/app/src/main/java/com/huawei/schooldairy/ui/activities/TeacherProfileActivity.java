/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2022. All rights reserved.
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
package com.huawei.schooldairy.ui.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.hmsscankit.WriterException;
import com.huawei.hms.ml.scan.HmsBuildBitmapOption;
import com.huawei.hms.ml.scan.HmsScan;
import com.huawei.schooldairy.R;
import com.huawei.schooldairy.databinding.ActivityTeacherProfileBinding;

/**
 * Activity for view the Teacher profile/information and QR Code for mapping
 * @author: Huawei
 * @since: 25-05-2021
 */
public class TeacherProfileActivity extends AbstractBaseActivity {

    public static final String TAG = "TeacherProfileActivity";
    private Bitmap qrBitmap;
    ActivityTeacherProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTeacherProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        showBackArrow();
    }

    /**
     * Initiate views, Generate and Display QR Code with Teachers detail
     * which can be scan by Student and mapped with this Teacher.
     * @param savedInstanceState
     */
    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        AGConnectUser user = AGConnectAuth.getInstance().getCurrentUser();
        String content = "{\"TeacherID\":\"" + user.getUid() + "\"," +
                "\"TeacherName\":\"" + user.getDisplayName() + "\"," +
                "\"EmailID\":\"" + user.getEmail() + "\"}";

        binding.txtTeacherName.setText(user.getDisplayName());
        binding.txtTeacherId.setText((user.getEmail() == null) ? "" : user.getEmail());

        int type = HmsScan.QRCODE_SCAN_TYPE;
        int width = 400;
        int height = 400;

        HmsBuildBitmapOption options = new HmsBuildBitmapOption.Creator().setBitmapMargin(3).create();
        try {
            // If the HmsBuildBitmapOption object is not constructed, set options to null.
            qrBitmap = ScanUtil.buildBitmap(content, type, width, height, options);
            ((ImageView) findViewById(R.id.img_teacher_qr)).setImageBitmap(qrBitmap);
        } catch (WriterException e) {
            Log.w("buildBitmap", e);
        }
    }
}