/*
 * Copyright 2022. Huawei Technologies Co., Ltd. All rights reserved.
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
     http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.huawei.codelabs.splitbill.ui.main.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;


import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import com.huawei.codelabs.splitbill.R;
import com.huawei.codelabs.splitbill.databinding.ActivitySplashScreenBinding;
import com.huawei.codelabs.splitbill.ui.main.helper.Common;
import com.huawei.codelabs.splitbill.ui.main.viewmodels.SplashViewModel;

import java.util.Timer;
import java.util.TimerTask;


public class SplashScreen extends BaseActivity {

    SplashViewModel splashViewModel;
    ActivitySplashScreenBinding activitySplashScreenBinding;
    Animation rotateAnim, splashleftAnim;
    int count = 0;
    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activitySplashScreenBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash_screen);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        activitySplashScreenBinding.progressBar.setVisibility(View.INVISIBLE);
        animation();
        initSplashViewModel();
        checkIfUserIsAuthenticated();
    }

    private void animation() {
        rotateAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_animation);
        splashleftAnim = AnimationUtils.loadAnimation(this, R.anim.splash_left_anim);
        activitySplashScreenBinding.splashImage.setAnimation(rotateAnim);
        activitySplashScreenBinding.splashSlogan.setAnimation(splashleftAnim);
    }

    private void initSplashViewModel() {
        splashViewModel = new ViewModelProvider(this).get(SplashViewModel.class);
    }

    private void checkIfUserIsAuthenticated() {
        splashViewModel.checkIfUserIsAuthenticated();
        splashViewModel.isUserAuthenticatedLiveData.observe(this, loginUser -> {
            if (!loginUser.isAuthenticated) {
                activitySplashScreenBinding.progressBar.setVisibility(View.VISIBLE);
                timer = new Timer();
                TimerTask timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        count++;
                        activitySplashScreenBinding.progressBar.setProgress(count);
                        if (count == 50) {
                            timer.cancel();
                            Common.setFirstTimeUserLoggedIn(getApplicationContext(), false);
                            SplashScreen.this.goToAuthInActivity();
                            SplashScreen.this.finish();
                        }
                    }
                };
                timer.schedule(timerTask, 0, 50);
            } else {
                activitySplashScreenBinding.progressBar.setVisibility(View.VISIBLE);
                Handler handler = new Handler();
                handler.postDelayed(() -> {
                    Common.setFirstTimeUserLoggedIn(getApplicationContext(), true);
                    SplashScreen.this.getUserFromDatabase(loginUser.userID);
                }, 1500);
            }
        });
    }

    public void goToAuthInActivity() {
        Intent intent = new Intent(SplashScreen.this, AuthActivity.class);
        startActivity(intent);
    }

    private void getUserFromDatabase(String uid) {
        splashViewModel.setUid(uid);
        splashViewModel.userLiveData.observe(this, user -> openAndCheckCloudBStatus(splashViewModel, user, null));
    }
}