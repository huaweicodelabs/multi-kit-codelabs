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

package com.hms.quickline.ui.login

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.hms.quickline.core.base.BaseViewModel
import com.hms.quickline.data.model.HuaweiAuthResult
import com.hms.quickline.data.Resource
import com.hms.quickline.data.model.Users
import com.hms.quickline.domain.repository.CloudDbWrapper
import com.hms.quickline.domain.usecase.LoginUseCase
import com.huawei.agconnect.auth.AGConnectUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val loginUseCase: LoginUseCase) : BaseViewModel() {

    private val TAG = "LoginViewModel"

    private val signInHuaweiIdLiveData: MutableLiveData<Resource<AGConnectUser?>> = MutableLiveData()
    fun getSignInHuaweiIdLiveData(): LiveData<Resource<AGConnectUser?>> = signInHuaweiIdLiveData

    private val checkUserLiveData: MutableLiveData<Boolean> = MutableLiveData()
    fun getCheckUserLiveData(): LiveData<Boolean> = checkUserLiveData

    fun signInWithHuaweiId(requestCode: Int, data: Intent?) {
        viewModelScope.launch {
            signInHuaweiIdLiveData.value = Resource.loading()

            when (val result = loginUseCase.signInWithHuaweiId(requestCode, data)) {
                is HuaweiAuthResult.UserSuccessful -> {
                    signInHuaweiIdLiveData.value = Resource.success(result.user)
                }
                is HuaweiAuthResult.UserFailure -> {
                    signInHuaweiIdLiveData.value = result.errorMessage?.let { Resource.Failed(it) }
                }
            }
        }
    }

    /**
     * Check user exists in CloudDatabase
     */
    fun checkUserLogin(userId: String) {
        CloudDbWrapper.checkUserById(userId, object : CloudDbWrapper.ResultListener {

            override fun onSuccess(result: Any?) {
                val resultList: ArrayList<Users>? = result as? ArrayList<Users>

                resultList?.forEach {
                    checkUserLiveData.value = it.uid == userId
                }
            }

            override fun onFailure(e: Exception) {
                e.localizedMessage?.let {
                    Log.e(TAG, it)
                }
            }
        })
    }
}