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
package com.huawei.schooldairy.userutils;

import androidx.annotation.StringRes;

import com.huawei.schooldairy.R;

/**
 * Global Constants used in the application
 * @author: Huawei
 * @since: 25-05-2021
 */
public interface Constants {
    int USER_TEACHER = 1;
    int USER_STUDENT = 0;

    int STATUS_NEW = 0; //default
    int STATUS_SUBMITTED = 1;
    int STATUS_EVALUATED = 2;
    int STATUS_CLOSED = 3;

    String[] statusArray = new String[]{"NEW", "SUBMITTED", "EVALUATED", "CLOSED"};
    String[] statusColorArray = new String[]{"#C6E3FA", "#F3DE9F", "#9FF3C1", "#F8CFDB"};

    int TASK_ITEM = 11;
    int TASK_HISTORY_ITEM = 12;
    int STUDENT_ITEM = 13;

    int[] STUDENT_TAB = new int[]{Constants.TASK_ITEM, Constants.TASK_HISTORY_ITEM};
    int[] TEACHER_TAB = new int[]{Constants.TASK_ITEM, Constants.STUDENT_ITEM};

    @StringRes
    int[] STUDENT_TAB_TITLES = new int[]{R.string.hometab_teach_task, R.string.hometab_teach_task_history};
    @StringRes
    int[] TEACHER_TAB_TITLES = new int[]{R.string.hometab_teach_task, R.string.hometab_teach_student};
}
