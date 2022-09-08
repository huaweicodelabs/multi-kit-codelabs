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
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.codelabs.splitbill.R;
import com.huawei.codelabs.splitbill.databinding.FragmentAccountBinding;
import com.huawei.codelabs.splitbill.ui.main.activities.MainActivity;
import com.huawei.codelabs.splitbill.ui.main.helper.Common;
import com.huawei.codelabs.splitbill.ui.main.helper.NearbyAgent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class AccountFragment extends Fragment implements View.OnClickListener {
    public static NearbyAgent nearbyAgent;
    FragmentAccountBinding fragmentAccountBinding;
    List<File> files;
    private File gpxfile;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        fragmentAccountBinding = FragmentAccountBinding.inflate(inflater, container, false);
        initView();
        Glide.with(this).load(R.drawable.billsplitimage).into(fragmentAccountBinding.imgProfile);
        return fragmentAccountBinding.getRoot();
    }

    private void initView() {
        files = new ArrayList<>();
        nearbyAgent = new NearbyAgent((MainActivity) getActivity(), fragmentAccountBinding);
        fragmentAccountBinding.btnSend.setOnClickListener(this);
        fragmentAccountBinding.btnLogOut.setOnClickListener(this);
        fragmentAccountBinding.btnReceive.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnLogOut:
                logout();
                break;
            case R.id.btnSend:
                File file = generateNoteOnSD("SplitBill.txt", "This is for testing");
                Log.d("file", file.getAbsolutePath() + ".." + file.getName() + ".." + file.length());
//                nearbyAgent.sendFile(generateNoteOnSD(getActivity(), "SplitBill.txt", "This is for testing"), customeScancodeBinding, dialog);
                break;
            case R.id.btnReceive:
//                nearbyAgent.receiveFile(fragmentGroupBinding);
                break;
        }

    }

    void logout() {
        if (null != AGConnectAuth.getInstance().getCurrentUser()) {
            AGConnectAuth.getInstance().signOut();
        }
        Common.getCommonInstace().goToAuthInActivity(getActivity());
    }

    public File generateNoteOnSD(String sFileName, String sBody) {
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "SplitBill");
            if (!root.exists()) {
                root.mkdirs();
            }
            gpxfile = new File(root, sFileName);
            Log.d("Path", gpxfile.getAbsolutePath());
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
            Log.d("Path", gpxfile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return gpxfile;
    }
}