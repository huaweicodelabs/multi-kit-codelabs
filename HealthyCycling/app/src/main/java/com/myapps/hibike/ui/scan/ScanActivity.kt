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

package com.myapps.hibike.ui.scan

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Bundle
import android.text.TextUtils
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hms.hmsscankit.RemoteView
import com.huawei.hms.ml.scan.HmsScan
import com.myapps.hibike.R
import com.myapps.hibike.databinding.ActivityScanBinding
import com.myapps.hibike.utils.extension.hide
import com.myapps.hibike.utils.extension.show


class ScanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanBinding
    private lateinit var remoteView: RemoteView
    private lateinit var rect: Rect

    companion object{
        const val SCAN_RESULT = "scanResult"
        const val SCAN_FRAME_SIZE = 300
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setScanningArea()

        remoteView = RemoteView.Builder().setContext(this).setBoundingBox(rect).setFormat(HmsScan.ALL_SCAN_TYPE).build()
        remoteView.apply {
            onCreate(savedInstanceState)
            setOnResultCallback { result ->
                if (result != null && result.isNotEmpty() && result[0] != null && !TextUtils.isEmpty(result[0].getOriginalValue())) {
                    val intent = Intent()
                    intent.putExtra(SCAN_RESULT, result[0])
                    setResult(RESULT_OK, intent)
                    finish()
                } else {
                    Toast.makeText(context, getString(R.string.scan_error), Toast.LENGTH_SHORT).show()
                }
            }
        }

        addRemoteViewToLayout()
        checkFlashlight()
        initListeners()
    }

    private fun checkFlashlight(){
        val hasFlash = this.packageManager
            .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)
        if (!hasFlash) {
            binding.ivFlashLight.hide()
        } else {
            binding.ivFlashLight.show()
        }
    }

    private fun initListeners(){
        with(binding){
            ivBack.setOnClickListener {
                finish()
            }
            ivFlashLight.setOnClickListener {
                if (remoteView.lightStatus) {
                    remoteView.switchLight()
                    ivFlashLight.setImageResource(com.huawei.hms.scankit.R.drawable.scankit_ic_light_off)
                } else {
                    remoteView.switchLight()
                    ivFlashLight.setImageResource(com.huawei.hms.scankit.R.drawable.scankit_ic_light_on)
                }
            }
        }
    }

    private fun setScanningArea(){
        val displayMetrics = resources.displayMetrics
        val density = displayMetrics.density
        val mScreenWidth = displayMetrics.widthPixels
        val mScreenHeight = displayMetrics.heightPixels
        val scanFrameSize = (SCAN_FRAME_SIZE * density)

        rect = Rect()
        rect.apply {
            left = (mScreenWidth / 2 - scanFrameSize / 2).toInt()
            right = (mScreenWidth / 2 + scanFrameSize / 2).toInt()
            top = (mScreenHeight / 2 - scanFrameSize / 2).toInt()
            bottom = (mScreenHeight / 2 + scanFrameSize / 2).toInt()
        }
    }

    private fun addRemoteViewToLayout(){
        val params = FrameLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        binding.frameLayout.addView(remoteView, params)
    }

    override fun onStart() {
        super.onStart()
        remoteView.onStart()
    }

    override fun onResume() {
        super.onResume()
        remoteView.onResume()
    }

    override fun onPause() {
        super.onPause()
        remoteView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        remoteView.onDestroy()
    }

    override fun onStop() {
        super.onStop()
        remoteView.onStop()
    }
}