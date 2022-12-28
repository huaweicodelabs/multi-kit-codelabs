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

package com.myapps.hibike.ui.login

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.huawei.hms.support.account.service.AccountAuthService
import com.myapps.hibike.R
import com.myapps.hibike.databinding.FragmentLoginBinding
import com.myapps.hibike.utils.extension.goSettings
import com.myapps.hibike.utils.extension.hide
import com.myapps.hibike.utils.extension.show
import com.myapps.hibike.utils.extension.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val loginViewModel: LoginViewModel by viewModels()

    @Inject
    lateinit var authService: AccountAuthService

    private var signInWithHuaweiID =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                loginViewModel.signIn(result.data)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater)

        initListeners()
        setupObserver()
        onBackPressed()

        return binding.root
    }

    private fun initListeners() {
        binding.btnSignIn.setOnClickListener {
            if (checkPermission()) signInWithHuaweiID.launch(authService.signInIntent)
            else showPermissionDialog()
        }
    }

    private fun showPermissionDialog(){
        val alertDialog = AlertDialog.Builder(requireActivity())
            .setMessage(getString(R.string.permissions_dialog_text))
            .setNegativeButton(getString(R.string.alert_neg)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(getString(R.string.go_settings)) { _, _ ->
                goSettings()
            }.create()
        alertDialog.show()
    }

    private fun setupObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.authState.collect { loginUiState ->
                    if (loginUiState.isUserSignedIn && loginUiState.isUserCreated) {
                        binding.loginProgress.hide()
                        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    }
                    if (loginUiState.isLoading) {
                        binding.loginProgress.show()
                    }
                    if (loginUiState.error.isNotEmpty()) {
                        binding.loginProgress.hide()
                        showToast(loginUiState.error)
                    }
                }
            }
        }
    }

    private fun onBackPressed() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                activity?.finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        } else {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }
}