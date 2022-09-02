/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
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
package com.huawei.hms.knowmyboard.dtse.activity.ml

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Build
import android.app.Activity
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.WindowManager
import java.lang.Exception

open class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    /**
     * Set status bar immersion.
     */
    protected fun setStatusBar() {
        // SDK 21/Android 5.0.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val decorView = this.window.decorView
            val setting = (View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
            decorView.systemUiVisibility = setting
            // Set the status bar to transparent.
            this.window.statusBarColor = Color.TRANSPARENT
        }
    }

    /**
     * Set the status bar font color to dark
     */
    fun setStatusBarFontColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val decorView = this.window.decorView
            var visibility = decorView.systemUiVisibility
            visibility = visibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            decorView.systemUiVisibility = visibility
        }
    }

    companion object {
        /**
         * Set status bar's color.
         * @param activity Activity of page.
         * @param colorId Color ID.
         */
        protected fun setStatusBarColor(activity: Activity, colorId: Int) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val window = activity.window
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    window.statusBarColor = activity.resources.getColor(colorId)
                }
            } catch (e: Exception) {
                Log.e("BaseActivity", e.message!!)
            }
        }
    }
}