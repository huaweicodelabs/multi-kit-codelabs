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
package com.huawei.schooldairy.ui.adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.huawei.schooldairy.R;
import com.huawei.schooldairy.databinding.LayoutAttachmentItemBinding;
import com.huawei.schooldairy.ui.activities.FullScreenViewActivity;

import java.util.ArrayList;
/**
 * Adapter for Task images which is uploaded by students
 *
 * @author: Huawei
 * @since: 25-05-2021
 */
public class AttachmentAdapter extends RecyclerView.Adapter<AttachmentAdapter.AttachmentViewHolder> {
    private Activity context;
    private ArrayList<String> attachmentList;

    public AttachmentAdapter(Activity context, ArrayList<String> attachmentList) {
        this.context = context;
        this.attachmentList = attachmentList;
    }

    @NonNull
    @Override
    public AttachmentAdapter.AttachmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutAttachmentItemBinding binding = LayoutAttachmentItemBinding.inflate(context.getLayoutInflater(), parent, false);
        return new AttachmentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull AttachmentAdapter.AttachmentViewHolder holder, int position) {
        holder.setItemPos(position);
        Glide.with(context)
                .load(attachmentList.get(position))
                .centerCrop()
                .placeholder(R.drawable.ic_image_placeholder)
                .error(R.drawable.ic_no_image_error)
                .into(holder.attachmentItemBinding.imgAttachment);
        holder.attachmentItemBinding.imgAttachment.setOnClickListener(v -> {
            Intent intent = new Intent(context, FullScreenViewActivity.class);
            intent.putStringArrayListExtra("imagelist", attachmentList);
            intent.putExtra("position", position);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return attachmentList.size();
    }

    public void updateList(ArrayList<String> attachmentList) {
        this.attachmentList = attachmentList;
        notifyDataSetChanged();
    }

    public class AttachmentViewHolder extends RecyclerView.ViewHolder {

        private LayoutAttachmentItemBinding attachmentItemBinding;
        private int itemPos;

        public void setItemPos(int itemPos) {
            this.itemPos = itemPos;
        }

        public AttachmentViewHolder(@NonNull LayoutAttachmentItemBinding attachmentItemBinding) {
            super(attachmentItemBinding.getRoot());
            this.attachmentItemBinding = attachmentItemBinding;
        }

    }
}
