/*
 * Copyright 2022. Explore in HMS. All rights reserved.
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
 *
 */
package com.huawei.separateaudiocodelab.ui.home

import android.Manifest
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.huawei.separateaudiocodelab.R
import com.huawei.separateaudiocodelab.databinding.FragmentHomeBinding
import com.huawei.separateaudiocodelab.util.PermissionUtil
import com.huawei.separateaudiocodelab.util.PermissionUtil.launchMultiplePermission
import com.huawei.separateaudiocodelab.util.PermissionUtil.registerPermission

class HomeFragment : Fragment() {

    private lateinit var binding : FragmentHomeBinding
    private val storagePermissions = arrayOf<String>(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private val storagePermission = registerPermission {
        onStoragePermissionResult(it)
    }

    private fun initListener() {
        binding.cardViewVideoEdit.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_extractionFragment)
        }
        binding.cardViewSoundEdit.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_separationFragment)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding = FragmentHomeBinding.inflate(inflater,container,false)
        storagePermission.launchMultiplePermission(storagePermissions)
        initListener()
        return binding.root
    }

    private fun onStoragePermissionResult(permissionState: PermissionUtil.PermissionState) {
        if (permissionState != PermissionUtil.PermissionState.Granted) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.permission_message_for_storage),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}