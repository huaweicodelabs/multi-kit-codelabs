/*
 * Copyright 2022. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.myapps.hibike.data.repository

import android.content.Intent
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.AGConnectUser
import com.huawei.agconnect.auth.HwIdAuthProvider
import com.huawei.hms.support.account.AccountAuthManager
import com.myapps.hibike.utils.IServiceListener
import javax.inject.Inject

class AuthServiceRepository @Inject constructor(private val agConnectAuth: AGConnectAuth) {

    fun userSignIn(intent: Intent?, serviceListener: IServiceListener<AGConnectUser>){
        AccountAuthManager.parseAuthResultFromIntent(intent)?.let { task ->
            if (task.isSuccessful){
                task.result.let { authAccount->
                    val credential = HwIdAuthProvider.credentialWithToken(authAccount.accessToken)
                    agConnectAuth.signIn(credential).addOnSuccessListener {
                        serviceListener.onSuccess(it.user)
                    }.addOnFailureListener {
                        serviceListener.onError(it)
                    }
                }
            }
            else
                serviceListener.onError(Exception("Task is unsuccessful!"))
        }
    }
}