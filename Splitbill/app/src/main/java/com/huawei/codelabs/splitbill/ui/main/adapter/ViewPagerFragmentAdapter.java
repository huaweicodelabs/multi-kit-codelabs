/* Copyright 2022. Huawei Technologies Co., Ltd. All rights reserved.
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
package com.huawei.codelabs.splitbill.ui.main.adapter;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.huawei.codelabs.splitbill.ui.main.fragments.BalanceTabFragment;
import com.huawei.codelabs.splitbill.ui.main.fragments.ExpenseTabFragment;

public class ViewPagerFragmentAdapter extends FragmentStateAdapter {
    private static final String TAG = "ViewPagerFragmentAdapter";
    private final int groupId;
    private final String userId;

    public ViewPagerFragmentAdapter(@NonNull Fragment fragment, int groupId, String userId) {
        super(fragment);
        this.groupId = groupId;
        this.userId = userId;
        Log.d(TAG, "Inside Constr");
    }

    //Created 2 switch case of Expense and Balance dynamically
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                Log.d(TAG, "Case 0 success");
                Bundle bundle = new Bundle();
                bundle.putInt("group_id", groupId);
                ExpenseTabFragment fragment = new ExpenseTabFragment();
                fragment.setArguments(bundle);
                return fragment;
            case 1:
                Log.d(TAG, "Case 1 success");
                Bundle bundles = new Bundle();
                bundles.putInt("group_ids", groupId);
                bundles.putString("user_ids", userId);
                BalanceTabFragment fragments = new BalanceTabFragment();
                fragments.setArguments(bundles);
                return fragments;
        }
        return new ExpenseTabFragment();
    }

    //Title tab length count
    @Override
    public int getItemCount() {
        return 2;
    }
}
