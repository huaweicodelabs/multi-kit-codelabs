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

package com.huawei.hmshomedecorapp.adapter;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.huawei.hmshomedecorapp.activity.ui.electronics.ElectronicsFragment;
import com.huawei.hmshomedecorapp.activity.ui.furniture.FurnitureFragment;

public class FragmentCollectionPagerAdapter extends FragmentStatePagerAdapter {
    public FragmentCollectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = null;
        if(i == 0) {
            fragment = new FurnitureFragment();
            Bundle args = new Bundle();
            args.putInt(FurnitureFragment.ARG_OBJECT, i + 1);
            fragment.setArguments(args);
        }
        else{
            fragment = new ElectronicsFragment();
            Bundle args = new Bundle();
            args.putInt(ElectronicsFragment.ARG_OBJECT, i + 1);
            fragment.setArguments(args);
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if(position == 0) {
            return "Furniture";
        }
        else if(position == 1) {
            return "Electronics";
        }
        else {
            return "Eye Wear";
        }
    }
}
