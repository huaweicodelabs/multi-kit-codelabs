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

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.huawei.codelabs.splitbill.R;
import com.huawei.codelabs.splitbill.databinding.FragmentGroupBinding;
import com.huawei.codelabs.splitbill.databinding.ItemGroupDisplayBinding;

import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {
    public static final String TAG = "GroupAdapter";
    FragmentGroupBinding fragmentGroupBinding;
    private final List<GroupUI> groupList;

    public GroupAdapter(List<GroupUI> myLists, FragmentGroupBinding fragmentGroupBinding) {
        this.groupList = myLists;
        this.fragmentGroupBinding = fragmentGroupBinding;
    }

    //Added Item group display card as viewholder object
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemGroupDisplayBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false));
    }

    //Setting up the data in ItemGroupDisplay Card
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            GroupUI groupModel = groupList.get(position);
            holder.textView.setText(groupModel.getGroupName());
            StringBuilder participantsString = new StringBuilder();
            for (String participant : groupModel.getParticipants()) {
                participantsString.append(participant).append(", ");
            }
            holder.participantsNames.setText(participantsString.toString());
            if (groupModel.getmGroupProfilePic() == null) {
                holder.imageView.setImageResource(R.drawable.group);
            } else {
                Glide.with(fragmentGroupBinding.getRoot()).load(groupModel.getmGroupProfilePic()).into(holder.imageView);
            }
        } catch (Exception e) {
            Log.d(TAG, "OnBindViewHodler ", e);
        }
    }

    //GroupList size
    @Override
    public int getItemCount() {
        return groupList.size();
    }

    private void showContent(int position, Bundle bundle) {
        GroupUI groupModel = groupList.get(position);

        int groupId = groupModel.getGroupId();
        String groupName = groupModel.getGroupName();
        String groupDescription = groupModel.getGroupDescription();
        String groupProfile = groupModel.getmGroupProfilePic();
        StringBuilder participantsString = new StringBuilder();
        for (String participant : groupModel.getParticipants()) {
            participantsString.append(participant).append(", ");
        }
        int groupStatus = groupModel.getGroupStatus();
        if (groupStatus == 1) {
            String groupStats = "Active";
            bundle.putString("members", groupStats);
        } else {
            String groupStats = "Inactive";
            bundle.putString("members", groupStats);
        }
        bundle.putString("group_profile", groupProfile);
        bundle.putInt("group_id", groupId);
        bundle.putString("group_name", groupName);
        bundle.putString("description_name", groupDescription);
        bundle.putString("members_name", String.valueOf(participantsString));

    }

    public static class GroupUI {
        private Integer groupId;
        private String mGroupName;
        private Integer mGroupStatus;
        private String mGroupDescription;
        private List<String> mParticipants;
        private String mGroupProfilePic;

        public String getGroupName() {
            return mGroupName;
        }

        public void setGroupName(String groupName) {
            mGroupName = groupName;
        }

        public List<String> getParticipants() {
            return mParticipants;
        }

        public void setParticipants(List<String> participants) {
            mParticipants = participants;
        }

        public Integer getGroupStatus() {
            return mGroupStatus;
        }

        public void setGroupStatus(Integer mGroupStatus) {
            this.mGroupStatus = mGroupStatus;
        }

        public String getGroupDescription() {
            return mGroupDescription;
        }

        public void setGroupDescription(String mGroupDescription) {
            this.mGroupDescription = mGroupDescription;
        }

        public Integer getGroupId() {
            return groupId;
        }

        public void setGroupId(Integer groupId) {
            this.groupId = groupId;
        }

        public String getmGroupProfilePic() {
            return mGroupProfilePic;
        }

        public void setmGroupProfilePic(String mGroupProfilePic) {
            this.mGroupProfilePic = mGroupProfilePic;
        }
    }

    //ViewHolder Class with onClickListener
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView textView;
        public TextView participantsNames;
        public ImageView imageView;
        public RelativeLayout groupListDetail;

        public ViewHolder(@NonNull ItemGroupDisplayBinding itemView) {
            super(itemView.getRoot());
            this.textView = itemView.textviewFirst;
            this.participantsNames = itemView.textviewSecond;
            this.imageView = itemView.imageviewGroup;
            this.groupListDetail = itemView.groupListDetail;
            itemView.getRoot().setOnClickListener(this);
            groupListDetail.setOnClickListener(this);
        }

        //OnClick from View Holder
        @Override
        public void onClick(View view) {
            Log.d(TAG, "Clicked");
            if (view.getId() == R.id.groupListDetail) {
                Bundle bundle = new Bundle();
                int position = this.getAdapterPosition();
                showContent(position, bundle);
                Navigation.findNavController(fragmentGroupBinding.getRoot()).navigate(R.id.groupDetailFragment, bundle);
            }
        }
    }
}

