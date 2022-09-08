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

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.huawei.codelabs.splitbill.R;
import com.huawei.codelabs.splitbill.databinding.FragmentFileDetailsBinding;
import com.huawei.codelabs.splitbill.ui.main.activities.MainActivity;
import com.huawei.codelabs.splitbill.ui.main.adapter.FilesAdapter;
import com.huawei.codelabs.splitbill.ui.main.helper.Constants;
import com.huawei.codelabs.splitbill.ui.main.models.Files;

import java.io.File;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class FileDetailsFragment extends Fragment implements View.OnClickListener {

    FragmentFileDetailsBinding fragmentFileDetailsBinding;
    ArrayList<Files> filesArrayList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentFileDetailsBinding = FragmentFileDetailsBinding.inflate(inflater, container, false);
        initView();
        return fragmentFileDetailsBinding.getRoot();
    }

    private void initView() {
        fragmentFileDetailsBinding.receiveInvoice.setOnClickListener(this);
        fragmentFileDetailsBinding.rcFiles.setHasFixedSize(true);
        fragmentFileDetailsBinding.rcFiles.setLayoutManager(new LinearLayoutManager(getActivity()));
        String directory_path = Environment.getExternalStorageDirectory().getPath() + Constants.DOWNLOAD_PATH;
        filesArrayList = new ArrayList<>();

        File directory = new File(directory_path);
        if (directory.listFiles() != null) {
            for (File file : directory.listFiles()) {
                Files files = new Files();
                files.setFileName(file.getName());
                files.setFilePath(new File(file.getAbsolutePath()));
                filesArrayList.add(files);

            }
        }
        FilesAdapter groupAdapter = new FilesAdapter(filesArrayList, fragmentFileDetailsBinding);
        fragmentFileDetailsBinding.rcFiles.setAdapter(groupAdapter);
        groupAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.receiveInvoice) {
            if (((MainActivity) getActivity()).nearbyAgent != null) {
                fragmentFileDetailsBinding.tvMainDesc.setVisibility(View.VISIBLE);
                ((MainActivity) getActivity()).nearbyAgent.receiveFile(fragmentFileDetailsBinding);
            } else {
                Toast.makeText(getActivity(), "null", Toast.LENGTH_SHORT).show();
            }
        }
    }
}