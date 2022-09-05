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

import com.huawei.schooldairy.databinding.LayoutTaskSummaryItemBinding;
import com.huawei.schooldairy.model.UserAndTask;
import com.huawei.schooldairy.ui.listeners.TaskItemClickListener;
import com.huawei.schooldairy.userutils.Constants;
import com.huawei.schooldairy.userutils.UserUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * List of particular task details, which is assigned students for that particular task
 * @author: Huawei
 * @since: 25-05-2021
 */
public class TaskSumListAdapter extends RecyclerView.Adapter<TaskSumListAdapter.TaskSumListViewHolder> {
    private Activity host;
    private List<UserAndTask> items;
    private TaskItemClickListener taskItemClickListener;

    public TaskSumListAdapter(Activity activity, List<UserAndTask> items, TaskItemClickListener listener) {
        this.host = activity;
        this.items = (null == items) ? new ArrayList() : items;
        this.taskItemClickListener = listener;
    }

    public void updateList(List<UserAndTask> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskSumListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate layout and retrieve binding
        LayoutTaskSummaryItemBinding binding = LayoutTaskSummaryItemBinding.inflate(host.getLayoutInflater(), parent, false);
        return new TaskSumListViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskSumListViewHolder holder, int position) {
        holder.setItemPos(position);
        holder.bindingView.setUserData(items.get(position).getUserData());
        holder.bindingView.setStatusText(Constants.statusArray[items.get(position).getTaskItem().getStatus()]);
        holder.bindingView.setBgColor(Constants.statusColorArray[items.get(position).getTaskItem().getStatus()]);
        String strDate = UserUtil.dateToString(items.get(position).getTaskItem().getDueDate());
        holder.bindingView.setDateText(UserUtil.utcToLocalString(strDate));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class TaskSumListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private LayoutTaskSummaryItemBinding bindingView;
        private int itemPos;

        public void setItemPos(int itemPos) {
            this.itemPos = itemPos;
        }

        public TaskSumListViewHolder(@NonNull LayoutTaskSummaryItemBinding bindingView) {
            super(bindingView.getRoot());
            this.bindingView = bindingView;
            this.bindingView.getRoot().setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            taskItemClickListener.onClick(1, itemPos, items.get(itemPos).getTaskItem());
        }
    }
}
