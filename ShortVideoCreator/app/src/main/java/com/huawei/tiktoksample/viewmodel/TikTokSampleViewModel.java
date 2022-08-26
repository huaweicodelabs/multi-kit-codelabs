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

package com.huawei.tiktoksample.viewmodel;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.huawei.agconnect.cloud.database.CloudDBZoneObjectList;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.agconnect.cloud.storage.core.StorageReference;
import com.huawei.agconnect.cloud.storage.core.UploadTask;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.tiktoksample.R;
import com.huawei.tiktoksample.TikTokSampleApplication;
import com.huawei.tiktoksample.db.clouddb.User;
import com.huawei.tiktoksample.db.clouddb.VideoComments;
import com.huawei.tiktoksample.db.clouddb.VideoUpload;
import com.huawei.tiktoksample.db.dao.CloudDBHelper;
import com.huawei.tiktoksample.util.AppLog;
import com.huawei.tiktoksample.util.OnApiError;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TikTokSampleViewModel extends ViewModel {

    private static final String TAG = "TikTokSampleViewModel";
    private MutableLiveData<List<VideoUpload>> userUploadedVideoList;
    private MutableLiveData<List<VideoComments>> userVideoComentsList;
    private MutableLiveData<Uri> uploadFileLiveData;
    private MutableLiveData<Boolean> insertVideoData;

    private MutableLiveData<Integer> insertVideoComment;
    private MutableLiveData<Integer> updateVideoLike;

    private OnApiError onApiError;

    public LiveData<Integer> insertCommentForVideo() {
        if (insertVideoData == null) {
            insertVideoComment = new MutableLiveData<>();
        }
        return insertVideoComment;
    }

    public LiveData<Integer> updateLikeForVideo() {
        if (updateVideoLike == null) {
            updateVideoLike = new MutableLiveData<>();
        }
        return updateVideoLike;
    }


    public LiveData<Uri> uploadFileLiveData() {
        if (uploadFileLiveData == null) {
            uploadFileLiveData = new MutableLiveData<>();
        }
        return uploadFileLiveData;
    }

    public LiveData<Boolean> insertUserVideoData() {
        if (insertVideoData == null) {
            insertVideoData = new MutableLiveData<>();
        }
        return insertVideoData;
    }

    public void uploadFile(StorageReference reference, String fileName, File filePath, OnApiError onApiError) {
        this.onApiError = onApiError;
        UploadTask task = reference.putFile(filePath);
        task.addOnSuccessListener(uploadSuccessListener)
                .addOnFailureListener(uploadFailureListener);
    }

    OnSuccessListener<UploadTask.UploadResult> uploadSuccessListener = new OnSuccessListener<UploadTask.UploadResult>() {
        @Override
        public void onSuccess(UploadTask.UploadResult uploadResult) {
            uploadResult.getStorage().getDownloadUrl().addOnSuccessListener(downloadLink)
                    .addOnFailureListener(downloadUriFailureListener);
        }
    };

    OnSuccessListener<Uri> downloadLink = new OnSuccessListener<Uri>() {
        @Override
        public void onSuccess(Uri uri) {
            uploadFileLiveData.postValue(uri);
        }
    };

    OnFailureListener uploadFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(Exception e) {
            if (onApiError != null) {
                onApiError.onError("Error in uploading file to server", e);
            }
        }
    };

    OnFailureListener downloadUriFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(Exception e) {
            if (onApiError != null) {
                onApiError.onError("Failed in getting uri", e);
            }
        }
    };


    public void insertUserVideoData(Context context, VideoUpload videoupload, OnApiError onApiError) {
        CloudDBHelper.getInstance().openDb((isConnected, cloudDBZone) -> {
            if (isConnected && cloudDBZone != null) {
                if (cloudDBZone == null) {
                    return;
                } else {
                    Task<Integer> insertTask = cloudDBZone.executeUpsert(videoupload);
                    insertTask.addOnSuccessListener(integer -> {
                        insertVideoData.setValue(true);
                        CloudDBHelper.getInstance().closeDb(context);
                    }).addOnFailureListener(e -> {
                        onApiError.onError(e.getLocalizedMessage(), e);
                        CloudDBHelper.getInstance().closeDb(context);
                    });
                }
            }
        });
    }

    public void insertUserData(Context context, User userData, OnApiError onApiError) {
        CloudDBHelper.getInstance().openDb((isConnected, cloudDBZone) -> {
            if (isConnected && cloudDBZone != null) {
                if (cloudDBZone == null) {
                    return;
                } else {
                    Task<Integer> insertTask = cloudDBZone.executeUpsert(userData);
                    insertTask.addOnSuccessListener(integer -> {
                        insertVideoData.setValue(true);
                        CloudDBHelper.getInstance().closeDb(context);
                    }).addOnFailureListener(e -> {
                        onApiError.onError(e.getLocalizedMessage(), e);
                        CloudDBHelper.getInstance().closeDb(context);
                    });
                }
            }
        });
    }

    public void getUserList(Context context, OnApiError onApiError) {
        CloudDBHelper.getInstance().openDb((isConnected, cloudDBZone) -> {
            if (cloudDBZone == null) {
                AppLog.logE(TAG, "CloudDBZone is null, try re-open it");
                return;
            }
            Task<CloudDBZoneSnapshot<User>> queryTask = cloudDBZone.executeQuery(
                    CloudDBZoneQuery.where(User.class),
                    CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
            queryTask.addOnSuccessListener((OnSuccessListener<CloudDBZoneSnapshot<User>>) snapshot -> processQueryResult(snapshot)).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    AppLog.logE(getClass().getName(), "Error in getting data from user table");
                }
            });

        });
    }

    private void processQueryResult(CloudDBZoneSnapshot<User> snapshot) {
        CloudDBZoneObjectList<User> bookInfoCursor = snapshot.getSnapshotObjects();
        List<User> bookInfoList = new ArrayList<>();
        try {
            while (bookInfoCursor.hasNext()) {
                User userInfo = bookInfoCursor.next();
                bookInfoList.add(userInfo);
            }
        } catch (AGConnectCloudDBException e) {
            AppLog.logE(TAG, "processQueryResult: " + e.getMessage());
        }

        AppLog.logE(getClass().getName(), "We got user data " + bookInfoList.toString());
        snapshot.release();
    }


    public void updateLikeCount(VideoUpload videoUpload) {
        CloudDBHelper.getInstance().openDb((isConnected, cloudDBZone) -> {
            if (cloudDBZone == null) {
                AppLog.logE(TAG, "CloudDBZone is null, try re-open it");
                return;
            }
            Task<Integer> queryTask = cloudDBZone.executeUpsert(videoUpload);
            queryTask
                    .addOnSuccessListener(integer ->
                            updateVideoLike.postValue(integer)
                    )
                    .addOnFailureListener(e -> {
                        onApiError.onError(e.getLocalizedMessage(), e);
            });

        });
    }

    public void enterCommentForVideo(VideoComments videoComments, OnApiError onApiError) {

        CloudDBHelper.getInstance().openDb((isConnected, cloudDBZone) -> {

            if (cloudDBZone == null) {
                AppLog.logE(TAG, TikTokSampleApplication.getInstance().getString(R.string.err_cloud_db_zone));
                onApiError.onError(TikTokSampleApplication.getInstance().getString(R.string.err_cloud_db_zone), new Throwable(TikTokSampleApplication.getInstance().getString(R.string.err_cloud_db_zone)));
                return;
            }

            Task<Integer> enterComment = cloudDBZone.executeUpsert(videoComments);
            enterComment.addOnSuccessListener(integer -> {
                insertVideoComment.postValue(integer);
            }).addOnFailureListener(e -> {
                onApiError.onError(e.getMessage(), e);
            });

        });

    }
    public LiveData<List<VideoUpload>> getUsersListOfVideo(){
        if(userUploadedVideoList==null){
            userUploadedVideoList = new MutableLiveData<>();
        }
        return userUploadedVideoList;
    }
    public LiveData<List<VideoComments>> getUsersListOfComments(){
        if(userVideoComentsList==null){
            userVideoComentsList = new MutableLiveData<>();
        }
        return userVideoComentsList;
    }
    public void getAllUserUploadedVideoList(String userEmail, OnApiError onApiError){

        CloudDBHelper.getInstance().openDb((isConnected, cloudDBZone) -> {
            if (cloudDBZone == null) {
                AppLog.logE(TAG, "CloudDBZone is null, try re-open it");
                return;
            }
            Task<CloudDBZoneSnapshot<VideoUpload>> queryTask = cloudDBZone.executeQuery(
                    CloudDBZoneQuery.where(VideoUpload.class).equalTo("user_email", userEmail),
                    CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
            queryTask.addOnSuccessListener(this::processVideoUploadResult).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    AppLog.logE(getClass().getName(), "Error in getting data from user table");
                }
            });

        });

    }

    private void processVideoUploadResult(CloudDBZoneSnapshot<VideoUpload> snapshot) {
        CloudDBZoneObjectList<VideoUpload> bookInfoCursor = snapshot.getSnapshotObjects();
        List<VideoUpload> videoUploads = new ArrayList<>();
        try {
            while (bookInfoCursor.hasNext()) {
                VideoUpload userInfo = bookInfoCursor.next();
                videoUploads.add(userInfo);
            }
            userUploadedVideoList.postValue(videoUploads);
        } catch (AGConnectCloudDBException e) {
            AppLog.logE(TAG, "processQueryResult: " + e.getMessage());
        }

        AppLog.logE(getClass().getName(), "We got user data " + videoUploads.toString());
        snapshot.release();
    }
    public void getComments(Long videoId) {
        CloudDBHelper.getInstance().openDb((isConnected, cloudDBZone) -> {
            if (cloudDBZone == null) {
                AppLog.logE("TAG", "CloudDBZone is null, try re-open it");
                return;
            }
            Task<CloudDBZoneSnapshot<VideoComments>> queryTask = cloudDBZone.executeQuery(
                    CloudDBZoneQuery.where(VideoComments.class).equalTo("video_id",videoId),
                    CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
            queryTask.addOnSuccessListener(snapshot -> processQueryCommentsResult(snapshot))
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            AppLog.logE(getClass().getName(), "Error in getting data from user table");
                        }
                    });

        });
    }
    private void processQueryCommentsResult(CloudDBZoneSnapshot<VideoComments> snapshot) {
        CloudDBZoneObjectList<VideoComments> videoInfoCursor = snapshot.getSnapshotObjects();
        List<VideoComments> commentList = new ArrayList<>();
        try {
            while (videoInfoCursor.hasNext()) {
                VideoComments videoComments = videoInfoCursor.next();
                commentList.add(videoComments);
            }

            userVideoComentsList.postValue(commentList);
        } catch (AGConnectCloudDBException e) {
            AppLog.logE("TAG", "processQueryResult: " + e.getMessage());
        }
        snapshot.release();
        Log.e("new test", "new test");
    }
}
