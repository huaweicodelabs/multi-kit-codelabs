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

package com.myapps.hibike.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.huawei.agconnect.auth.AGConnectAuth
import com.myapps.hibike.R
import com.myapps.hibike.databinding.FragmentProfileBinding
import com.myapps.hibike.utils.extension.hide
import com.myapps.hibike.utils.extension.show
import com.myapps.hibike.utils.extension.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding

    @Inject
    lateinit var agConnectAuth: AGConnectAuth

    private val profileViewModel: ProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater)

        profileViewModel.checkUserWeight()

        initView()
        initListeners()
        onBackPressed()
        setupObserver()

        return binding.root
    }

    private fun initView(){
        val currentUser = agConnectAuth.currentUser
        with(binding){
            Glide.with(requireContext())
                .load(currentUser.photoUrl)
                .circleCrop()
                .into(ivProfile)
            tvDisplayName.text = currentUser.displayName
            numberPicker.apply {
                minValue = 0
                maxValue = 200
                textColor = requireContext().getColor(R.color.blue)
            }
        }
    }

    private fun initListeners(){
        with(binding) {
            btnSignOut.setOnClickListener {
                agConnectAuth.signOut()
                findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
            }
            btnLastRides.setOnClickListener {
                findNavController().navigate(R.id.lastRidesFragment)
            }
            ivEdit.setOnClickListener {
                numberPicker.show()
                tvWeight.hide()
                ivOk.show()
                ivEdit.hide()
            }
            ivOk.setOnClickListener {
                val weight = numberPicker.value
                profileViewModel.updateUserWeight(weight.toDouble())
            }
        }
    }

    private fun setupObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                profileViewModel.profileUiState.collect { profileUiState ->
                    if (profileUiState.userWeight != null) {
                        binding.tvWeight.text = profileUiState.userWeight.toString()
                        binding.numberPicker.value = profileUiState.userWeight.toInt()
                    }
                    if(profileUiState.userWeightStatus){
                        profileViewModel.checkUserWeight()
                        binding.numberPicker.hide()
                        binding.tvWeight.show()
                        binding.ivOk.hide()
                        binding.ivEdit.show()
                        profileUiState.userWeightStatus = false
                    }
                    if (profileUiState.isLoading) {
                        binding.profileProgress.show()
                    }
                    if (!profileUiState.isLoading) {
                        binding.profileProgress.hide()
                    }
                    if (profileUiState.error.isNotEmpty()) {
                        showToast(profileUiState.error)
                    }
                }
            }
        }
    }

    private fun onBackPressed() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.homeFragment)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

}