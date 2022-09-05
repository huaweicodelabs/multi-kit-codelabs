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
package com.huawei.schooldairy.ui.listeners;

import android.util.Log;

import com.huawei.schooldairy.model.UserData;

import java.util.List;
/**
 * Listener for UserData table Cloud DB operation
 * @author: Huawei
 * @since: 25-05-2021
 */
public interface UiStudentCallBack {
    UiStudentCallBack DEFAULT = new UiStudentCallBack() {
        @Override
        public void onStudentAddOrQuery(List<UserData> studentItemList, int tag) {
            Log.i("onStudentAddOrQuery", "Executed:"+tag);
        }
        @Override
        public void updateStudentUiOnError(String errorMessage) {
            Log.i("Error", "DB Operation Error, Something went wrong!");
        }
    };

    void onStudentAddOrQuery(List<UserData> studentItemList, int tag);

    void updateStudentUiOnError(String errorMessage);
}