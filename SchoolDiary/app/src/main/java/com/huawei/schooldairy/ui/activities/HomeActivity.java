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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.huawei.schooldairy.R;
import com.huawei.schooldairy.databinding.ActivityTeachersHomeBinding;
import com.huawei.schooldairy.ui.adapters.SectionsPagerAdapter;
import com.huawei.schooldairy.userutils.Constants;
import com.huawei.schooldairy.userutils.PrefUtil;

/**
 * Activity that holds fragments of Home page for Teacher, Student users
 * @author: Huawei
 * @since: 25-05-2021
 */
public class HomeActivity extends AppCompatActivity {

    public static boolean NEED_UPDATE = false;
    private ActivityTeachersHomeBinding binding;
    private int userType;

    /**
     * Init views, set home page view pager adapter for load fragments
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTeachersHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setTitle(R.string.app_name);

        if (PrefUtil.getInstance(this).getBool("IS_MAPPED")) {
            userType = PrefUtil.getInstance(this).getInt("USER_TYPE");
        }

        if (userType == -1) {
            Toast.makeText(HomeActivity.this, "Invalid User Type", Toast.LENGTH_SHORT).show();
        } else {

            SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager(), userType);
            binding.viewPager.setAdapter(sectionsPagerAdapter);
            binding.tabs.setupWithViewPager(binding.viewPager);
        }
    }


    /**
     * Options menu icon for user's profile page
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_student_detail, menu);
        return true;
    }

    /**
     * Options menu via profile page based on logged in user
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            if (userType == Constants.USER_TEACHER)
                startActivity(new Intent(HomeActivity.this, TeacherProfileActivity.class));
            else if (userType == Constants.USER_STUDENT)
                startActivity(new Intent(HomeActivity.this, StudentProfileActivity.class));
            else
                Toast.makeText(HomeActivity.this, "Invalid User type", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}