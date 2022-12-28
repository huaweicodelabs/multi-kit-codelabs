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

package com.myapps.hibike.di

import android.content.Context
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.hms.hihealth.data.Scopes
import com.huawei.hms.support.account.AccountAuthManager
import com.huawei.hms.support.account.request.AccountAuthParams
import com.huawei.hms.support.account.request.AccountAuthParamsHelper
import com.huawei.hms.support.account.service.AccountAuthService
import com.huawei.hms.support.api.entity.auth.Scope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AuthServiceModule {

    @Provides
    @Singleton
    fun provideAccountAuthParams(): AccountAuthParams {
        val huaweiIdAuthParamsHelper = AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
        val scopeList = listOf(
            Scope(Scopes.HEALTHKIT_CALORIES_READ),
            Scope(Scopes.HEALTHKIT_CALORIES_WRITE),

            Scope(Scopes.HEALTHKIT_SPEED_READ),
            Scope(Scopes.HEALTHKIT_SPEED_WRITE),

            Scope(Scopes.HEALTHKIT_DISTANCE_READ),
            Scope(Scopes.HEALTHKIT_DISTANCE_WRITE),

            Scope(Scopes.HEALTHKIT_ACTIVITY_READ),
            Scope(Scopes.HEALTHKIT_ACTIVITY_WRITE),

            Scope(Scopes.HEALTHKIT_ACTIVITY_RECORD_READ),
            Scope(Scopes.HEALTHKIT_ACTIVITY_RECORD_WRITE)
        )
        huaweiIdAuthParamsHelper.setScopeList(scopeList)
        return huaweiIdAuthParamsHelper
            .setIdToken()
            .setAccessToken()
            .createParams()
    }

    @Provides
    @Singleton
    fun provideAccountAuthService(
        @ApplicationContext context: Context,
        huaweiIdAuthParams: AccountAuthParams
    ): AccountAuthService {
        return AccountAuthManager.getService(context, huaweiIdAuthParams)
    }

    @Provides
    @Singleton
    fun provideAGConnectAuth(): AGConnectAuth {
        return AGConnectAuth.getInstance()
    }
}