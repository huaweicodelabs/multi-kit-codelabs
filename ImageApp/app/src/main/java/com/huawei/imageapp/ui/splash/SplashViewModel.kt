/*
 * Copyright 2022. Huawei Technologies Co., Ltd. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * Copyright 2022. Huawei Technologies Co., Ltd. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.huawei.imageapp.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huawei.imageapp.domain.usecase.InitializeCloudDBUseCase
import com.huawei.imageapp.core.common.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val initializeCloudDBUseCase: InitializeCloudDBUseCase
): ViewModel() {
    private val _uiState = MutableStateFlow<SplashViewState>(SplashViewState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        initializeCloudDB()
    }

    private fun initializeCloudDB() = viewModelScope.launch {
        initializeCloudDBUseCase().collect{ result ->
            when(result){
                is Result.Success -> _uiState.value = SplashViewState.Success
                is Result.Error -> _uiState.value = SplashViewState.Error(result.message)
                is Result.Loading -> _uiState.value = SplashViewState.Loading
            }
        }
    }
}