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
package com.huawei.codelabs.splitbill.ui.main.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.huawei.codelabs.splitbill.R;
import com.huawei.codelabs.splitbill.databinding.MainActivityBinding;
import com.huawei.codelabs.splitbill.ui.main.helper.NearbyAgent;
import com.huawei.codelabs.splitbill.ui.main.viewmodels.ExpenseViewModel;
import com.huawei.codelabs.splitbill.ui.main.viewmodels.ExpenseViewModelFactory;
import com.huawei.codelabs.splitbill.ui.main.viewmodels.FriendsViewModel;
import com.huawei.codelabs.splitbill.ui.main.viewmodels.FriendsViewModelFactory;
import com.huawei.codelabs.splitbill.ui.main.viewmodels.GroupViewModel;
import com.huawei.codelabs.splitbill.ui.main.viewmodels.GroupViewModelFactory;

// Main Activity to hold fragment
public class MainActivity extends BaseActivity {
    //FragmentName store the name of Group and show it via setActionTitleBar method
    public static String fragmentName = "";
    public NavController navController;
    public MainActivityBinding mainActivityBinding;
    AppBarConfiguration appBarConfiguration;
    AndroidViewModel androidViewModel;
    public NearbyAgent nearbyAgent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivityBinding = DataBindingUtil.setContentView(this, R.layout.main_activity);
        initView();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NearbyAgent.REQUEST_CODE_SCAN_ONE) {
            nearbyAgent.onScanResult(data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void initView() {
        nearbyAgent = new NearbyAgent(this);
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_groups, R.id.navigation_activity, R.id.navigation_account)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(mainActivityBinding.navView, navController);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    public AndroidViewModel createViewModel(Fragment fragment) {
        androidViewModel = new ViewModelProvider(fragment, new GroupViewModelFactory(this)).get(GroupViewModel.class);
        return androidViewModel;
    }


    //ExpenseViewModel method to create a ViewModel via given factory
    public ExpenseViewModel createExpenseViewModel(Fragment fragment) {
        return new ViewModelProvider(fragment, new ExpenseViewModelFactory(this)).get(ExpenseViewModel.class);
    }

    //FriendsViewModel Method to create a ViewModel via given factory
    public FriendsViewModel createFriendsViewModel(Fragment fragment) {
        return new ViewModelProvider(fragment, new FriendsViewModelFactory(this)).get(FriendsViewModel.class);
    }

    //To change the action title bar
    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

}