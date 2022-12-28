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
package com.myapps.hibike.ui.progress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.myapps.hibike.R
import com.myapps.hibike.databinding.FragmentProgressBinding
import com.myapps.hibike.ui.MainActivity
import com.myapps.hibike.utils.CalculationHelper
import com.myapps.hibike.utils.extension.hide
import com.myapps.hibike.utils.extension.show
import com.myapps.hibike.utils.extension.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class ProgressFragment : Fragment() {

    private lateinit var binding: FragmentProgressBinding
    private val progressViewModel: ProgressViewModel by viewModels()
    @Inject
    lateinit var calendar: Calendar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProgressBinding.inflate(inflater)
        progressViewModel.checkUserWeight()
        progressViewModel.getWeeklyRides()
        setupObserver()

        return binding.root
    }

    private fun initView(){
        val progressUiStateValue = progressViewModel.progressUiState.value
        with(binding){
            tvSubTitleProgress.text = "${CalculationHelper.convertTimeMillisToDate(calendar.timeInMillis)} - ${CalculationHelper.convertTimeMillisToDate(calendar.timeInMillis.plus(518400000))}"
            progressUiStateValue.totalCalorie.let { totalCalorie ->
                if (totalCalorie != null) {
                    circularProgressBar.progress = totalCalorie
                    tvCalorieData.text = "${DecimalFormat(getString(R.string.decimal_pattern)).format(progressUiStateValue.totalCalorie)}/${circularProgressBar.progressMax}"
                }
                else {
                    circularProgressBar.progress = 0F
                    tvCalorieData.text = "${DecimalFormat(getString(R.string.decimal_pattern)).format(0F)}/${circularProgressBar.progressMax}"
                }
            }
            customTime.initCustomView(R.drawable.ic_timer, getString(R.string.total_time), progressUiStateValue.time, getString(R.string.hour))
            val strSpeed = DecimalFormat(getString(R.string.decimal_pattern)).format(progressUiStateValue.avgSpeed)
            customSpeed.initCustomView(R.drawable.ic_speed, getString(R.string.avg_speed), strSpeed, getString(R.string.km_h))
            val strDist = DecimalFormat(getString(R.string.decimal_pattern)).format(progressUiStateValue.totalDistance)
            customDistance.initCustomView(R.drawable.ic_distance, getString(R.string.total_dist), strDist, getString(R.string.km))
            customRide.initCustomView(R.drawable.ic_cycling, getString(R.string.total_ride), progressUiStateValue.totalRide.toString(), getString(R.string.ride))
        }
    }

    private fun setupObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                progressViewModel.progressUiState.collect { progressUiState ->
                    if (progressUiState.rideList != null && progressUiState.totalCalorie != null) {
                        initView()
                        if(progressUiState.totalCalorie >= binding.circularProgressBar.progressMax && progressUiState.userInformedStatus == false){
                            progressViewModel.sendNotification(progressUiState.accessToken, MainActivity.pushTokenText, getString(R.string.congratulations), getString(R.string.congratulations_text))
                            progressViewModel.updateUserInformed(true)
                        }
                        if(progressUiState.userInformedStatus == true && progressUiState.totalCalorie < binding.circularProgressBar.progressMax) {
                            progressViewModel.updateUserInformed(false)
                        }
                    }
                    if (progressUiState.isLoading) {
                        binding.progressProgress.show()
                    }
                    if (!progressUiState.isLoading) {
                        binding.progressProgress.hide()
                    }
                    if (progressUiState.error.isNotEmpty()) {
                        showToast(progressUiState.error)
                    }
                    if (progressUiState.userWeight == 0.0) {
                        showToast(getString(R.string.sub_title_progress))
                    }
                    if (progressUiState.throwable.isNotEmpty()){
                        showToast(progressUiState.throwable)
                    }
                }
            }
        }
    }
}