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
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.schooldairy.R;
import com.huawei.schooldairy.databinding.LayoutTaskListItemBinding;
import com.huawei.schooldairy.model.TaskItem;
import com.huawei.schooldairy.ui.listeners.TaskItemClickListener;
import com.huawei.schooldairy.userutils.Constants;
import com.huawei.schooldairy.userutils.UserUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Home Page Task list, Task history Recyclers list adapter
 * @author: Huawei
 * @since: 25-05-2021
 */
public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskListViewHolder> {
    private Activity host;
    private List<TaskItem> items;
    private TaskItemClickListener taskItemClickListener;

    public TaskListAdapter(Activity activity, List<TaskItem> items, TaskItemClickListener listener) {
        this.host = activity;
        this.items = (null == items) ? new ArrayList() : items;
        this.taskItemClickListener = listener;
    }

    public void updateList(List<TaskItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutTaskListItemBinding binding = DataBindingUtil
                .inflate(
                        host.getLayoutInflater(),
                        R.layout.layout_task_list_item,
                        parent, false);
        return new TaskListViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskListViewHolder holder, int position) {
        holder.setItemPos(position);
        holder.bindingView.setTaskItem(items.get(position));
        holder.bindingView.setStatusText(Constants.statusArray[items.get(position).getStatus()]);
        holder.bindingView.setBgColor(Constants.statusColorArray[items.get(position).getStatus()]);
        String strDate = UserUtil.dateToString(items.get(position).getDueDate());
        holder.bindingView.setDateText(UserUtil.utcToLocalString(strDate));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class TaskListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private LayoutTaskListItemBinding bindingView;
        private int itemPos;

        public void setItemPos(int itemPos) {
            this.itemPos = itemPos;
        }

        public TaskListViewHolder(@NonNull LayoutTaskListItemBinding bindingView) {
            super(bindingView.getRoot());
            this.bindingView = bindingView;
            this.bindingView.getRoot().setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            taskItemClickListener.onClick(Constants.TASK_ITEM, itemPos, items.get(itemPos));
        }
    }
}
