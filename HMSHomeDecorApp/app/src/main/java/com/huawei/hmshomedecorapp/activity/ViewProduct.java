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

package com.huawei.hmshomedecorapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hms.scene.sdk.ux.ar.utils.PermissionUtil;
import com.huawei.hms.scene.sdk.ux.base.utils.InitializeHelper;
import com.huawei.hmshomedecorapp.R;

public class ViewProduct extends AppCompatActivity {
    String paths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_product_home_decor);
        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!PermissionUtil.checkARPermissions(ViewProduct.this)) {
                    PermissionUtil.requestARPermissions(ViewProduct.this);
                }

                if (!InitializeHelper.getInstance().isInitialized()) {
                    Toast.makeText(ViewProduct.this, "SceneKit initializing", Toast.LENGTH_SHORT).show();

                    InitializeHelper.getInstance().initialize(ViewProduct.this);
                    return;
                }

                Intent intent = new Intent(ViewProduct.this , TryProduct.class);
                startActivity(intent);
            }
        });
    }


}