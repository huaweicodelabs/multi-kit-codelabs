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
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.codelabs.splitbill.R;
import com.huawei.codelabs.splitbill.databinding.BalanceCardBinding;
import com.huawei.codelabs.splitbill.ui.main.models.Expense;

import java.util.List;

public class GroupDetailBalanceAdapter extends RecyclerView.Adapter<GroupDetailBalanceAdapter.ViewHolder> {
    public Expense expense;
    List<GroupDetailBalanceAdapter.OweUI> mOweList;


    public GroupDetailBalanceAdapter(List<GroupDetailBalanceAdapter.OweUI> oweList) {
        mOweList = oweList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(BalanceCardBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false));
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (mOweList.get(position).getAmount() < 0) {
            holder.textView.setText(mOweList.get(position).getPayee());
            holder.textView3.setText(mOweList.get(position).getReceiver());
            holder.textView2.setText(holder.itemView.getContext().getString(R.string.Rs) + String.format("%.2f", Math.abs(mOweList.get(position).getAmount())));
        } else {
            holder.textView.setText(mOweList.get(position).getReceiver());
            holder.textView2.setText(holder.itemView.getContext().getString(R.string.Rs) + String.format("%.2f", mOweList.get(position).getAmount()));
            holder.textView3.setText(mOweList.get(position).getPayee());
        }
    }

    @Override
    public int getItemCount() {
        return mOweList.size();
    }

    // Viewholder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;
        public TextView textView2;
        public TextView textView3;

        public ViewHolder(BalanceCardBinding itemView) {
            super(itemView.getRoot());
            this.textView = itemView.balanceSender;
            this.textView2 = itemView.balanceAmount;
            this.textView3 = itemView.balanceRecipient;
        }
    }


    public static class OweUI {
        public String getPayee() {
            return payee;
        }

        public void setPayee(String payee) {
            this.payee = payee;
        }

        public String getReceiver() {
            return receiver;
        }

        public void setReceiver(String receiver) {
            this.receiver = receiver;
        }

        public Float getAmount() {
            return amount;
        }

        public void setAmount(Float amount) {
            this.amount = amount;
        }

        private String payee;
        private String receiver;
        private Float amount;
    }
}
