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


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.huawei.codelabs.splitbill.R;

import com.huawei.codelabs.splitbill.databinding.FragmentExpenseDetailBinding;
import com.huawei.codelabs.splitbill.ui.main.activities.MainActivity;
import com.huawei.codelabs.splitbill.ui.main.adapter.FriendsListAdapter;
import com.huawei.codelabs.splitbill.ui.main.helper.NearbyAgent;
import com.huawei.codelabs.splitbill.ui.main.models.Expense;
import com.huawei.codelabs.splitbill.ui.main.models.User;
import com.huawei.codelabs.splitbill.ui.main.viewmodels.ExpenseViewModel;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

public class ExpenseDetailFragment extends Fragment implements View.OnClickListener {
    public static final String TAG = "Expense Detail Fragment";
    public NearbyAgent nearbyAgent;
    Bitmap imageBitmap, scaledImageBitmap;
    FragmentExpenseDetailBinding fragmentViewExpenseBinding;
    List<FriendsListAdapter.FriendsUI> friendsUIList;
    private RecyclerView participantsList;
    private ExpenseViewModel expenseViewModel;
    private List<User> userList;
    public FriendsListAdapter friendsListAdapter;
    public Expense mExpense;
    private String expenseName;
    private String expenseAttachment;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentViewExpenseBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_expense_detail, container, false);
        View view = fragmentViewExpenseBinding.getRoot();
        Bundle args = getArguments();
        expenseName = args.getString("expense_name");
        expenseAttachment = args.getString("expense_attachment");
        if (expenseAttachment!=null) {
            Glide.with(this).load(expenseAttachment).into(fragmentViewExpenseBinding.imageViewExpenseDetail);
        }
        ((MainActivity) getActivity()).setActionBarTitle(expenseName);
        initView();
        return view;
    }

    private void initView() {
        imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.group);
        scaledImageBitmap = Bitmap.createScaledBitmap(imageBitmap, 720, 257, false);
        fragmentViewExpenseBinding.btnSend.setOnClickListener(this);
        nearbyAgent = ((MainActivity) getActivity()).nearbyAgent;
        participantsList = fragmentViewExpenseBinding.sharersList;
        expenseViewModel = ((MainActivity) getActivity()).createExpenseViewModel(this);
        expenseViewModel.baseRepository.download(expenseAttachment,expenseName);
        expenseViewModel.getUserLiveData().observe(getActivity(), users -> {
            userList = users;
            fetchExpenseData();
        }
        );
        participantsList.setLayoutManager(new LinearLayoutManager(getActivity()));
    }


    private void fetchExpenseData() {
        Bundle args = getArguments();
        int expenseId = args.getInt("expense_id");

        expenseViewModel.getExpensebyId(expenseId).observe(getActivity(), new Observer<List<Expense>>() {
            @Override
            public void onChanged(List<Expense> expenses) {
                List<FriendsListAdapter.FriendsUI> friendsUIList = getNamesListForUI(expenses);
                friendsListAdapter = new FriendsListAdapter(friendsUIList, ExpenseDetailFragment.this, false);
                participantsList.setAdapter(friendsListAdapter);
            }
        });
    }

    private List<FriendsListAdapter.FriendsUI> getNamesListForUI(List<Expense> expenses) {
        friendsUIList = new ArrayList<>();
        mExpense = expenses.get(0);
        UpdateUIWithExpense();
            String[] participants = mExpense.getUser_ids().split(",");
            for (String participant : participants){
                FriendsListAdapter.FriendsUI friendsUI = new FriendsListAdapter.FriendsUI();
                for(User user : userList) {
                    if (user.getAgc_user_id().equals(participant)){
                        friendsUI.setFriendsName(user.getName());

                    }
                }
                friendsUI.setAmount(mExpense.getAmount()/participants.length);
                friendsUIList.add(friendsUI);
            }
        return friendsUIList;
    }

    @SuppressLint("SetTextI18n")
    private void UpdateUIWithExpense() {
        fragmentViewExpenseBinding.expenseDetailAmount.setText(getString(R.string.rs) + " "+ mExpense.getAmount() );
        fragmentViewExpenseBinding.expenseDetailName.setText(mExpense.getName());
        fragmentViewExpenseBinding.Receiver.setText(userList.get(mExpense.getPaid_user_id() - 1).getName());

    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnSend) {
            File file = nearbyAgent.createPdf(friendsUIList, scaledImageBitmap, getActivity());
            Bundle bundle = new Bundle();
            bundle.putString("mValues", String.valueOf(file));
            Navigation.findNavController(view).navigate(R.id.action_send_expense_details, bundle);
        }
    }
}
