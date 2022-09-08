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
package com.huawei.codelabs.splitbill.ui.main.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.codelabs.splitbill.R;
import com.huawei.codelabs.splitbill.databinding.FragmentGroupBinding;
import com.huawei.codelabs.splitbill.ui.main.activities.MainActivity;
import com.huawei.codelabs.splitbill.ui.main.adapter.GroupAdapter;
import com.huawei.codelabs.splitbill.ui.main.models.Group;
import com.huawei.codelabs.splitbill.ui.main.models.User;
import com.huawei.codelabs.splitbill.ui.main.viewmodels.GroupViewModel;

import java.util.ArrayList;
import java.util.List;

public class GroupFragment extends Fragment implements View.OnClickListener {
    public static final String TAG = "Group Fragment";
    FragmentGroupBinding fragmentGroupBinding;
    NavController navController;
    Bundle addUpdateBundle = new Bundle();
    private RecyclerView rcGroup;
    private GroupAdapter groupAdapter;
    private GroupViewModel groupViewModel;
    private List<User> userList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentGroupBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_group, container, false);
        View rootView = fragmentGroupBinding.getRoot();
        onScroll();
        init();
        return rootView;
    }

    // On scroll is used when we scroll the recycle view of group fragment then the floating action button
    // hide for that particular moment
    private void onScroll() {
        fragmentGroupBinding.rcGroup.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 || dy < 0 && fragmentGroupBinding.createGroup.isShown()) {
                    fragmentGroupBinding.createGroup.hide();
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    fragmentGroupBinding.createGroup.show();
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }

    //To resume the navigation bar
    @Override
    public void onResume() {
        ((MainActivity) getActivity()).mainActivityBinding.navView.setVisibility(View.VISIBLE);
        super.onResume();
    }

    // Init fragment view
    private void init() {
        fragmentGroupBinding.createGroup.setOnClickListener(this);
        groupViewModel = (GroupViewModel) ((MainActivity) getActivity()).createViewModel(this);
        rcGroup = fragmentGroupBinding.rcGroup;
        rcGroup.setHasFixedSize(true);
        rcGroup.setLayoutManager(new LinearLayoutManager(getActivity()));

        groupViewModel.getUserLiveData().observe(getActivity(), users -> {
                    userList = users;
                    getGroupData();
                }
        );

    }

    private void getGroupData() {
        groupViewModel.getGroupsLiveData().observe(getActivity(), groups -> {
            List<GroupAdapter.GroupUI> groupUIs = getGroupDetails(groups);
            groupAdapter = new GroupAdapter(groupUIs, fragmentGroupBinding);
            rcGroup.setAdapter(groupAdapter);
        });
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.createGroup) {
            ((MainActivity) getActivity()).mainActivityBinding.navView.setVisibility(View.GONE);
            addUpdateBundle.putString("add_update_mode", "add");
            navController = Navigation.findNavController(view);
            navController.navigate(R.id.createGroupFragment, addUpdateBundle);
        }

    }

    // Move it to Util class
    private List<GroupAdapter.GroupUI> getGroupDetails(List<Group> groups) {
        List<GroupAdapter.GroupUI> groupUIList = new ArrayList<>();
        for (Group var : groups) {
            GroupAdapter.GroupUI groupUI = new GroupAdapter.GroupUI();
            groupUI.setGroupName(var.getName());
            String[] participants = var.getUser_ids().split(",");
            List<String> participantsName = new ArrayList<>();
            for (String participant : participants) {
                for (User user : userList) {
                    if (user.getAgc_user_id().equals(participant)) {
                        participantsName.add(user.getName());
                    }
                }
            }
            groupUI.setGroupId(var.getId());
            groupUI.setGroupStatus(var.getStatus());
            groupUI.setGroupDescription(var.getDescription());
            groupUI.setParticipants(participantsName);
            groupUI.setmGroupProfilePic(var.getProfile_pic());
            groupUIList.add(groupUI);
        }
        return groupUIList;
    }
}