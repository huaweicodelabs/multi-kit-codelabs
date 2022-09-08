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
package com.huawei.codelabs.splitbill.ui.main.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.huawei.codelabs.splitbill.R;
import com.huawei.codelabs.splitbill.databinding.FragmentGroupDetailBinding;
import com.huawei.codelabs.splitbill.ui.main.activities.MainActivity;
import com.huawei.codelabs.splitbill.ui.main.adapter.ViewPagerFragmentAdapter;
import com.huawei.codelabs.splitbill.ui.main.models.Expense;
import com.huawei.codelabs.splitbill.ui.main.viewmodels.ExpenseViewModel;

import java.util.List;

public class GroupDetailFragment extends Fragment {
    public static final String TAG = "Group Detail Fragment";
    public float total = 0;
    public int groupId;
    FragmentGroupDetailBinding fragmentViewGroupBinding;
    ViewPagerFragmentAdapter viewPagerFragmentAdapter;
    private String userId;
    //String titles are used to set the name of two tab dynamically
    private final int[] titles = new int[]{R.string.expense, R.string.balance};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentViewGroupBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_group_detail, container, false);
        View view = fragmentViewGroupBinding.getRoot();
        setContent();
        fragmentViewGroupBinding.addExpense.setOnClickListener(view1 -> {
            Navigation.findNavController(view1).navigate(R.id.action_newExpense);
            Log.d(TAG, "New Expense Fragment Success");
        });

        initView();
        return view;
    }

    private void setContent() {
        Bundle args = getArguments();
        String description = args.getString("description_name");
        String participant = args.getString("members_name");
        String status = args.getString("members");
        String groupName = args.getString("group_name");
        String groupProfile = args.getString("group_profile");
        groupId = args.getInt("group_id");
        userId = args.getString("members_name");
        fragmentViewGroupBinding.groupDetailsMemeberTextView.setText(participant);
        fragmentViewGroupBinding.groupDetailsDescriptionShow.setText(description);
        fragmentViewGroupBinding.groupDetailStatusShow.setText(status);
        Glide.with(this).load(groupProfile).into(fragmentViewGroupBinding.groupDetailImage);

        ((MainActivity) getActivity()).setActionBarTitle(groupName);
    }


    //Tab layout init method which will create 2 tab called Expense and Balance
    private void initView() {
        try {
            TabLayout tabLayout = fragmentViewGroupBinding.tabLayout;
            ViewPager2 viewPager = fragmentViewGroupBinding.viewPage;
            viewPagerFragmentAdapter = new ViewPagerFragmentAdapter(this, groupId, userId);
            viewPager.setAdapter(viewPagerFragmentAdapter);
            new TabLayoutMediator(tabLayout, viewPager, ((tab, position) -> tab.setText(titles[position]))).attach();
            ExpenseViewModel expenseViewModel = ((MainActivity) getActivity()).createExpenseViewModel(this);
            expenseViewModel.getExpenseLiveData(groupId).observe(getActivity(), expenses -> {
                if (total == 0) {
                    float amt = 0;
                    for (Expense expense : expenses) {
                        amt += expense.getAmount();
                    }
                    total = amt;
                }
                fragmentViewGroupBinding.groupDetailTotalExpenseAmountTextView.setText(String.valueOf(total));
            });

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "Error : ", e);
        }
    }
}