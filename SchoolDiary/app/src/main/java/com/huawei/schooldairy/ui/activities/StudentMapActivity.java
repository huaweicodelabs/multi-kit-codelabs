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

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions;
import com.huawei.schooldairy.databinding.ActivityStudentMapBinding;
import com.huawei.schooldairy.model.CloudDBZoneWrapper;
import com.huawei.schooldairy.model.Loginmapping;
import com.huawei.schooldairy.model.UserData;
import com.huawei.schooldairy.userutils.Constants;
import com.huawei.schooldairy.userutils.PrefUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Activity for mapping the student with Teacher and insert the mapping details in cloud db
 * @author: Huawei
 * @since: 25-05-2021
 */
public class StudentMapActivity extends AbstractBaseActivity {

    private static final String TAG = "StudentMapActivity";
    private static final int REQUEST_CODE = 999;
    private CloudDBZone mCloudDBZone;

    private CloudDBZoneWrapper mCloudDBZoneWrapper;
    private Handler mHandler;
    ActivityStudentMapBinding binding;
    private String initFrom = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        if (getIntent().hasExtra("initFrom")) {
            initFrom = getIntent().getStringExtra("initFrom");
        }

        /*If this activity called from StudentProfileActivity
        directly move to camera screen for scan
        else
        display screen for initiate the camera screen for scan
        */
        if (initFrom.equals("StudentProfileActivity")) {
            initScanQR();
        } else {
            binding.layScan.setOnClickListener(view -> {
                initScanQR();
            });
        }

    }

    /**
     * Start the camera screen for scan
     */
    private void initScanQR() {
        HmsScanAnalyzerOptions options = new HmsScanAnalyzerOptions.Creator()
                .setHmsScanTypes(HmsScan.QRCODE_SCAN_TYPE, HmsScan.DATAMATRIX_SCAN_TYPE)
                .create();
        ScanUtil.startScan(StudentMapActivity.this, REQUEST_CODE, options);
    }


    /**
     * On result of QR Code scan, pass the JSON object which is
     * obtained from the Teacher's QR code.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) {
            return;
        }
        if (requestCode == REQUEST_CODE) {
            // Input an image for scanning and return the result.
            HmsScan obj = data.getParcelableExtra(ScanUtil.RESULT);
            if (obj != null && !TextUtils.isEmpty(((HmsScan) obj).getOriginalValue())) {
                try {
                    /*QR code Teacher info will be returned in json format*/
                    JSONObject jsonObject = new JSONObject(obj.getOriginalValue());
                    initCloudDB(jsonObject);

                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
            } else {
                Log.e("Error", "Scanned result (null) not available");
            }
        } else {
            Log.e("Error", "Scanned result not available");
        }

    }

    /**
     * Init cloud db wrapper class, on init completion call the insert db operation method
     * @param jsonObject
     */
    private void initCloudDB(JSONObject jsonObject) {
        mCloudDBZoneWrapper = new CloudDBZoneWrapper();
        mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(() -> {
            if (null != AGConnectAuth.getInstance().getCurrentUser()) {
                mCloudDBZoneWrapper.createObjectType();
                mCloudDBZoneWrapper.openCloudDBZoneV2(mCloudDBZone1 -> {
                    this.mCloudDBZone = mCloudDBZone1;
                    try {
                        upsertTeacherDetails(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    /**
     * Insert the Teacher and Student mapping record into the
     * LoginMapping table in cloud DB
     * @param jsonObject
     * @throws JSONException
     */
    public void upsertTeacherDetails(JSONObject jsonObject) throws JSONException {
        if (mCloudDBZone == null) {
            Log.e(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        showProgressDialog("Loading...");

        AGConnectUser user = AGConnectAuth.getInstance().getCurrentUser();
        String teacherId = jsonObject.getString("TeacherID");

        Loginmapping loginmapping = new Loginmapping();
        loginmapping.setStudentID(user.getUid());
        loginmapping.setTeacherID(teacherId);
        loginmapping.setStudentName(user.getDisplayName());
        loginmapping.setStudentEmail(user.getEmail());
        loginmapping.setTeacherEmail(jsonObject.getString("EmailID"));
        loginmapping.setTeacherName(jsonObject.getString("TeacherName"));
        //loginmapping.setUserType(1);
        Date date = new Date();
        loginmapping.setMappedDate(date);

        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(loginmapping);
        upsertTask.addOnSuccessListener(cloudDBZoneResult -> {
            insertUserType(teacherId);
        }).addOnFailureListener(e -> {
            hideDialog();
            Log.e("TAG", "insert_failed " + e.getLocalizedMessage() + " records");
        });
    }


    /**
     * Insert the student record who is mapped with the teacher.
     * @param teacherId
     */
    public void insertUserType(String teacherId) {
        if (mCloudDBZone == null) {
            Log.e(TAG, "CloudDBZone is null, try re-open it");
            return;
        }

        AGConnectUser user = AGConnectAuth.getInstance().getCurrentUser();
        UserData userData = new UserData();
        userData.setUserID(user.getUid());
        userData.setUserName(user.getDisplayName());
        userData.setUserType(String.valueOf(Constants.USER_STUDENT));
        userData.setTeacherId(teacherId);

        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(userData);
        upsertTask.addOnSuccessListener(cloudDBZoneResult -> {
            hideDialog();
            Toast.makeText(StudentMapActivity.this, "Student Registered and Mapped.", Toast.LENGTH_SHORT).show();
            if (!initFrom.equals("StudentProfileActivity")) {
                PrefUtil.getInstance(this).setInt("USER_TYPE", Constants.USER_STUDENT);
                PrefUtil.getInstance(this).setBool("IS_MAPPED", true);
                startActivity(new Intent(StudentMapActivity.this, HomeActivity.class));
            }
            finish();
        });

        upsertTask.addOnFailureListener(e -> {
            hideDialog();
            Log.e(TAG, "insert_failed " + e.getLocalizedMessage() + " records");
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCloudDBZoneWrapper != null)
            mCloudDBZoneWrapper.closeCloudDBZone();
    }


}
