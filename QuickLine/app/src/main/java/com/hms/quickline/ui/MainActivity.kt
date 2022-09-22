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

package com.hms.quickline.ui

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import com.hms.quickline.R
import com.hms.quickline.core.base.BaseActivity
import com.hms.quickline.core.base.BaseFragment
import com.hms.quickline.core.common.viewBinding
import com.hms.quickline.core.util.Constants.REQUEST_CODE
import com.hms.quickline.core.util.setupWithNavController
import com.hms.quickline.core.util.showToastLong
import com.hms.quickline.core.util.showToastShort
import com.hms.quickline.databinding.ActivityMainBinding
import com.hms.quickline.domain.repository.CloudDbWrapper
import com.huawei.hms.analytics.HiAnalytics
import com.huawei.hms.analytics.HiAnalyticsTools
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.api.safetydetect.SafetyDetect
import com.huawei.hms.support.api.safetydetect.SafetyDetectStatusCodes
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom

@AndroidEntryPoint
class MainActivity : BaseActivity(), BaseFragment.FragmentNavigation {

    private val binding by viewBinding(ActivityMainBinding::inflate)
    private var currentNavController: LiveData<NavController>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        invokeSysIntegrity()

        HiAnalyticsTools.enableLog()
        HiAnalytics.getInstance(this)

        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        setupBottomNavigationBar()
    }

    private fun setupBottomNavigationBar() {
        val navGraphIds =
            listOf(
                R.navigation.main_nav_graph,
                R.navigation.home,
                R.navigation.contacts,
                R.navigation.profile
            )

        val controller = binding.bottomNav.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.nav_host_fragment,
            intent = intent
        )

        currentNavController = controller

        checkPermissions(this)
    }

    private fun checkPermissions(activity: Activity) {

        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d("checkPermissions", "No Permissions")

            ActivityCompat.requestPermissions(
                activity,
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                REQUEST_CODE
            )
        }
    }

    override fun giveAction(action: Int) {
        currentNavController?.value?.navigate(action)
    }

    override fun navigateUP() {
        currentNavController?.value?.navigateUp()
    }

    override fun navigateTop() {
        finish()
        startActivity(intent)
        currentNavController?.value?.navigate(R.id.home)
    }

    override fun setBottomBarVisibility(isVisible: Boolean) {
        binding.bottomNav.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    override fun onDestroy() {
        CloudDbWrapper.closeCloudDBZone()
        super.onDestroy()
    }

    private fun invokeSysIntegrity() {
        val nonce = ByteArray(24)
        try {
            val random: SecureRandom = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                SecureRandom.getInstanceStrong()
            } else {
                SecureRandom.getInstance("SHA1PRNG")
            }
            random.nextBytes(nonce)
        } catch (e: NoSuchAlgorithmException) {
            Log.e("NoSuchAlgorithmException", e.message!!)
        }

        SafetyDetect.getClient(this)
            .sysIntegrity(nonce, "105993909")
            .addOnSuccessListener { response ->
                val jwsStr = response.result
                val jwsSplit = jwsStr.split(".").toTypedArray()
                val jwsPayloadStr = jwsSplit[1]
                val payloadDetail = String(
                    Base64.decode(
                        jwsPayloadStr.toByteArray(StandardCharsets.UTF_8),
                        Base64.URL_SAFE
                    ), StandardCharsets.UTF_8
                )
                try {
                    val jsonObject = JSONObject(payloadDetail)
                    val basicIntegrity = jsonObject.getBoolean("basicIntegrity")
                    val isBasicIntegrity = basicIntegrity.toString()
                    val basicIntegrityResult = "Basic Integrity: $isBasicIntegrity"

                    Log.i("Basic Integrity", basicIntegrityResult)
                    showToastLong(this, "The device is secure")

                    if (!basicIntegrity) {
                        Log.i("Advice", jsonObject.getString("advice"))
                    }
                } catch (e: JSONException) {
                    val errorMsg = e.message
                    Log.e("JsonException", errorMsg ?: "unknown error")
                }
            }
            .addOnFailureListener { e ->
                val errorMsg: String? = if (e is ApiException) {
                    SafetyDetectStatusCodes.getStatusCodeString(e.statusCode) + ": " + e.message
                } else {
                    e.message
                }
                Log.e("TAG", errorMsg.orEmpty())
                errorMsg?.let { showToastShort(this, it) }
            }
    }
}