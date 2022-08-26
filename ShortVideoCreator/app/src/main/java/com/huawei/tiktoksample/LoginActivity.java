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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.SignInResult;
import com.huawei.agconnect.cloud.storage.core.StorageReference;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.request.AccountAuthParams;
import com.huawei.hms.support.account.request.AccountAuthParamsHelper;
import com.huawei.hms.support.account.result.AuthAccount;
import com.huawei.hms.support.account.service.AccountAuthService;
import com.huawei.tiktoksample.db.clouddb.User;
import com.huawei.tiktoksample.model.Constants;
import com.huawei.tiktoksample.util.LoginHelper;
import com.huawei.tiktoksample.viewmodel.TikTokSampleViewModel;
import com.huawei.tiktoksample.viewmodel.UserProfile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class LoginActivity extends AppCompatActivity implements LoginHelper.OnLoginEventCallBack {

    private static final String MY_PREFS_NAME = "MyPrefsFile";
    private UserProfile userProfile;
    private ImageView userImage;
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    public static int profileImagePage=1;
    private TikTokSampleViewModel userProfileImage;
    private Bitmap profileFhotoBitmap;
    private AlertDialog alert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button btnSignup = findViewById(R.id.btnSignup);
        userProfileImage = new ViewModelProvider(this).get(TikTokSampleViewModel.class);
        btnSignup.setOnClickListener(v -> signup());
        Handler mHandler = new Handler(Looper.getMainLooper());
        if (AGConnectAuth.getInstance().getCurrentUser() == null) {
            mHandler.post(() -> {
                LoginHelper loginHelper = new LoginHelper(LoginActivity.this);
                loginHelper.addLoginCallBack(this);
                loginHelper.login();
            });
        }
        userProfile = new ViewModelProvider(LoginActivity.this).get(UserProfile.class);
        userProfile.userMutableLiveData.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(aBoolean) {
                    SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putInt("count", 1);
                    editor.apply();
                    Intent myIntent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(myIntent);
                    finish();
                }
            }
        });
        Intent intent = getIntent();
        profileFhotoBitmap = (Bitmap) intent.getParcelableExtra("NewBitmapImage");
        if(null !=profileFhotoBitmap) {
            SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
            String email = prefs.getString("email", "0");
            String mobile = prefs.getString("mobile", "0");
            String name = prefs.getString("name", "0");
            getProfileDialog(email,mobile,name);
        }
    }
    private void signup(){
        try {
            AccountAuthParams mAuthParam = new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                    .setIdToken()
                    .setAccessToken()
                    .createParams();
            AccountAuthService mAuthManager = AccountAuthManager.getService(LoginActivity.this, mAuthParam);
            startActivityForResult(mAuthManager.getSignInIntent(), Constants.REQUEST_SIGN_IN_LOGIN);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Uri imageUri = data.getData();
            profileFhotoBitmap = (Bitmap) data.getExtras().get("data");
            userImage.setImageBitmap(profileFhotoBitmap);
            Intent myIntent = new Intent(LoginActivity.this, EditImageActivity.class);
            myIntent.putExtra("BitmapImage",  profileFhotoBitmap);
            startActivity(myIntent);
        }
        if (requestCode == Constants.REQUEST_SIGN_IN_LOGIN) {
            Task<AuthAccount> authAccountTask = AccountAuthManager.parseAuthResultFromIntent(data);
            if (authAccountTask.isSuccessful()) {
                AuthAccount authAccount = authAccountTask.getResult();
                if(null !=authAccount.getEmail()) {
                    if (authAccount.getEmail().equalsIgnoreCase("0") || authAccount.getDisplayName().equalsIgnoreCase("0")) {
                        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                        editor.putString("email", authAccount.getEmail());
                        editor.putString("mobile", authAccount.getDisplayName());
                        editor.putString("name", authAccount.getUid());
                        editor.putInt("login", 1);
                        editor.putInt("count", 1);
                        editor.apply();
                        getProfileDialog(authAccount.getEmail(), authAccount.getDisplayName(), authAccount.getUid());
                    }
                }
                else{
                    SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putString("email", authAccount.getEmail());
                    editor.putString("mobile", authAccount.getDisplayName());
                    editor.putString("name", authAccount.getUid());
                    editor.putInt("login", 1);
                    editor.putInt("count", 1);
                    editor.apply();
                    getProfileDialog("", authAccount.getDisplayName(), authAccount.getUid());
                }
            } else {
                Log.i(Constants.APP_TAG, "signIn failed: " + ((ApiException) authAccountTask.getException()).getStatusCode());
            }
        }
    }
    public void getProfileDialog(String email, String mobile, String uId) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        final View customLayout = getLayoutInflater().inflate(R.layout.profile_alert, null);
        alertDialog.setView(customLayout);
        alert = alertDialog.create();
        alert.setCancelable(false);
        alert.setCanceledOnTouchOutside(false);
        EditText etEmail = customLayout.findViewById(R.id.etEmail);
        EditText etMobile = customLayout.findViewById(R.id.etMobile);
        Button submitBtn = customLayout.findViewById(R.id.btnSubmit);
        userImage= (ImageView) customLayout.findViewById(R.id.userImage);
        ImageView editIcon = (ImageView) customLayout.findViewById(R.id.editIcon);
        if(!email.equalsIgnoreCase("")){
        }
        if(!mobile.equalsIgnoreCase("0")){
        }
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String image = prefs.getString("image", "0");
        if(null !=image) {
            if(!image.equalsIgnoreCase("0")) {
                byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                userImage.setImageBitmap(decodedByte);
            }
        }
        Intent intent = getIntent();
        Bitmap bitmap = (Bitmap) intent.getParcelableExtra("NewBitmapImage");
        if(null !=bitmap) {
            userImage.setImageBitmap(bitmap);
        }
        userImage.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                }
                else {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
            }
        });
        editIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userImage.invalidate();
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                }
                else {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
            }
        });
        submitBtn.setOnClickListener(v -> {
            if (!etEmail.getText().toString().equalsIgnoreCase("")) {
                if (!etMobile.getText().toString().equalsIgnoreCase("")) {
                    alert.dismiss();
                    SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
                    editor.putString("email", etEmail.getText().toString());
                    editor.putString("mobile", etMobile.getText().toString());
                    editor.putString("name", uId);
                    editor.putInt("login", 1);
                    editor.putInt("count", 1);
                    editor.apply();
                    User user= new User();
                    user.setUserEmail(etEmail.getText().toString());
                    user.setUserName(etEmail.getText().toString());
                    user.setUserPhone(etMobile.getText().toString());
                    user.setUserId((int) System.currentTimeMillis());
                    user.setUserProfilePic("");
                    user.setUserShadowFlag(true);
                    if(null !=profileFhotoBitmap) {
                        uploadImage(bitmapToFile(this, profileFhotoBitmap, etMobile.getText().toString() + ".png"), etMobile.getText().toString() + ".png", user);
                    }
                    else{
                        updateProfileData(user);
                    }
                } else {
                    Toast.makeText(this,"Please Enter Mobile Number", Toast.LENGTH_LONG).show();
                }
            }
            else{
                Toast.makeText(this,"Please Enter Email ID", Toast.LENGTH_LONG).show();
            }
        });
        alert.show();
    }
    public void uploadImage(File path, String filename, User user) {
        userProfileImage.uploadFileLiveData().observe(LoginActivity.this, uri -> {
            user.setUserProfilePic(uri.toString());
            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
            editor.putString("profileImageURL", uri.toString());
            editor.apply();
            updateProfileData(user);
        });
        final StorageReference storageReference = TikTokSampleApplication.getStorageManagement().getStorageReference("tiktoksample/user_profile/" + "7703849473.png");
        userProfileImage.uploadFile(storageReference,
                filename, path,
                (errorMessage, e) -> Log.d("ProfileFragment", "filePAth--->Error" + e));
    }
    public void updateProfileData(User user) {
        userProfile.saveUser(user, this);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
            else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
    public static File bitmapToFile(Context context, Bitmap bitmap, String fileNameToSave) {
        File file = null;
        try {
            file = new File(Environment.getExternalStorageDirectory() + File.separator + fileNameToSave);
            file.createNewFile();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
            byte[] bitmapdata = bos.toByteArray();
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return file;
        }
    }

    @Override
    public void onLogin(boolean showLoginUserInfo, SignInResult signInResult) {
    }

    @Override
    public void onLogOut(boolean showLoginUserInfo) {
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }
}