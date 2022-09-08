/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
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

package com.huawei.codelabs.splitbill.ui.main.repo;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.ListenerHandler;
import com.huawei.codelabs.splitbill.ui.main.db.CloudDBZoneWrapper;
import com.huawei.codelabs.splitbill.ui.main.helper.NetworkKitDownloadEngine;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.network.file.api.Progress;
import com.huawei.hms.network.file.api.Response;
import com.huawei.hms.network.file.api.Result;
import com.huawei.hms.network.file.api.exception.NetworkException;
import com.huawei.hms.network.file.download.api.DownloadManager;
import com.huawei.hms.network.file.download.api.FileRequestCallback;
import com.huawei.hms.network.file.download.api.GetRequest;

import java.io.Closeable;
import java.io.File;

public class BaseRepository extends NetworkKitDownloadEngine {
    protected AGConnectAuth agConnectAuth;
    protected CloudDBZone mCloudDBZone;
    protected ListenerHandler mRegister;
    private static final String TAG = "DownloadEngine";
    DownloadManager downloadManager;
    GetRequest getRequest;
    FileRequestCallback callback;

    public BaseRepository() {

    }


    public BaseRepository(Context context) {
        super(context);
    }

    /**
     * Open cloud DB and update status in Live data
     *
     * @param cloudDBZoneWrapper
     * @return boolean - open cloud DB status
     */
    public MutableLiveData<Boolean> initAndCheckCloudDBStatus(CloudDBZoneWrapper cloudDBZoneWrapper) {
        agConnectAuth = AGConnectAuth.getInstance();

        AGConnectUser currentUser = agConnectAuth.getCurrentUser();
        cloudDBZoneWrapper.createObjectType();
        MutableLiveData<Boolean> checkCloudDBStatusLiveData = new MutableLiveData<>();
        if (currentUser != null) {
            cloudDBZoneWrapper.createObjectType();
            Task<CloudDBZone> openDBZoneTask = cloudDBZoneWrapper.openCloudDBZoneV2();
            openDBZoneTask.addOnSuccessListener(new OnSuccessListener<CloudDBZone>() {
                @Override
                public void onSuccess(CloudDBZone cloudDBZone) {
                    Log.i("BaseRepository", "success: CloudDBZone ");
                    cloudDBZoneWrapper.setCloudDBZone(cloudDBZone);
                    checkCloudDBStatusLiveData.setValue(true);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    Log.i("BaseRepository", "failure: CloudDBZone ");
                    checkCloudDBStatusLiveData.setValue(false);
                }
            });
        }
        return checkCloudDBStatusLiveData;
    }


    //Network kit init method
    @Override
    protected void initManager() {
        //Download manager
        downloadManager = new DownloadManager.Builder("downloadManager")
                .build(context);

        callback = new FileRequestCallback() {
            @Override
            public GetRequest onStart(GetRequest request) {
                return request;
            }

            @Override
            public void onProgress(GetRequest request, Progress progress) {
                Log.i(TAG, "onProgress:" + progress);
            }

            @Override
            public void onSuccess(Response<GetRequest, File, Closeable> response) {
                String filePath = "";
                if (response.getContent() != null) {
                    filePath = response.getContent().getAbsolutePath();
                }
                Log.i(TAG, "onSuccess" + " for " + filePath);
            }

            @Override
            public void onException(GetRequest getRequest, NetworkException e, Response<GetRequest, File, Closeable> response) {
                if (e instanceof Exception) {
                    String errorMsg = "download exception for paused or canceled";
                    Log.w(TAG, errorMsg);
                } else {
                    String errorMsg = "download exception for request:" + getRequest.getId() +
                            "\n\ndetail : " + e.getMessage();
                    if (e.getCause() != null) {
                        errorMsg += " , cause : " +
                                e.getCause().getMessage();
                    }
                    Log.e(TAG, errorMsg);
                }
            }
        };
    }

    @Override
    public void download(String Url, String name) {
        Log.d(TAG, "URL : " + Url);
        expenseImageDownload(context, Url, name);

    }

    private void expenseImageDownload(Context context, String Url, String name) {
        if (downloadManager == null) {
            Log.e(TAG, "can not download without init");
            return;
        }
        // replace the path to store the file
        String nameOfFile = name + ".jpg";
        String downloadFilePath = context.getObbDir().getPath() + File.separator + nameOfFile;
        getRequest = DownloadManager.newGetRequestBuilder()
                .filePath(downloadFilePath)
                .url(Url)
                .build();
        Result result = downloadManager.start(getRequest, callback);
        checkResult(result);
    }
}
