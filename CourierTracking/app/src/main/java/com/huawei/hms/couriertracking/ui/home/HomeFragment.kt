/*
 * Copyright 2022. Huawei Technologies Co., Ltd. All rights reserved.
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
     http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.huawei.hms.couriertracking.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.huawei.hms.couriertracking.core.adapters.OrderRecyclerViewAdapter
import com.huawei.hms.couriertracking.databinding.FragmentHomeBinding
import com.huawei.hms.couriertracking.ui.home.HomeViewState.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModels()
    private val orderAdapter = OrderRecyclerViewAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        homeViewModel.getOrders()
        collectUIState()
    }

    private fun collectUIState() {
        lifecycleScope.launch {
            homeViewModel.uiState.collectLatest { uiState ->
                when(uiState){
                    is Success -> {
                        orderAdapter.differ.submitList(uiState.orders)
                        binding.progressBarHome.visibility = View.GONE
                    }
                    Loading -> { binding.progressBarHome.visibility = View.VISIBLE }
                    is Error -> {
                        Toast.makeText(requireContext(), uiState.errorMessage, Toast.LENGTH_LONG).show()
                        binding.progressBarHome.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewCompletedOrders.apply {
            adapter = orderAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,false)
        }
        initItemClick()
    }

    private fun initItemClick() {
        orderAdapter.setOnItemClickListener {
            findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToCourierTrackingFragment(it.storeLocation)
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}