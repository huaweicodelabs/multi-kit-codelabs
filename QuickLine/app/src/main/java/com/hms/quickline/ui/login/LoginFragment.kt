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

package com.hms.quickline.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.hms.quickline.R
import com.hms.quickline.core.base.BaseFragment
import com.hms.quickline.core.common.viewBinding
import com.hms.quickline.core.util.*
import com.hms.quickline.core.util.Constants.HUAWEI_ID_SIGN_IN
import com.hms.quickline.data.Resource
import com.hms.quickline.data.model.Users
import com.hms.quickline.databinding.FragmentLoginBinding
import com.hms.quickline.domain.repository.CloudDbWrapper
import com.hms.quickline.service.CallService
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.cloud.database.CloudDBZone
import com.huawei.hms.support.account.AccountAuthManager
import com.huawei.hms.support.account.request.AccountAuthParams
import com.huawei.hms.support.account.request.AccountAuthParamsHelper
import com.huawei.hms.support.account.service.AccountAuthService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : BaseFragment(R.layout.fragment_login) {

    private val binding by viewBinding(FragmentLoginBinding::bind)
    private val TAG = "LoginFragmentTag"

    private var cloudDBZone: CloudDBZone? = null

    private val loginViewModel: LoginViewModel by viewModels()
    private lateinit var service: AccountAuthService
    private lateinit var authParams: AccountAuthParams

    @Inject
    lateinit var agConnectAuth: AGConnectAuth

    private var hasUserDb: Boolean? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mFragmentNavigation.setBottomBarVisibility(false)

        cloudDBZone = CloudDbWrapper.cloudDBZone

        binding.buttonAuth.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate()
                .alpha(1f)
                .setDuration(1000)
                .setListener(null)
        }

        observeData()

        authParams = AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
            .setAccessToken()
            .createParams()

        service = AccountAuthManager.getService(requireActivity(), authParams)

        binding.buttonAuth.setOnClickListener {
            signInHuaweiId()
        }
    }

    private fun observeData(){
        loginViewModel.getCheckUserLiveData().observe(viewLifecycleOwner) {
            hasUserDb = it
            saveUserToDb()
        }

        loginViewModel.getSignInHuaweiIdLiveData().observe(viewLifecycleOwner) {
            handleSignInReturn(it)
        }
    }

    private fun signInHuaweiId(){
        val signInIntent = service.signInIntent
        startActivityForResult(signInIntent, HUAWEI_ID_SIGN_IN)
    }

    private fun handleSignInReturn(data: Resource<*>) {
        when (data) {
            is Resource.Loading<*> -> {
                binding.progressBar.showProgress()
            }
            is Resource.Success<*> -> {
                loginViewModel.checkUserLogin(agConnectAuth.currentUser.uid)
                binding.progressBar.hideProgress()
            }
            is Resource.Failed<*> -> {
                binding.progressBar.hideProgress()
                showToastLong(binding.root.context, data.message)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == HUAWEI_ID_SIGN_IN) {
            loginViewModel.signInWithHuaweiId(requestCode, data)
        }
    }

    private fun saveUserToDb() {
        agConnectAuth.currentUser.apply {
            val currentUser = Users()
            currentUser.uid = uid
            currentUser.name = displayName
            currentUser.email = email
            currentUser.photo = photoUrl
            currentUser.phone = phone

            val upsertTask = cloudDBZone?.executeUpsert(currentUser)
            upsertTask?.addOnSuccessListener { cloudDBZoneResult ->
                Log.i(TAG, "User Upsert success: $cloudDBZoneResult")

                hasUserDb?.let {
                    if (it) navigateHome()
                }

            }?.addOnFailureListener {
                Log.i(TAG, "User Upsert failed: ${it.message}")
            }

        }
    }

    private fun navigateHome() {
        val intent = Intent(requireContext(), CallService::class.java)
        intent.putExtra(Constants.UID, agConnectAuth.currentUser.uid)
        activity?.startService(intent)

        navigate(LoginFragmentDirections.actionLoginFragmentToHomeFragment())
    }
}