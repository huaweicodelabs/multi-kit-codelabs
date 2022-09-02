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

package com.huawei.hms.knowmyboard.dtse.activity.viewmodel;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.huawei.hmf.tasks.OnCompleteListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.knowmyboard.dtse.R;
import com.huawei.hms.knowmyboard.dtse.activity.app.MyApplication;
import com.huawei.hms.location.LocationResult;
import com.huawei.hms.site.api.model.Site;
import com.huawei.hms.support.account.AccountAuthManager;
import com.huawei.hms.support.account.request.AccountAuthParams;
import com.huawei.hms.support.account.request.AccountAuthParamsHelper;
import com.huawei.hms.support.account.service.AccountAuthService;

import java.util.ArrayList;

public class LoginViewModel extends AndroidViewModel {
    AccountAuthService service;
    private MutableLiveData<String> message = new MutableLiveData<>();
    private MutableLiveData<ArrayList<String>> textRecognized = new MutableLiveData<>();
    private MutableLiveData<Bitmap> image = new MutableLiveData<>();
    private MutableLiveData<LocationResult> locationResult = new MutableLiveData<>();
    private MutableLiveData<Site> siteSelected = new MutableLiveData<Site>();

    public LiveData<Site> getSiteSelected() {
        return siteSelected;
    }

    public void setSiteSelected(Site siteSelected) {
        this.siteSelected.setValue(siteSelected);
    }

    public void sendData(String msg) {
        message.setValue(msg);
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public MutableLiveData<ArrayList<String>> getTextRecognized() {
        return textRecognized;
    }

    public void setImage(Bitmap imagePath) {
        this.image.setValue(imagePath);
    }

    public MutableLiveData<LocationResult> getLocationResult() {
        return locationResult;
    }

    public void setLocationResult(LocationResult locationResult) {
        this.locationResult.setValue(locationResult);
    }

    public void setTextRecognized(ArrayList<String> textRecongnized) {
        this.textRecognized.setValue(textRecongnized);
    }

    public MutableLiveData<Bitmap> getImagePath() {
        return image;
    }

    public LoginViewModel(@NonNull Application application) {
        super(application);
    }

    public void logoutHuaweiID() {
        if (service != null) {
            service.signOut();
            sendData("KnowMyBoard");
            Toast.makeText(getApplication(), "You are logged out from Huawei ID", Toast.LENGTH_LONG).show();
        }
    }

    public void loginClicked() {
        AccountAuthParams authParams =
                new AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                        .setAuthorizationCode()
                        .createParams();
        service = AccountAuthManager.getService(MyApplication.getActivity(), authParams);
        MyApplication.getActivity().startActivityForResult(service.getSignInIntent(), 8888);
    }

    public void cancelAuthorization() {
        if (service != null) {
            // service indicates the AccountAuthService instance generated using the getService method during the
            // sign-in authorization.
            service.cancelAuthorization()
                    .addOnCompleteListener(
                            new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Processing after a successful authorization cancellation.
                                        sendData(getApplication().getResources().getResourceName(R.string.app_name));
                                        Toast.makeText(getApplication(), "Cancelled authorization", Toast.LENGTH_LONG)
                                                .show();
                                    } else {
                                        // Handle the exception.
                                        Exception exception = task.getException();
                                        if (exception instanceof ApiException) {
                                            int statusCode = ((ApiException) exception).getStatusCode();
                                            Toast.makeText(
                                                            getApplication(),
                                                            "Failed to cancel authorization. status code " + statusCode,
                                                            Toast.LENGTH_LONG)
                                                    .show();
                                        }
                                    }
                                }
                            });
        } else {
            Toast.makeText(getApplication(), "Login required", Toast.LENGTH_LONG).show();
        }
    }
}
