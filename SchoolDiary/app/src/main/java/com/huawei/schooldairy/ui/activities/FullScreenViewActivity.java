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
package com.huawei.schooldairy.ui.activities;

import android.app.Activity;
import android.os.Bundle;

import com.huawei.schooldairy.databinding.ActivityFullscreenViewBinding;
import com.huawei.schooldairy.ui.adapters.FullScreenImageAdapter;

import java.util.ArrayList;

/**
 * Activity that handle, view images submitted by the student for the Task assigned for them
 * @author: Huawei
 * @since: 25-05-2021
 */
public class FullScreenViewActivity extends Activity {

    private FullScreenImageAdapter adapter;
    private ArrayList<String> imageUrlList = new ArrayList<>();
    private int position = 0;
    ActivityFullscreenViewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFullscreenViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getIntent().hasExtra("imagelist")) {
            imageUrlList.clear();
            imageUrlList = getIntent().getStringArrayListExtra("imagelist");
            position = getIntent().getExtras().getInt("position");
        }

        adapter = new FullScreenImageAdapter(FullScreenViewActivity.this, imageUrlList);
        binding.pager.setAdapter(adapter);
        binding.pager.setCurrentItem(position);
    }
}