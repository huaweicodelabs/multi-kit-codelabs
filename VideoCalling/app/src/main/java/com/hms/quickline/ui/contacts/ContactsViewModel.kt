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

package com.hms.quickline.ui.contacts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hms.quickline.core.base.BaseViewModel
import com.hms.quickline.data.model.Users
import com.hms.quickline.domain.repository.CloudDbWrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor () : BaseViewModel() {

    private val userLiveData: MutableLiveData<Users> = MutableLiveData()
    fun getUserLiveData(): LiveData<Users> = userLiveData

    private val userListLiveData: MutableLiveData<ArrayList<Users>> = MutableLiveData()
    fun getUserListLiveData(): LiveData<ArrayList<Users>> = userListLiveData

    fun getUser(uid: String) {

        CloudDbWrapper.getUserById(uid, object : CloudDbWrapper.ICloudDbWrapper {
            override fun onUserObtained(users: Users) {
                userLiveData.value = users
            }
        })
    }

    fun getUserList() {
        CloudDbWrapper.queryUsers {
           userListLiveData.value = it
       }
    }

}