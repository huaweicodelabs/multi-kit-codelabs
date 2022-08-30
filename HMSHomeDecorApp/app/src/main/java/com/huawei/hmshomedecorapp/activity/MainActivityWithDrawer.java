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

package com.huawei.hmshomedecorapp.activity;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.huawei.hmshomedecorapp.R;
import com.huawei.hmshomedecorapp.databinding.ActivityMainWithDrawerBinding;
import com.huawei.hmshomedecorapp.model.ItemModel;
import com.huawei.hmshomedecorapp.utils.Constants;
import com.huawei.hmshomedecorapp.utils.CountDrawable;
import com.huawei.hmshomedecorapp.utils.SharedPreferenceUtilClass;
import com.huawei.hmshomedecorapp.view.CircularImageView;

public class MainActivityWithDrawer extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainWithDrawerBinding binding;
    SharedPreferenceUtilClass sharedPreferenceUtilClass;
    NavController navController;
    Menu defaultMenu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainWithDrawerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.appBarMainActivityWithDrawer.toolbar);
        binding.appBarMainActivityWithDrawer.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        View headerView = navigationView.getHeaderView(0);
        TextView textView = (TextView) headerView.findViewById(R.id.user_name);
        TextView textViewEmail = (TextView) headerView.findViewById(R.id.textView);
        CircularImageView profileImage = (CircularImageView) headerView.findViewById(R.id.imageView);
        sharedPreferenceUtilClass = SharedPreferenceUtilClass.getInstance(MainActivityWithDrawer.this);
        String email = sharedPreferenceUtilClass.getData(Constants.USER_EMAIL);
        String name = sharedPreferenceUtilClass.getData(Constants.USER_NAME);
        String profileURL = sharedPreferenceUtilClass.getData(Constants.USER_PROFILE_IMAGE);
        textView.setText(name);
        textViewEmail.setText(email);
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(R.mipmap.ic_launcher_round)
                .error(R.mipmap.ic_launcher_round);
        Glide.with(this).load(profileURL).apply(options).into(profileImage);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setOpenableLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main_activity_with_drawer);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_with_drawer, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        defaultMenu = menu;
        String cartValueInString = sharedPreferenceUtilClass.getData(Constants.ADD_TO_CART_KEY);
        setCount(this,String.valueOf(cartValueInString));
        return super.onPrepareOptionsMenu(menu);
    }

    public void setCount(Context context, String count) {
        MenuItem menuItem = defaultMenu.findItem(R.id.ic_group);
        LayerDrawable icon = (LayerDrawable) menuItem.getIcon();
        CountDrawable badge;
        Drawable reuse = icon.findDrawableByLayerId(R.id.ic_group_count);
        if (reuse != null && reuse instanceof CountDrawable) {
            badge = (CountDrawable) reuse;
        } else {
            badge = new CountDrawable(context);
        }

        badge.setCount(count);
        icon.mutate();
        icon.setDrawableByLayerId(R.id.ic_group_count, badge);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navigationController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main_activity_with_drawer);
        return NavigationUI.navigateUp(navigationController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void navigateToParticularFragment(ItemModel itemModel) {
            Bundle itemDetails = new Bundle();
            itemDetails.putString("Item Name",itemModel.getApplianceName());
            itemDetails.putInt("Item Image",itemModel.getImageId());
            itemDetails.putString("Item ThreeDFile",itemModel.getThreeDimensionalFileName());
            navController.navigate(R.id.nav_gallery,itemDetails);
    }

}