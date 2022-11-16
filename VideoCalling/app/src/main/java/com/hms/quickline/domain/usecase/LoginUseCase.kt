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

package com.hms.quickline.domain.usecase

import android.content.Context
import android.content.Intent
import com.hms.quickline.R
import com.hms.quickline.core.util.Constants
import com.hms.quickline.data.model.HuaweiAuthResult
import com.hms.quickline.domain.repository.LoginRepository
import com.huawei.agconnect.auth.HwIdAuthProvider
import com.huawei.hms.support.account.AccountAuthManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class LoginUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val loginRepository: LoginRepository) {

    suspend fun signInWithHuaweiId(requestCode: Int, data: Intent?): HuaweiAuthResult =
        suspendCoroutine { continuation ->
            if (requestCode == Constants.HUAWEI_ID_SIGN_IN) {
                val authAccountTask = AccountAuthManager.parseAuthResultFromIntent(data)

                if (authAccountTask.isSuccessful) {
                    val authAccount = authAccountTask.result
                    val credential = HwIdAuthProvider.credentialWithToken(authAccount.accessToken)

                    loginRepository.signInWithHuaweiId(credential)
                        .addOnSuccessListener { signInResult ->
                            val user = signInResult.user
                            user?.let {
                                continuation.resume(HuaweiAuthResult.UserSuccessful(it))
                            }
                        }.addOnFailureListener {
                            it?.let {
                                continuation.resume(HuaweiAuthResult.UserFailure(it.message))
                            }
                        }
                } else {
                    continuation.resume(HuaweiAuthResult.UserFailure(authAccountTask?.exception?.message))
                }
            } else {
                continuation.resume(HuaweiAuthResult.UserFailure(context.getString(R.string.request_code_invalid)))
            }
        }
}