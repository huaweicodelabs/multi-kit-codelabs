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

package com.huawei.hmshomedecorapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.hms.scene.sdk.ux.ar.utils.PermissionUtil;
import com.huawei.hms.scene.sdk.ux.base.utils.InitializeHelper;
import com.huawei.hmshomedecorapp.R;
import com.huawei.hmshomedecorapp.activity.MainActivityWithDrawer;
import com.huawei.hmshomedecorapp.model.ItemModel;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {
    List<ItemModel> itemModelList;
    Context context;

    public ItemAdapter(Context context, List<ItemModel> itemModelList) {
        this.itemModelList = itemModelList;
        this.context = context;
    }
    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        ItemViewHolder itemViewHolder = new ItemViewHolder(itemLayoutView);
        return itemViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        ItemModel itemModel = itemModelList.get(position);
        holder.itemImage.setImageResource(itemModel.getImageId());
        holder.itemTitle.setText(itemModel.getApplianceName());
        holder.itemImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    if (!PermissionUtil.checkARPermissions((Activity)context)) {
                        PermissionUtil.requestARPermissions((Activity)context);
                    }

                    if (!InitializeHelper.getInstance().isInitialized()) {
                        Toast.makeText(context, "SceneKit initializing", Toast.LENGTH_SHORT).show();

                        InitializeHelper.getInstance().initialize((Activity)context);
                        ((MainActivityWithDrawer)context).navigateToParticularFragment(itemModel);
                        return;
                    }
                    else {
                        ((MainActivityWithDrawer)context).navigateToParticularFragment(itemModel);
                    }
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemModelList.size();
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemTitle;
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemImage = (ImageView) itemView.findViewById(R.id.item_image);
            this.itemTitle = (TextView) itemView.findViewById(R.id.item_title);
        }
    }
}
