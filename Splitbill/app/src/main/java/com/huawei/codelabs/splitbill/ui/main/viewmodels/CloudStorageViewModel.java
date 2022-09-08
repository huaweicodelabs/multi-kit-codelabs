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
package com.huawei.codelabs.splitbill.ui.main.viewmodels;

import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.huawei.agconnect.cloud.storage.core.StorageReference;
import com.huawei.agconnect.cloud.storage.core.UploadTask;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;

import java.io.File;

public class CloudStorageViewModel extends BaseViewModel{
    private static final String TAG = "ChitChatStorageViewModel";
    private MutableLiveData<Uri> uploadFileLiveData;

    public CloudStorageViewModel(@NonNull Application application) {
        super(application);
    }
    public LiveData<Uri> uploadFileLiveData() {
        if (uploadFileLiveData == null) {
            uploadFileLiveData = new MutableLiveData<>();
        }
        return uploadFileLiveData;
    }

    public void uploadFile(StorageReference reference, String fileName, File filePath) {
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
            Log.d(TAG,"Sucess DownloadLink"+uri.toString());
        }
    };

    OnFailureListener uploadFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(Exception e) {
                Log.d(TAG,"Error in uploading file to server", e);
        }
    };

    OnFailureListener downloadUriFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(Exception e) {
            Log.d(TAG,"Failed in getting uri", e);
        }
    };
}
