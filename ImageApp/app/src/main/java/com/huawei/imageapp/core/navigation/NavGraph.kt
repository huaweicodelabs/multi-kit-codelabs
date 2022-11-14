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


package com.huawei.imageapp.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.huawei.imageapp.ui.image_detail.ImageDetailScreen
import com.huawei.imageapp.ui.search_image.SearchImageScreen
import com.huawei.imageapp.ui.splash.SplashScreen

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(route = Screen.Splash.route) {
            SplashScreen(
                goToSearchImageScreen = {
                    navController.navigate(Screen.SearchImage.route){
                        popUpTo(Screen.Splash.route){
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable(route = Screen.SearchImage.route) {
            SearchImageScreen(
                navigateToImageDetail = {
                    navController.navigate(Screen.ImageDetail.route)
                },
                setImageToCurrentBackStackEntry = { externalImage ->
                    navController.currentBackStackEntry?.savedStateHandle?.set(
                        "externalImage",externalImage
                    )
                }
            )
        }
        composable(route = Screen.ImageDetail.route) {
            ImageDetailScreen(
                externalImage = navController.previousBackStackEntry?.savedStateHandle?.get("externalImage"),
                goPreviousScreen = { navController.popBackStack() }
            )
        }
    }
}