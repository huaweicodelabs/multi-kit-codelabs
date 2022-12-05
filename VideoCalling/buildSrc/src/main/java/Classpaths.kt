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

object Classpaths {
    const val buildGradle = "com.android.tools.build:gradle:${Versions.gradlePluginVersion}"
    const val kotlinGradle = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlinVersion}"
    const val hiltGradle = "com.google.dagger:hilt-android-gradle-plugin:${Versions.hiltVersion}"
    const val safeArgsGradle =
        "androidx.navigation:navigation-safe-args-gradle-plugin:${Versions.navigationVersion}"
    const val agconnectGradle = "com.huawei.agconnect:agcp:${Versions.agcVersion}"
}