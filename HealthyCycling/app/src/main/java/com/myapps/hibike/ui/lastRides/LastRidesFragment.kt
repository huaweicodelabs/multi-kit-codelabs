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

package com.myapps.hibike.ui.lastRides

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.myapps.hibike.databinding.FragmentLastRidesBinding
import com.myapps.hibike.utils.extension.hide
import com.myapps.hibike.utils.extension.show
import com.myapps.hibike.utils.extension.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LastRidesFragment : Fragment() {

    private val lastRidesViewModel: LastRidesViewModel by viewModels()
    private lateinit var binding: FragmentLastRidesBinding
    @Inject
    lateinit var lastRidesInfoRecyclerAdapter: LastRidesInfoRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLastRidesBinding.inflate(inflater)

        lastRidesViewModel.getMyLastRides()
        setupObservable()
        initView()

        return binding.root
    }

    private fun initView(){
        binding.rvLastRides.adapter = lastRidesInfoRecyclerAdapter
    }

    private fun setupObservable() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                lastRidesViewModel.lastRidesUiState.collect { lastRidesUiState ->
                    if (lastRidesUiState.isLoading) {
                        binding.lastRidesProgress.show()
                    }
                    if (lastRidesUiState.error.isNotEmpty()) {
                        binding.lastRidesProgress.hide()
                        showToast(lastRidesUiState.error)
                    }
                    if (lastRidesUiState.rideList?.isNotEmpty() == true) {
                        binding.lastRidesProgress.hide()
                        binding.apply {
                            rvLastRides.show()
                            tvLetsMove.hide()
                        }
                        lastRidesInfoRecyclerAdapter.submitList(lastRidesUiState.rideList)
                    }
                    if (lastRidesUiState.rideList?.isEmpty() == true) {
                        binding.lastRidesProgress.hide()
                        binding.apply {
                            rvLastRides.hide()
                            tvLetsMove.show()
                        }
                    }
                    if (lastRidesUiState.unpaidRide.isNotEmpty()){
                        binding.lastRidesProgress.hide()
                        lastRidesInfoRecyclerAdapter.setUnpaidRideId(lastRidesUiState.unpaidRide)
                    }
                }
            }
        }
    }
}