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
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.schooldairy.databinding.LayoutStudentListItemBinding;
import com.huawei.schooldairy.model.UserData;

import java.util.ArrayList;
import java.util.List;

/**
 * Home Page Students list recycler view adapter
 * @author: Huawei
 * @since: 25-05-2021
 */
public class StudentListAdapter extends RecyclerView.Adapter<StudentListAdapter.StudentViewHolder> {

    public interface StudentItemClickListener {
        void onClick(int itemType, int position, UserData studentItem);
    }

    private Activity host;
    private List<UserData> items;
    private StudentItemClickListener studentItemClickListener;

    public StudentListAdapter(Activity activity, List<UserData> items, StudentItemClickListener listener) {
        this.host = activity;
        this.items = (null == items) ? new ArrayList() : items;
        this.studentItemClickListener = listener;
    }

    public void updateList(List<UserData> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StudentListAdapter.StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutStudentListItemBinding binding = LayoutStudentListItemBinding.inflate(
                        host.getLayoutInflater(), parent, false);
        return new StudentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentListAdapter.StudentViewHolder holder, int position) {
        holder.setItemPos(position);
        holder.bindingView.setStudentItem(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class StudentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private LayoutStudentListItemBinding bindingView;
        private int itemPos;

        public void setItemPos(int itemPos) {
            this.itemPos = itemPos;
        }

        public StudentViewHolder(@NonNull LayoutStudentListItemBinding bindingView) {
            super(bindingView.getRoot());
            this.bindingView = bindingView;
            this.bindingView.getRoot().setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            studentItemClickListener.onClick(2, getAdapterPosition(), items.get(itemPos));
        }
    }
}
