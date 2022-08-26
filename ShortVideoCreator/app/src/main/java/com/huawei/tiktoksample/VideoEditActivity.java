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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.SignInResult;
import com.huawei.agconnect.cloud.storage.core.StorageReference;
import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.hms.videoeditor.ui.api.DraftInfo;
import com.huawei.hms.videoeditor.ui.api.MediaApplication;
import com.huawei.hms.videoeditor.ui.api.MediaExportCallBack;
import com.huawei.hms.videoeditor.ui.api.MediaInfo;
import com.huawei.hms.videoeditor.ui.api.VideoEditorLaunchOption;
import com.huawei.tiktoksample.adapter.DraftAdapter;
import com.huawei.tiktoksample.db.clouddb.VideoUpload;
import com.huawei.tiktoksample.util.AppLog;
import com.huawei.tiktoksample.util.LoginHelper;
import com.huawei.tiktoksample.util.OnApiError;
import com.huawei.tiktoksample.util.PermissionUtils;
import com.huawei.tiktoksample.viewmodel.TikTokSampleViewModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VideoEditActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener, LoginHelper.OnLoginEventCallBack {
    private static final String TAG = "VideoEditActivity";
    private static final int PERMISSION_REQUESTS = 1;
    private ImageView mSetting;
    private Context mContext;
    static final int REQUEST_VIDEO_CAPTURE = 1;
    private VideoView videoView;
    private TikTokSampleViewModel tiktokStorageViewModel;
    private RecyclerView recyclerView;
    private DraftAdapter draftAdapter;
    private List<DraftInfo> draftInfos;
    private int position;
    private AlertDialog alert;
    private String videoName;
    public static final String MY_PREFS_NAME = "MyPrefsFile";
    private ProgressBar pbHeaderProgress;
    private final String[] PERMISSIONS = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.activity_video_edit);
        pbHeaderProgress=(ProgressBar) findViewById(R.id.pbHeaderProgress);
        pbHeaderProgress.setVisibility(View.GONE);
        Handler mHandler = new Handler(Looper.getMainLooper());
        if (AGConnectAuth.getInstance().getCurrentUser() == null) {
            mHandler.post(() -> {
                LoginHelper loginHelper = new LoginHelper(VideoEditActivity.this);
                loginHelper.addLoginCallBack(this);
                loginHelper.login();
            });
        }
        videoView = (VideoView) findViewById(R.id.simpleVideoView);
        tiktokStorageViewModel = new ViewModelProvider(VideoEditActivity.this).get(TikTokSampleViewModel.class);
        getReferDialog();
    }
    public void getReferDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        final View customLayout = getLayoutInflater().inflate(R.layout.video_name_alert, null);
        alertDialog.setView(customLayout);
        alert = alertDialog.create();
        alert.setCancelable(false);
        alert.setCanceledOnTouchOutside(false);
        EditText editText = customLayout.findViewById(R.id.etComments);
        Button submitBtn = customLayout.findViewById(R.id.btnSubmit);
        Button btnCancel = customLayout.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> {
            alert.dismiss();
            Intent myIntent = new Intent(VideoEditActivity.this, HomeActivity.class);
            this.startActivity(myIntent);
            finish();
        });
        submitBtn.setOnClickListener(v -> {
            String userText = editText.getText().toString();
            if (!userText.equals("")) {
                alert.dismiss();
                videoName = editText.getText().toString();
                initSetting();
                initView();
                initData();
                initEvent();
                requestPermission();
                pbHeaderProgress.setVisibility(View.VISIBLE);
            }
        });
        alert.show();
    }


    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = intent.getData();
            videoView.setVideoURI(videoUri);
            MediaController mediaController = new MediaController(this);
            videoView.setMediaController(mediaController);
            videoView.start();
            try {
                Log.e("videopath", getRealPathFromURI(intent.getData()));
                AssetFileDescriptor videoAsset = getContentResolver().openAssetFileDescriptor(intent.getData(), "r");
                FileInputStream fis = videoAsset.createInputStream();
                File root = new File(Environment.getExternalStorageDirectory(), "Pictures");
                if (!root.exists()) {
                    root.mkdirs();
                }
                File file;
                file = new File(root, "VideoEditor/" + System.currentTimeMillis() + ".mp4");
                File videoFile = new File(getRealPathFromURI(intent.getData()));
                if(videoFile.exists()){
                    InputStream in = new FileInputStream(videoFile);
                    OutputStream out = new FileOutputStream(file);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();
                    Log.v("TAG", "Copy file successful.");
                }else{
                    Log.v("TAG", "Copy file failed. Source file missing.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void requestPermission() {
        PermissionUtils.checkManyPermissions(mContext, PERMISSIONS, new PermissionUtils.PermissionCheckCallBack() {
            @Override
            public void onHasPermission() {
                startUIActivity();
            }
            @Override
            public void onUserHasReject(String... permission) {
                PermissionUtils.requestManyPermissions(mContext, PERMISSIONS, PERMISSION_REQUESTS); }
            @Override
            public void onUserRejectAndDontAsk(String... permission) {
                PermissionUtils.requestManyPermissions(mContext, PERMISSIONS, PERMISSION_REQUESTS); }
        });
    }
    private void initSetting() {
        UUID uuid = UUID.randomUUID();
        MediaApplication.getInstance().setLicenseId(uuid.toString());
        AGConnectServicesConfig config = AGConnectServicesConfig.fromContext(this);
        MediaApplication.getInstance().setApiKey(config.getString("client/api_key"));
        MediaApplication.getInstance().setOnMediaExportCallBack(CALL_BACK);
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(draftInfos != null) {
            draftInfos = MediaApplication.getInstance().getDraftList();
            draftAdapter.setData(draftInfos);
            draftAdapter.notifyDataSetChanged();
        }
    }
    private void initEvent() {
        draftAdapter.setSelectedListener(new DraftAdapter.OnStyleSelectedListener() {
            @Override
            public void onStyleSelected(int position) {
                String draftId = draftInfos.get(position).getDraftId();
                Log.d("Video_path", draftInfos.get(position).getDraftId()+" "+draftInfos.get(position).getDraftCoverPath());
                MediaApplication.getInstance()
                        .launchEditorActivity(VideoEditActivity.this,
                                new VideoEditorLaunchOption.Builder()
                                        .setStartMode(MediaApplication.START_MODE_IMPORT_FROM_DRAFT)
                                        .setDraftId(draftId)
                                        .build());
            }
        });

        draftAdapter.setLongSelectedListener(new DraftAdapter.OnStyleLongSelectedListener() {
            @Override
            public void onStyleLongSelected(View v, int position) {
                VideoEditActivity.this.position = position;
                PopupMenu popup = new PopupMenu(VideoEditActivity.this, v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu, popup.getMenu());
                popup.setOnMenuItemClickListener(VideoEditActivity.this);
                popup.show();
            }
        });
        mSetting.setOnClickListener(v -> this.startActivity(new Intent(VideoEditActivity.this, SettingActivity.class)));
    }
    private void initData() {
        draftAdapter = new DraftAdapter(this);
        draftAdapter.setData(draftInfos);
        draftAdapter.notifyDataSetChanged();
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(draftAdapter);
    }
    private void initView() {
        mSetting = findViewById(R.id.setting);
        recyclerView = findViewById(R.id.draft_rv);
        tiktokStorageViewModel.uploadFileLiveData().observe(VideoEditActivity.this, new Observer<Uri>() {
            @Override
            public void onChanged(Uri uri) {
                insertVideoData(uri);
            }
        });
        tiktokStorageViewModel.insertUserVideoData().observe(this, isInserted -> {
            if(isInserted){
                Toast.makeText(VideoEditActivity.this, "Data inserted successfully", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(VideoEditActivity.this, "Error in inserting data", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void insertVideoData(Uri videoUri){
        Uri uri = Uri.parse(mediaPath);
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(VideoEditActivity.this, uri);
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        long timeInMillisec = Long.parseLong(time);
        retriever.release();
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String email = prefs.getString("email", "0");
        String mobile = prefs.getString("mobile", "0");
        String name = prefs.getString("name", "0");
        if(email.equalsIgnoreCase("0")){
            email=mobile;
        }
        long timeInLong = System.currentTimeMillis();
        VideoUpload videoUpload = new VideoUpload();
        videoUpload.setUserEmail(email);
        videoUpload.setUserName(name);
        videoUpload.setUserPhone(mobile);
        videoUpload.setVideoCreatedTime(timeInLong);
        videoUpload.setVideoDuration(timeInMillisec);
        videoUpload.setVideoNoOfComments(0);
        videoUpload.setVideoNoOfLikes(0);
        videoUpload.setVideoId(timeInLong);
        videoUpload.setVideoName(videoName);
        videoUpload.setVideoShadowFlag(true);
        videoUpload.setVideoUploadLink(videoUri.toString());
        tiktokStorageViewModel.insertUserVideoData(VideoEditActivity.this,videoUpload, (errorMessage, e) -> {
            AppLog.logE(getClass().getSimpleName(), e.getLocalizedMessage());
        });
        pbHeaderProgress.setVisibility(View.GONE);
        callFinish();
    }
    private void startUIActivity() {
        MediaApplication.getInstance().launchEditorActivity(this, null);
    }
    String mediaPath;
    private final MediaExportCallBack CALL_BACK = new MediaExportCallBack() {
        @Override
        public void onMediaExportSuccess(MediaInfo mediaInfo) {
            AppLog.logE(getClass().getSimpleName(),"length of media info "+mediaInfo.getMediaPath().length());
            mediaPath = mediaInfo.getMediaPath();
            Log.i(TAG, "The current video export path is" + mediaPath);
            uploadVideoToStorage(mediaPath);
        }
        @Override
        public void onMediaExportFailed(int errorCode) {
            Log.d(TAG, "errorCode" + errorCode);
        }
    };
    private void callFinish() {
        VideoEditActivity.this.finish();
    }
    private void showToAppSettingDialog() {
        new AlertDialog.Builder(this).setMessage(getString(R.string.permission_tips))
                .setPositiveButton(getString(R.string.setting), (dialog, which) -> PermissionUtils.toAppSetting(mContext))
                .setNegativeButton(getString(R.string.cancels), null)
                .show();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,@NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUESTS) {
            PermissionUtils.onRequestMorePermissionsResult(mContext, PERMISSIONS,
                    new PermissionUtils.PermissionCheckCallBack() {
                        @Override
                        public void onHasPermission() {
                            startUIActivity();
                        }
                        @Override
                        public void onUserHasReject(String... permission) {
                        }
                        @Override
                        public void onUserRejectAndDontAsk(String... permission) {
                            showToAppSettingDialog();
                        }
                    });
        }
    }
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                String draftId = draftInfos.get(VideoEditActivity.this.position).getDraftId();
                List<String> draftIds = new ArrayList<>();
                draftIds.add(draftId);
                MediaApplication.getInstance().deleteDrafts(draftIds);
                draftInfos = MediaApplication.getInstance().getDraftList();
                draftAdapter.setData(draftInfos);
                draftAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
        return false;
    }

    OnApiError onApiError = new OnApiError() {
        @Override
        public void onError(String errorMessage, Throwable e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(VideoEditActivity.this, "File upload failed", Toast.LENGTH_LONG).show();
                }
            });
        }
    };
    @Override
    public void onBackPressed() {
        Log.d("CDA", "onBackPressed Called");
        alert.dismiss();
        Intent myIntent = new Intent(VideoEditActivity.this, HomeActivity.class);
        this.startActivity(myIntent);
        finish();
    }
    private void uploadVideoToStorage(String filePath){
        File file = new File(filePath);
        StorageReference storageReference = TikTokSampleApplication.getStorageManagement().getStorageReference("tiktoksample/user_video/"+file.getName());
        tiktokStorageViewModel.uploadFile(storageReference,
                file.getName(), file, onApiError);
    }


    @Override
    public void onLogin(boolean showLoginUserInfo, SignInResult signInResult) {
    }

    @Override
    public void onLogOut(boolean showLoginUserInfo) {
    }
}