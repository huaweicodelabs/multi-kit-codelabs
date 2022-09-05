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

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.schooldairy.R;
import com.huawei.schooldairy.databinding.ActivityCreateTaskBinding;
import com.huawei.schooldairy.userutils.UserUtil;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * Activity class for create the student Task. It can be accessible only by Teacher User
 * @author: Huawei
 * @since: 25-05-2021
 */
public class CreateTaskActivity extends AbstractBaseActivity {

    private int mYear;
    private int mMonth;
    private int mDay;
    private String dueDate;
    private ActivityCreateTaskBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        showBackArrow();
        initCalendar();
        binding.btnCreateNewTask.setOnClickListener(v -> {
            hideKeyboard();
            upsertTaskItem();
        });
    }

    /**
     * Set the task data as Activity result via intent
     */
    public void upsertTaskItem() {
        AGConnectUser user = AGConnectAuth.getInstance().getCurrentUser();
        Intent intent = new Intent();
        intent.putExtra("student_id", "");
        intent.putExtra("task_name", binding.edtTaskName.getText().toString());
        intent.putExtra("task_desc", binding.edtTaskDescription.getText().toString());
        intent.putExtra("due_date", dueDate); //binding.edtTaskDueDate.getText().toString()
        intent.putExtra("created_date", "");
        setResult(RESULT_OK, intent);
        finish();
    }


    /**
     * To show Date picker
     */
    private void initCalendar() {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getDefault());
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        binding.edtTaskDueDate.setOnClickListener(v -> {
            hideKeyboard();
            DatePickerDialog dpd = new DatePickerDialog(
                    CreateTaskActivity.this,
                    R.style.AppTheme_DatePickerDialog,
                    (view, year, month, day) -> {
                        c.set(year, month, day, 0,0,0);
                        dueDate = UserUtil.dateToString(c.getTime());
                        binding.edtTaskDueDate.setText(UserUtil.dateToStringDisplay(c.getTime()));
                        mYear = c.get(Calendar.YEAR);
                        mMonth = c.get(Calendar.MONTH);
                        mDay = c.get(Calendar.DAY_OF_MONTH);
                    }, mYear, mMonth, mDay);
            dpd.setCancelable(false);
            dpd.getDatePicker().setMinDate(System.currentTimeMillis());
            Calendar d = Calendar.getInstance();
            d.add(Calendar.DAY_OF_MONTH, 1);
            dpd.getDatePicker().setMinDate(d.getTimeInMillis());
            dpd.show();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
    }
}