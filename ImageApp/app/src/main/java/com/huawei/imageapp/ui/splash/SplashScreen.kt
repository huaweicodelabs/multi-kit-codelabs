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

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.huawei.imageapp.R
import com.huawei.imageapp.core.designsystem.component.VerticalSpacer

@Composable
fun SplashScreen(
    goToSearchImageScreen: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val splashViewState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(splashViewState) {
        when (splashViewState) {
            is SplashViewState.Success -> {
                goToSearchImageScreen()
            }
            is SplashViewState.Loading -> Unit
            is SplashViewState.Error -> {
                Toast.makeText(
                    context,
                    (splashViewState as SplashViewState.Error).errorMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_splash),
            contentDescription = null,
            modifier = modifier
                .height(250.dp)
                .width(250.dp)
        )
        VerticalSpacer(height = 32.dp)
        if (splashViewState is SplashViewState.Loading) {
            CircularProgressIndicator()
        }
    }
}