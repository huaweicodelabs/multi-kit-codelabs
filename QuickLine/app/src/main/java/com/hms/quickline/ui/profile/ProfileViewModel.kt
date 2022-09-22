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

package com.hms.quickline.ui.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hms.quickline.core.base.BaseViewModel
import com.hms.quickline.data.model.UserEntity
import com.hms.quickline.data.model.Users
import com.hms.quickline.domain.repository.CloudDbWrapper
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.cloud.database.CloudDBZone
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(private val agConnectAuth: AGConnectAuth) :
    BaseViewModel() {
    private val TAG = "ProfileViewModel"

    private val availableLiveData: MutableLiveData<Boolean> = MutableLiveData()
    fun getAvailableLiveData(): LiveData<Boolean> = availableLiveData

    private val userListLiveData: MutableLiveData<ArrayList<Users>> = MutableLiveData()
    fun getUserListLiveData(): LiveData<ArrayList<Users>> = userListLiveData

    private val userLiveData: MutableLiveData<Users> = MutableLiveData()
    fun getUserLiveData(): LiveData<Users> = userLiveData

    private val _userData = MutableLiveData<UserEntity>()
    val userData: LiveData<UserEntity>
        get() = _userData

    fun checkAvailable(id: String) {
        CloudDbWrapper.getUserById(id, object : CloudDbWrapper.ICloudDbWrapper {
            override fun onUserObtained(users: Users) {
                availableLiveData.value = users.isAvailable
            }
        })
    }

    fun updateAvailable(id: String, isAvailable: Boolean, cloudDBZone: CloudDBZone) {
        Log.d(TAG, "updateAvailable: ")
        CloudDbWrapper.getUserById(id, object : CloudDbWrapper.ICloudDbWrapper {
            override fun onUserObtained(users: Users) {

                users.isAvailable = isAvailable

                cloudDBZone.executeUpsert(users)?.addOnSuccessListener { cloudDBZoneResult ->
                    Log.i("ProfileFragmentBusy", "Available data success: $cloudDBZoneResult")
                }?.addOnFailureListener {
                    Log.e("ProfileFragmentBusy", "Available data failed: ${it.message}")
                }
            }
        })
    }

    fun getUserList() {
        CloudDbWrapper.queryUsers {
            userListLiveData.value = it
        }
    }

    fun getUser(uid: String) {
        CloudDbWrapper.getUserById(uid, object : CloudDbWrapper.ICloudDbWrapper {
            override fun onUserObtained(users: Users) {
                userLiveData.value = users
            }
        })
    }

    fun getUserInfo() {
        val user = UserEntity(
            email = agConnectAuth.currentUser.email,
            displayName = agConnectAuth.currentUser.displayName,
            photoUrl = agConnectAuth.currentUser.photoUrl,
        )
        _userData.value = user
    }

    fun signOut() {
        agConnectAuth.signOut()
    }
}