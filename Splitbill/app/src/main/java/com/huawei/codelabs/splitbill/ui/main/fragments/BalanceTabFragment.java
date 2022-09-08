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

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.codelabs.splitbill.R;
import com.huawei.codelabs.splitbill.databinding.FragmentBalanceTabBinding;
import com.huawei.codelabs.splitbill.ui.main.activities.MainActivity;
import com.huawei.codelabs.splitbill.ui.main.adapter.GroupDetailBalanceAdapter;
import com.huawei.codelabs.splitbill.ui.main.models.Expense;
import com.huawei.codelabs.splitbill.ui.main.models.User;
import com.huawei.codelabs.splitbill.ui.main.viewmodels.ExpenseViewModel;

import java.util.ArrayList;
import java.util.List;

public class BalanceTabFragment extends Fragment {
    private static final String TAG = "BalanceTabFragment";
    FragmentBalanceTabBinding fragmentBalanceTabBinding;
    RecyclerView balanceViewGroup;
    private GroupDetailBalanceAdapter groupAdapter;
    private ExpenseViewModel expenseViewModel;
    private List<User> userList;
    int count;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentBalanceTabBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_balance_tab, container, false);
        View view = fragmentBalanceTabBinding.getRoot();
        initView();
        return view;
    }

    private void initView() {
        expenseViewModel = ((MainActivity) getActivity()).createExpenseViewModel(this);
        balanceViewGroup = fragmentBalanceTabBinding.balanceViewGroup;
        balanceViewGroup.setHasFixedSize(true);
        balanceViewGroup.setLayoutManager(new LinearLayoutManager(getActivity()));

        expenseViewModel.getUserLiveData().observe(getActivity(), users -> {
                    userList = users;
                    fetchExpenseData();
                }
        );
    }

    private void fetchExpenseData() {
        Bundle args = getArguments();
        if (args != null) {
            int groupId = args.getInt("group_ids");
            String userId = args.getString("user_ids");
            expenseViewModel.getExpenseLiveData(groupId).observe(getActivity(), expenses -> {

                groupAdapter = new GroupDetailBalanceAdapter(getOweList(calculateOwe(expenses, wordCount(userId))));
                balanceViewGroup.setAdapter(groupAdapter);
            });
        }
    }

    private int wordCount(String userId) {
        char[] ch = new char[userId.length()];
        count = 0;
        for (int i = 0; i < userId.length(); i++) {
            ch[i] = userId.charAt(i);
            if (((i > 0) && (ch[i] != ' ') && (ch[i - 1] == ' ')) || ((ch[0] != ' ') && (i == 0)))
                count++;
        }
        return count;
    }

    private Float[][] calculateOwe(List<Expense> expenses, int noOfUsers) {
        // Current assumption User 0 is logged in.
        // oweArray is two dimensional array with size = (no Of users) * (no of expenses)
        Float[][] oweArray = new Float[noOfUsers][expenses.size()];
        Log.d(TAG, "Total number of Users are :" + noOfUsers);
        for (int i = 0; i < expenses.size(); i++) {
            //Currently we consider all users are participants of the expenses
            Expense expense = expenses.get(i);
            Float expenseAmount = expense.getAmount();
            int paidUserId = expense.getPaid_user_id() - 1;
            String[] expenseParticipants = expense.getUser_ids().split(",");
            List<Integer> userIDs = getParticipantsUserIdfromAgc_Ids(expenseParticipants);
            int noOfParticipants = expenseParticipants.length;
            for (int j = 0; j < noOfUsers; j++) {
                Log.d(TAG, "j,paiduserID = " + j + "," + paidUserId);
                if (j != paidUserId && isAParticipants(j, userIDs)) {
                    if (j < paidUserId) {
                        if (oweArray[j][paidUserId] != null) {
                            oweArray[j][paidUserId] = oweArray[j][paidUserId] - (expenseAmount / noOfParticipants);
                        } else {
                            oweArray[j][paidUserId] = -(expenseAmount / noOfParticipants);
                        }
                        Log.d(TAG, "result " + oweArray[j][paidUserId]);
                    } else {
                        if (oweArray[paidUserId][j] != null) {
                            oweArray[paidUserId][j] = oweArray[paidUserId][j] + (expenseAmount / noOfParticipants);
                        } else {
                            oweArray[paidUserId][j] = (expenseAmount / noOfParticipants);
                        }
                        Log.d(TAG, "-result " + oweArray[paidUserId][j]);
                    }
                }
            }

        }
        return oweArray;
    }


    private List<Integer> getParticipantsUserIdfromAgc_Ids(String[] expenseParticipants) {
        List<Integer> userIds = new ArrayList<>();
        for (String participant : expenseParticipants) {
            for (User user : userList) {
                if (participant.equals(user.getAgc_user_id())) {
                    userIds.add(user.getId());
                    break;
                }
            }
        }
        return userIds;
    }

    private boolean isAParticipants(int userId, List<Integer> userIds) {
        return userIds.contains(userId + 1);
    }

    private List<GroupDetailBalanceAdapter.OweUI> getOweList(Float[][] oweArray) {
        List<GroupDetailBalanceAdapter.OweUI> oweList = new ArrayList<>();
        for (int i = 0; i < oweArray.length; i++)
            for (int j = 0; j < oweArray[i].length; j++)
                if (oweArray[i][j] == null) {
                    continue;
                } else {
                    GroupDetailBalanceAdapter.OweUI owe = new GroupDetailBalanceAdapter.OweUI();
                    owe.setAmount(oweArray[i][j]);
                    owe.setPayee((userList.get(i).getName()));
                    owe.setReceiver(userList.get(j).getName());
                    oweList.add(owe);
                }

        return oweList;
    }
}