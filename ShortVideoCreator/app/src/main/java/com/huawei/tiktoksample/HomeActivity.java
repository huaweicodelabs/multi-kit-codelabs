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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.SignInResult;
import com.huawei.tiktoksample.db.clouddb.VideoComments;
import com.huawei.tiktoksample.ui.HomeFragment;
import com.huawei.tiktoksample.util.LoginHelper;
import com.huawei.tiktoksample.util.OnApiError;
import com.huawei.tiktoksample.viewmodel.TikTokSampleViewModel;

public class HomeActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener, LoginHelper.OnLoginEventCallBack {
    private BottomNavigationView mBtmView;
    public String videoUrl;
    public String videName;
    public String userName;
    public String userMobile;
    public String userEmail;
    public int noOfComments;
    public int noOfLikes;
    public int intentValue=0;
    public static final String MY_PREFS_NAME = "MyPrefsFile";
    private String profileImage;
    private String currentUserEmail;
    private TikTokSampleViewModel tikTokSampleViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        tikTokSampleViewModel = new ViewModelProvider(this).get(TikTokSampleViewModel.class);
        setSharePref();
        getSharePref();
        mBtmView = (BottomNavigationView) findViewById(R.id.nav_view);
        AppCompatImageView addIconImage = (AppCompatImageView) findViewById(R.id.image_view_add_icon);
        mBtmView.setOnNavigationItemSelectedListener(this);
        mBtmView.getMenu().findItem(R.id.navigation_home).setChecked(true);
        View view = mBtmView.findViewById(R.id.navigation_home);
        view.performClick();
        view.performClick();
        Log.i("click new","click");
        Handler mHandler = new Handler(Looper.getMainLooper());
        if (AGConnectAuth.getInstance().getCurrentUser() == null) {
            mHandler.post(() -> {
                LoginHelper loginHelper = new LoginHelper(HomeActivity.this);
                loginHelper.addLoginCallBack(this);
                loginHelper.login();
            });
        } else {

        }
        Intent newIntent = getIntent();
        if(null != newIntent) {
            if(null != newIntent.getExtras()) {
                intentValue = 1;
                videoUrl = newIntent.getStringExtra("VideoUrl");
                videName = newIntent.getStringExtra("VideoName");
                userName = newIntent.getStringExtra("UserName");
                userMobile = newIntent.getStringExtra("UserMobile");
                userEmail = newIntent.getStringExtra("UserEmail");
                noOfComments = newIntent.getIntExtra("NoOfComments", 0);
                noOfLikes = newIntent.getIntExtra("NoOfLikes", 0);
            }
        }

        addIconImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(HomeActivity.this, VideoEditActivity.class);
                startActivity(myIntent);
            }
        });
        tikTokSampleViewModel.insertCommentForVideo().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {

            }
        });

        tikTokSampleViewModel.updateLikeForVideo().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {

            }
        });
        HomeFragment fragment = new HomeFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content, fragment, "HomeFragment");
        fragmentTransaction.commit();
    }

    private void getSharePref() {
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        profileImage = prefs.getString("profileImageURL", "0");
        currentUserEmail = prefs.getString("email", "0");

    }

    private void setSharePref() {
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        int count = prefs.getInt("count", 0);

        if(count ==1){
            Intent myIntent = new Intent(HomeActivity.this, HomeActivity.class);
            startActivity(myIntent);
            finish();
        }
        editor.putInt("count", 0);
        editor.apply();
    }

    OnApiError onApiError = new OnApiError() {
        @Override
        public void onError(String errorMessage, Throwable e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(HomeActivity.this, "File upload failed",
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    };
    public void openVideoCommentDilog(String user_email, Long video_id) {
        HomeFragment fragment = (HomeFragment) getSupportFragmentManager().findFragmentByTag("HomeFragment");
        if (fragment != null) {
            fragment.openVideoCommentDilog(video_id);
        }
    }
    public void callSubmitData(Long video_id, String comment) {
        VideoComments videoComments= new VideoComments();
        videoComments.setCommentCreateTime(System.currentTimeMillis());
        videoComments.setVideoId(video_id);
        videoComments.setCommentId(System.currentTimeMillis());
        videoComments.setCommentShadowFlag(true);
        videoComments.setCommentText(comment);
        videoComments.setUserEmail(currentUserEmail);
        videoComments.setUserProfilePic(profileImage);
        tikTokSampleViewModel.enterCommentForVideo(videoComments,onApiError);
        tikTokSampleViewModel.enterCommentForVideo(videoComments, new OnApiError() {
            @Override
            public void onError(String errorMessage, Throwable e) {

            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        for (int i = 0; i < mBtmView.getMenu().size(); i++) {
            MenuItem menuItem = mBtmView.getMenu().getItem(i);
            boolean isChecked = menuItem.getItemId() == item.getItemId();
            menuItem.setChecked(isChecked);
        }

        switch (item.getItemId()) {
            case R.id.navigation_home: {
                HomeFragment fragment = new HomeFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.content, fragment, "HomeFragment");
                fragmentTransaction.commit();
            }
            break;
            case R.id.navigation_me: {
                Intent myIntent = new Intent(HomeActivity.this, MainActivity.class);
                this.startActivity(myIntent);
                finish();
            }
            break;
            case R.id.navigation_discovery: {
            }
            break;
        }
        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onLogin(boolean showLoginUserInfo, SignInResult signInResult) {

    }

    @Override
    public void onLogOut(boolean showLoginUserInfo) {

    }


}