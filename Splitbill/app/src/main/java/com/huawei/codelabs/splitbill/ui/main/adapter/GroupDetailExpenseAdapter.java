/* Copyright 2022. Huawei Technologies Co., Ltd. All rights reserved.
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
package com.huawei.codelabs.splitbill.ui.main.adapter;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.codelabs.splitbill.R;
import com.huawei.codelabs.splitbill.databinding.ExpenseCardBinding;
import com.huawei.codelabs.splitbill.databinding.FragmentExpenseTabBinding;

import java.util.List;

public class GroupDetailExpenseAdapter extends RecyclerView.Adapter<GroupDetailExpenseAdapter.ViewHolder> {
    private final List<ExpenseUI> expenseList;
    FragmentExpenseTabBinding fragmentExpenseTabBinding;

    public GroupDetailExpenseAdapter(List<ExpenseUI> expenseUIList, FragmentExpenseTabBinding fragmentExpenseTabBinding) {
        this.expenseList = expenseUIList;
        this.fragmentExpenseTabBinding = fragmentExpenseTabBinding;
    }

    //Added Item group display card as viewholder object
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GroupDetailExpenseAdapter.ViewHolder(ExpenseCardBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false));
    }

    //To update data in expense Card
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExpenseUI expenseModel = expenseList.get(position);
        holder.expenseName.setText(expenseModel.expenseName);
        holder.expenseAmount.setText(expenseModel.getAmount().toString());
        holder.participants.setText(expenseModel.getParticipants());
        holder.payee.setText("Paid by " + expenseModel.getPaidBy());
        int backgroundColor;
        if (position % 2 == 0) {
            backgroundColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.sec_orange);
        } else {
            backgroundColor = ContextCompat.getColor(holder.itemView.getContext(), R.color.colorSmoke);
        }
        holder.imageview_icon.setBackgroundColor(backgroundColor);
    }

    @Override
    public int getItemCount() {
        return expenseList.size();
    }

    private void showContent(int position, Bundle bundle) {
        ExpenseUI expenseModel = expenseList.get(position);
        String expenseName = expenseModel.getExpenseName();
        bundle.putString("expense_name", expenseName);
        bundle.putInt("expense_id", expenseModel.getId());
        bundle.putString("expense_attachment", expenseModel.getExpenseAttachment());
    }

    public static class ExpenseUI {
        Float amount;
        String expenseName;
        String participants;
        String paidBy;
        String expenseAttachment;
        Integer Id;

        public Float getAmount() {
            return amount;
        }

        public void setAmount(Float amount) {
            this.amount = amount;
        }

        public String getExpenseName() {
            return expenseName;
        }

        public void setExpenseName(String expenseName) {
            this.expenseName = expenseName;
        }

        public String getParticipants() {
            return participants;
        }

        public void setParticipants(String participants) {
            this.participants = participants;
        }

        public String getPaidBy() {
            return paidBy;
        }

        public void setPaidBy(String paidBy) {
            this.paidBy = paidBy;
        }

        public Integer getId() {
            return Id;
        }

        public void setId(Integer id) {
            Id = id;
        }

        public String getExpenseAttachment() {
            return expenseAttachment;
        }

        public void setExpenseAttachment(String expenseAttachment) {
            this.expenseAttachment = expenseAttachment;
        }
    }

    // Viewholder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView expenseName;
        public TextView expenseAmount;
        public TextView payee;
        public TextView participants;
        public RelativeLayout expenseDetailItem;
        public RelativeLayout imageview_icon;

        public ViewHolder(ExpenseCardBinding itemView) {
            super(itemView.getRoot());
            this.expenseName = itemView.expenseName;
            this.expenseAmount = itemView.expenseAmount;
            this.payee = itemView.payee;
            this.participants = itemView.expenseParticipants;
            this.expenseDetailItem = itemView.expenseDetailItem;
            this.imageview_icon = itemView.imageviewIcon;
            expenseDetailItem.setOnClickListener(this);

        }

        //OnClick from View Holder
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.expenseDetailItem) {
                Bundle bundle = new Bundle();
                int position = this.getAdapterPosition();
                showContent(position, bundle);
                Navigation.findNavController(fragmentExpenseTabBinding.getRoot()).navigate(R.id.expenseDetailFragment, bundle);
            }
        }

    }
}