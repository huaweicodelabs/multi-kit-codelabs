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
package com.huawei.discovertourismapp.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.huawei.discovertourismapp.R;


public class UserProfileActivity extends AppCompatActivity {
    String email, pic, name;
    TextView user_name, user_email;
    ImageView imageView;
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        init();
        toolbar.setTitle("Profile");
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            email = bundle.getString("email");
            pic = bundle.getString("pic");
            name = bundle.getString("name");
        }
        user_email.setText(email);
        user_name.setText(name);
        imageView.setImageDrawable(Drawable.createFromPath(pic));
    }

    public void init() {
        user_name = findViewById(R.id.user_name);
        user_email = findViewById(R.id.user_email);
        imageView = findViewById(R.id.imageView);
        toolbar = findViewById(R.id.toolbar);
    }

}
