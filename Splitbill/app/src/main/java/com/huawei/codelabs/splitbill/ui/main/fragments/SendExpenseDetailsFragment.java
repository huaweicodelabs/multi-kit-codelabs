/*
 * Copyright 2022. Huawei Technologies Co., Ltd. All rights reserved.
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

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.huawei.codelabs.splitbill.R;
import com.huawei.codelabs.splitbill.databinding.FragmentSendExpenseDetailsBinding;
import com.huawei.codelabs.splitbill.ui.main.activities.MainActivity;

import java.io.File;


/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 */
public class SendExpenseDetailsFragment extends Fragment {
    public static final String TAG = "SendExpenseDetailsFragment";
    FragmentSendExpenseDetailsBinding fragmentSendExpenseDetailsBinding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentSendExpenseDetailsBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_send_expense_details, container, false);
        View view = fragmentSendExpenseDetailsBinding.getRoot();
        initView();
        return view;
    }

    @Override
    public void onResume() {
        //Setting the action title bar name
        ((MainActivity) getActivity()).setActionBarTitle(MainActivity.fragmentName);
        super.onResume();
    }

    private void initView() {
        ((MainActivity) getActivity()).nearbyAgent.sendFile(new File(getArguments().getString("mValues")), fragmentSendExpenseDetailsBinding);
    }

}
