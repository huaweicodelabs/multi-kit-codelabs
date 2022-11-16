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

package com.hms.quickline.ui.profile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.hms.quickline.R
import com.hms.quickline.core.base.BaseFragment
import com.hms.quickline.core.common.viewBinding
import com.hms.quickline.core.util.gone
import com.hms.quickline.core.util.showToastShort
import com.hms.quickline.core.util.visible
import com.hms.quickline.data.model.Users
import com.hms.quickline.databinding.FragmentProfileBinding
import com.hms.quickline.domain.repository.CloudDbWrapper
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.cloud.database.CloudDBZone
import com.huawei.hms.mlsdk.livenessdetection.MLLivenessCapture
import com.huawei.hms.mlsdk.livenessdetection.MLLivenessCaptureConfig
import com.huawei.hms.mlsdk.livenessdetection.MLLivenessCaptureResult
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : BaseFragment(R.layout.fragment_profile) {

    companion object {
        private val PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA
        )
    }

    @Inject
    lateinit var agConnectAuth: AGConnectAuth

    private val TAG = "ProfileFragment"

    private val binding by viewBinding(FragmentProfileBinding::bind)
    private val viewModel: ProfileViewModel by viewModels()

    private var cloudDBZone: CloudDBZone? = null

    private var name = ""
    private var userId = ""

    private lateinit var user: Users

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        agConnectAuth.currentUser?.let {
            name = it.displayName
            userId = it.uid
        }

        cloudDBZone = CloudDbWrapper.cloudDBZone

        CloudDbWrapper.updateLastSeen(userId, Date())

        initAvailable()
        observeData()
        viewModel.checkAvailable(userId)

        initClickListener()

        agConnectAuth.currentUser?.let {
            viewModel.getUser(it.uid)
        }

        viewModel.getUserInfo()
        observeLiveData()
    }

    private fun initClickListener() {
        binding.btnVerify.setOnClickListener {
            detect()
        }

        binding.signOut.setOnClickListener {
            viewModel.signOut()
            mFragmentNavigation.navigateTop()
        }
    }

    private fun initAvailable() {
        binding.btnBusy.setOnCheckedChangeListener { _, isChecked ->
            cloudDBZone?.let {
                viewModel.updateAvailable(userId, !isChecked, it)
            }
        }
    }

    private fun observeData() {
        viewModel.getAvailableLiveData().observe(viewLifecycleOwner) {
            binding.btnBusy.isChecked = !it
        }

        viewModel.getUserLiveData().observe(viewLifecycleOwner) {
            if (!it.isVerified)
                binding.btnVerify.visible()

            user = it
        }
    }

    private fun detect() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            livenessDetection()
            return
        }
        ActivityCompat.requestPermissions(requireActivity(), PERMISSIONS, 0)
    }

    private val callback: MLLivenessCapture.Callback = object : MLLivenessCapture.Callback {
        /**
         * Liveness detection success callback.
         * @param result result
         */
        override fun onSuccess(result: MLLivenessCaptureResult) {
            Log.i(TAG, "success")
            if (result.isLive) {
                user.isVerified = true

                val upsertTask = cloudDBZone?.executeUpsert(user)
                upsertTask?.addOnSuccessListener { cloudDBZoneResult ->
                    Log.i("UpsertUser", "User Upsert success: $cloudDBZoneResult")
                    showToastShort(
                        requireContext(),
                        resources.getText(R.string.user_verified_message).toString()
                    )

                    viewModel.getUserList()
                    binding.btnVerify.gone()
                }?.addOnFailureListener {
                    Log.i("UpsertUser", "User Upsert failed: ${it.message}")
                }
            } else {
                showToastShort(
                    requireContext(),
                    resources.getText(R.string.user_verified_error_message).toString()
                )
            }
        }

        override fun onFailure(errorCode: Int) {
            Log.i(TAG, "error")
        }
    }

    private fun livenessDetection() {
        //Obtain liveness detection config and set detect mask and sunglasses
        val captureConfig: MLLivenessCaptureConfig = MLLivenessCaptureConfig.Builder().setOptions(
            MLLivenessCaptureConfig.DETECT_MASK
        ).build()

        // Obtains the liveness detection plug-in instance.
        val capture: MLLivenessCapture = MLLivenessCapture.getInstance()
        capture.setConfig(captureConfig)
        capture.startDetect(requireActivity(), this.callback)
    }

    // Permission application callback.
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.i(TAG, "onRequestPermissionsResult ")

        livenessDetection()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        Log.i(
            TAG,
            "onActivityResult requestCode $requestCode, resultCode $resultCode"
        )
    }

    private fun observeLiveData() {
        viewModel.userData.observe(viewLifecycleOwner) {
            it?.let {
                binding.profileName.text = it.displayName

                context?.let { ctx ->
                    Glide.with(ctx).load(it.photoUrl).fitCenter().circleCrop()
                        .into(binding.profileImage)
                }
            }
        }
    }
}
