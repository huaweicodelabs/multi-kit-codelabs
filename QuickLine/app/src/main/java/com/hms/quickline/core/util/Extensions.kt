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

package com.hms.quickline.core.util

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavDirections
import androidx.navigation.fragment.findNavController
import java.util.*

fun showToastLong(context: Context, message: String) =
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()

fun showToastShort(context: Context, message: String) =
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

fun showToastLongCenter(context: Context, message: String, length: Int) {
    val toastCenter = Toast.makeText(context, message, length)
    toastCenter.setGravity(Gravity.CENTER, 0, 0)
    toastCenter.show()
}

fun ViewGroup.layoutInflaterFactory(): LayoutInflater = LayoutInflater.from(context)

fun Fragment.navigate(navDirections: NavDirections) = findNavController().navigate(navDirections)

fun getStatus(lastSeen: Date): String {
    val currentDate = Date()
    val difference: Long = currentDate.time - lastSeen.time

    val criteriaOnline = 1000 * 60 //a minute
    val criteriaAway = 1000 * 60 * 10 //ten minutes

    return when {
        difference < criteriaOnline -> "Available"

        difference < criteriaAway -> "Away"

        else -> "Offline"
    }
}
