/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
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

package com.huawei.tiktoksample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.BannerAdSize;
import com.huawei.hms.ads.banner.BannerView;
import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.result.AuthAccount;
import com.huawei.tiktoksample.adapter.ImageAdapter;
import com.huawei.tiktoksample.db.clouddb.VideoUpload;
import com.huawei.tiktoksample.model.Constants;
import com.huawei.tiktoksample.model.ImageModel;
import com.huawei.tiktoksample.ui.HomeFragment;
import com.huawei.tiktoksample.util.AppLog;
import com.huawei.tiktoksample.util.OnApiError;
import com.huawei.tiktoksample.viewmodel.TikTokSampleViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView imageRecyclerView;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    public static ArrayList<ImageModel> imageList;
    private ImageView userImage;
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    public static final String MY_PREFS_NAME = "MyPrefsFile";
    private TextView tvMyVideoTitle;
    private ProgressBar pbHeaderProgress;
    private int noOfVideo;
    private int videoCount=0;
    private RelativeLayout rlt;
    private TextView tvPW;
    private String videoURL;
    private String email;
    private List<VideoUpload> videoInfoList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView ivLogout = (ImageView) findViewById(R.id.ivLogout);
        ivLogout.setImageDrawable(changeDrawableColor(this,R.drawable.logout, Color.WHITE));
        pbHeaderProgress=(ProgressBar) findViewById(R.id.pbHeaderProgress);
        rlt= (RelativeLayout) findViewById(R.id.rlt);
        tvPW= (TextView) findViewById(R.id.tvPW);
        Toolbar toolbar = findViewById(R.id.toolbar);
        userImage= (ImageView) findViewById(R.id.userImage);
        ImageView editIcon = (ImageView) findViewById(R.id.editIcon);
        TextView titleTxt = (TextView) findViewById(R.id.tv_title);
        tvMyVideoTitle= (TextView) findViewById(R.id.tvMyVideoTitle);
        LoginActivity.profileImagePage=0;
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        email = prefs.getString("email", "0");
        String mobile = prefs.getString("mobile", "0");
        String name = prefs.getString("name", "0");
        String image = prefs.getString("image", "0");
        if(null !=image) {
            if(!image.equalsIgnoreCase("0")) {
                byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                userImage.setImageBitmap(decodedByte);
            }
        }
        titleTxt.setText(email);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        collapsingToolbarLayout=findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(mobile);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);
        Intent intent = getIntent();
        Bitmap bitmap = (Bitmap) intent.getParcelableExtra("NewBitmapImage");
        if(null !=bitmap) {
            userImage.setImageBitmap(bitmap);
        }
        userImage.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                }
                else {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
            }
        });
        editIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userImage.invalidate();
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                }
                else {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }

            }
        });
        init();
        if(null !=imageList) {
            if (imageList.size() == 0) {
                imageList = new ArrayList<>();
                myFun();
            } else {
                tvMyVideoTitle.setVisibility(View.VISIBLE);
                setImageList();
            }
        }
        else{
            imageList = new ArrayList<>();
            myFun();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent myIntent = new Intent(MainActivity.this, HomeActivity.class);
                this.startActivity(myIntent);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
    public void showLogOutDiloge(View view) {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences preferences =getSharedPreferences("MyPrefsFile",Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.clear();
                        editor.apply();
                        Intent myIntent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(myIntent);
                        finish();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getApplicationContext(),"Nothing Happened",Toast.LENGTH_LONG).show();
                    }
                })
                .show();
    }
    public static Drawable changeDrawableColor(Context context, int icon, int newColor) {
        Drawable mDrawable = ContextCompat.getDrawable(context, icon).mutate();
        mDrawable.setColorFilter(new PorterDuffColorFilter(newColor, PorterDuff.Mode.SRC_IN));
        return mDrawable;
    }
    void myFun(){
        if (HomeFragment.videoInfoList.size() > 0) {
            for (int i = 0; i < HomeFragment.videoInfoList.size(); i++) {
                VideoUpload videoUpload = HomeFragment.videoInfoList.get(i);
                if (videoUpload.getUserEmail().equalsIgnoreCase(email)) {
                    videoInfoList.add(videoUpload);
                }
            }
            noOfVideo = videoInfoList.size();
            for (int i = 0; i < videoInfoList.size(); i++) {
                VideoUpload videoUpload = videoInfoList.get(i);
                if (videoUpload.getUserEmail().equalsIgnoreCase(email)) {
                    videoURL = videoUpload.getVideoUploadLink().substring(0, videoUpload.getVideoUploadLink().indexOf(".mp4") + 4);
                    new DownLoadTask().execute(videoURL); }
            }
        } else {
            TikTokSampleViewModel viewModel = new ViewModelProvider(this)
                    .get(TikTokSampleViewModel.class);

            viewModel.getUsersListOfVideo().observe(this, new Observer<List<VideoUpload>>() {
                @Override
                public void onChanged(List<VideoUpload> videoUploads) {
                    videoInfoList = videoUploads;
                    AppLog.logE(getClass().getName(), "Video uploaded by users are ===>" + videoUploads.size());
                    noOfVideo = videoUploads.size();
                    for (int i = 0; i < videoUploads.size(); i++) {
                        VideoUpload videoUpload = videoUploads.get(i);
                        videoURL = videoUpload.getVideoUploadLink().substring(0, videoUpload.getVideoUploadLink().indexOf(".mp4") + 4);
                        new DownLoadTask().execute(videoURL);
                    }
                }
            });
            viewModel.getAllUserUploadedVideoList(email, new OnApiError() {
                @Override
                public void onError(String errorMessage, Throwable e) {
                    AppLog.logE(getClass().getName(), "Video uploaded by users are ===>" + e.getLocalizedMessage());
                }
            });
        }
    }
    public void init() {
        imageRecyclerView = findViewById(R.id.recycler_view);
        ArrayList<String> selectedImageList = new ArrayList<>();
    }
    public void setImageList() {
        imageRecyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 3));
        ImageAdapter imageAdapter = new ImageAdapter(MainActivity.this, imageList);
        imageRecyclerView.setAdapter(imageAdapter);
        pbHeaderProgress.setVisibility(View.GONE);
        tvPW.setVisibility(View.GONE);
        rlt.setVerticalGravity(View.GONE);
        callBannerAdd();
        imageAdapter.setOnItemClickListener(new ImageAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                String url=imageList.get(position).getVideoURL();
            }
        });
    }

    private void callBannerAdd() {
        AdParam adParam = new AdParam.Builder().build();
        BannerView topBannerView = new BannerView(this);
        topBannerView.setAdId(getString(R.string.banner_ad_id));
        topBannerView.setBannerAdSize(BannerAdSize.BANNER_SIZE_SMART);
        topBannerView.loadAd(adParam);
        RelativeLayout rootView = findViewById(R.id.root_view);
        rootView.addView(topBannerView);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
            else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            userImage.setImageBitmap(photo);
            Intent myIntent = new Intent(MainActivity.this, EditImageActivity.class);
            myIntent.putExtra("BitmapImage", photo);
            startActivity(myIntent);
        }
        if (requestCode == Constants.REQUEST_SIGN_IN_LOGIN) {
            Task<AuthAccount> authAccountTask = AccountAuthManager.parseAuthResultFromIntent(data);
            if (authAccountTask.isSuccessful()) {
                AuthAccount authAccount = authAccountTask.getResult();
                Log.i(Constants.APP_TAG, authAccount.getDisplayName() + " signIn success ");
                collapsingToolbarLayout.setTitle(authAccount.getDisplayName());
            }
        }
    }
    @Override
    public void onBackPressed() {
        Intent myIntent = new Intent(MainActivity.this, HomeActivity.class);
        this.startActivity(myIntent);
        finish();
    }
    class DownLoadTask extends AsyncTask<String, Integer, Bitmap> {
        String TAG = getClass().getSimpleName();
        protected void onPreExecute() {
            super.onPreExecute();
        }
        protected Bitmap doInBackground(String...arg0) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(arg0[0], new HashMap<String, String>());
            Bitmap bitmap = retriever.getFrameAtTime(1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            return bitmap;
        }
        protected void onProgressUpdate(Integer...a) {
            super.onProgressUpdate(a);
        }
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            videoCount++;
            ImageModel imageModel = new ImageModel();
            imageModel.setImage(result);
            imageModel.setVideoURL(videoInfoList.get(videoCount-1).getVideoUploadLink().substring(0,videoInfoList.get(videoCount-1).getVideoUploadLink().indexOf(".mp4")+4));
            imageModel.setVideoName(videoInfoList.get(videoCount-1).getVideoName());
            imageModel.setUserName(videoInfoList.get(videoCount-1).getUserName());
            imageModel.setUserMobile(videoInfoList.get(videoCount-1).getUserPhone());
            imageModel.setUserEmail(videoInfoList.get(videoCount-1).getUserEmail());
            imageModel.setNoOfLike(videoInfoList.get(videoCount-1).getVideoNoOfLikes());
            imageModel.setNoOfComments(videoInfoList.get(videoCount-1).getVideoNoOfComments());
            imageList.add(imageModel);
            if(noOfVideo ==videoCount) {
                tvMyVideoTitle.setVisibility(View.VISIBLE);
                setImageList();
            }
        }
    }
}