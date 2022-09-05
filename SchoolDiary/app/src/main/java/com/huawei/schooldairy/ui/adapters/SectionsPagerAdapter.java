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
package com.huawei.schooldairy.ui.adapters;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.huawei.schooldairy.ui.fragment.StudentListFragment;
import com.huawei.schooldairy.ui.fragment.TaskListFragment;
import com.huawei.schooldairy.userutils.Constants;

/**
 * Home Page Viewpager with fragments adapter
 * @author: Huawei
 * @since: 25-05-2021
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {
    private int[] tabListIndex;
    private int[] tabListTitles;
    private final Context mContext;
    private final int userType;

    public SectionsPagerAdapter(Context context, FragmentManager fm, int uType) {
        super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.mContext = context;
        this.userType = uType;
        tabListIndex = (userType == Constants.USER_TEACHER) ? Constants.TEACHER_TAB : Constants.STUDENT_TAB;
        tabListTitles = (userType == Constants.USER_TEACHER) ? Constants.TEACHER_TAB_TITLES : Constants.STUDENT_TAB_TITLES;
    }

    @Override
    public Fragment getItem(int position) {
        int fragmentIndex = tabListIndex[position];
        if (fragmentIndex == Constants.STUDENT_ITEM) {
            return StudentListFragment.newInstance(fragmentIndex);
        } else if (fragmentIndex == Constants.TASK_ITEM || fragmentIndex == Constants.TASK_HISTORY_ITEM) {
            return TaskListFragment.newInstance(fragmentIndex);
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
         return mContext.getResources().getString(tabListTitles[position]);
    }

    @Override
    public int getCount() {
        return tabListTitles.length;
    }
}