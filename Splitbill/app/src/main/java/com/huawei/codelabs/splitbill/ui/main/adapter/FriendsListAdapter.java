/*
 * Copyright 2022. Huawei Technologies Co., Ltd. All rights reserved.
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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.codelabs.splitbill.R;
import com.huawei.codelabs.splitbill.databinding.FriendsCardBinding;


import java.text.DecimalFormat;
import java.util.List;

//FriendsList adapter for friends list view
public class FriendsListAdapter extends RecyclerView.Adapter<FriendsListAdapter.ViewHolder> {

    private final Boolean mCheckBoxed;
    private final List<FriendsUI> friendsList;

    public FriendsListAdapter(List<FriendsUI> friends, Fragment fragments, boolean selectable) {
        this.friendsList = friends;
        mCheckBoxed = selectable;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(FriendsCardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        DecimalFormat df = new DecimalFormat("0.00");
        FriendsUI friendsUIModel = friendsList.get(position);
        holder.textView.setText(friendsUIModel.getFriendsName());
        if (friendsUIModel.getAmount() != null) {
            holder.shareTextView.setText(holder.mView.getResources().getString(R.string.rs) + " " + df.format(friendsUIModel.amount));
        }

    }

    @Override
    public int getItemCount() {
        return friendsList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final View mView;
        public final TextView textView;
        public final TextView shareTextView;
        public final CheckBox checkbox;
        public final CardView highlightCard;

        public ViewHolder(@NonNull FriendsCardBinding view) {
            super(view.getRoot());
            mView = view.getRoot();
            textView = view.friendNAme;
            checkbox = view.checkFriends;
            shareTextView = view.share;
            highlightCard = view.highlightCard;
            if (mCheckBoxed) {
                checkbox.setVisibility(View.VISIBLE);
                shareTextView.setVisibility(View.INVISIBLE);
                if (checkbox.isChecked()) {
                    highlightCard.setCardBackgroundColor(mView.getResources().getColor(R.color.sec_var_orange));
                } else {
                    highlightCard.setCardBackgroundColor(mView.getResources().getColor(R.color.icebergColor));
                }
            } else {
                checkbox.setVisibility(View.INVISIBLE);
                shareTextView.setVisibility(View.VISIBLE);

                highlightCard.setCardBackgroundColor(mView.getResources().getColor(R.color.sec_var_orange));
            }

            checkbox.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                if (isChecked) {
                    highlightCard.setCardBackgroundColor(mView.getResources().getColor(R.color.sec_var_orange));
                } else {
                    highlightCard.setCardBackgroundColor(mView.getResources().getColor(R.color.icebergColor));
                }
            }
            );
        }


        @NonNull
        @Override
        public String toString() {
            return super.toString();
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.lnrFriends:
                    break;
            }
        }
    }

    public static class FriendsUI {
        Float amount;
        String friendsName;

        public Float getAmount() {
            return amount;
        }

        public void setAmount(Float amount) {
            this.amount = amount;
        }

        public String getFriendsName() {
            return friendsName;
        }

        public void setFriendsName(String friendsName) {
            this.friendsName = friendsName;
        }
    }
}
