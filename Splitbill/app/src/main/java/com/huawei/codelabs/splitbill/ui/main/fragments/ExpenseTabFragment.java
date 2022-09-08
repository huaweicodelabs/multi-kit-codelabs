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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.codelabs.splitbill.R;
import com.huawei.codelabs.splitbill.databinding.FragmentExpenseTabBinding;
import com.huawei.codelabs.splitbill.ui.main.activities.MainActivity;
import com.huawei.codelabs.splitbill.ui.main.adapter.GroupDetailExpenseAdapter;
import com.huawei.codelabs.splitbill.ui.main.models.Expense;
import com.huawei.codelabs.splitbill.ui.main.models.User;
import com.huawei.codelabs.splitbill.ui.main.viewmodels.ExpenseViewModel;

import java.util.ArrayList;
import java.util.List;

public class ExpenseTabFragment extends Fragment {
    public static final String TAG = "ExpenseTab";
    FragmentExpenseTabBinding fragmentExpenseTabBinding;
    private ExpenseViewModel expenseViewModel;
    private RecyclerView expenseViewGroup;
    private GroupDetailExpenseAdapter groupAdapter;
    private List<User> userList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentExpenseTabBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_expense_tab, container, false);
        View view = fragmentExpenseTabBinding.getRoot();
        init();
        return view;
    }

    //Recycle view init method of Expense tab
    private void init() {
        expenseViewModel = ((MainActivity) getActivity()).createExpenseViewModel(this);
        expenseViewGroup = fragmentExpenseTabBinding.expenseViewGroup;
        expenseViewGroup.setHasFixedSize(true);
        expenseViewGroup.setLayoutManager(new LinearLayoutManager(getActivity()));

        expenseViewModel.getUserLiveData().observe(getActivity(), users -> {
                    userList = users;
                    fetchExpenseData();
                }
        );

    }

    private void fetchExpenseData() {
        Bundle args = getArguments();
        int groupId = args.getInt("group_id");

        expenseViewModel.getExpenseLiveData(groupId).observe(getActivity(), expenses -> {
            List<GroupDetailExpenseAdapter.ExpenseUI> expenseUIList = getExpenseListForUI(expenses);
            groupAdapter = new GroupDetailExpenseAdapter(expenseUIList, fragmentExpenseTabBinding);
            expenseViewGroup.setAdapter(groupAdapter);
            Log.d(TAG, "Expense Tab fragment recycleView Changed");
        });
    }

    private List<GroupDetailExpenseAdapter.ExpenseUI> getExpenseListForUI(List<Expense> expenses) {
        List<GroupDetailExpenseAdapter.ExpenseUI> expenseUIList = new ArrayList<>();

        for (Expense expense : expenses) {
            GroupDetailExpenseAdapter.ExpenseUI expenseUI = new GroupDetailExpenseAdapter.ExpenseUI();
            expenseUI.setId(expense.getId());
            expenseUI.setExpenseName(expense.getName());
            expenseUI.setAmount(expense.getAmount());
            expenseUI.setPaidBy(userList.get(expense.getPaid_user_id() - 1).getName());
            expenseUI.setParticipants(getParticipantsName(expense.getUser_ids().split(",")));
            expenseUI.setExpenseAttachment(expense.getAttachment());
            expenseUIList.add(expenseUI);

        }
        return expenseUIList;
    }

    private String getParticipantsName(String[] expenseParticipants) {
        StringBuilder sb = new StringBuilder();
        for (String participant : expenseParticipants) {
            for (User user : userList) {
                if (participant.equals(user.getAgc_user_id())) {
                    sb.append(user.getName()).append(", ");
                    break;
                }
            }
        }
        return sb.toString();
    }
}