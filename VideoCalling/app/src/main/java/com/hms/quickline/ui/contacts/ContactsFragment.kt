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

package com.hms.quickline.ui.contacts

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.hms.quickline.R
import com.hms.quickline.core.base.BaseFragment
import com.hms.quickline.core.common.viewBinding
import com.hms.quickline.core.util.Constants.IS_JOIN
import com.hms.quickline.core.util.Constants.IS_MEETING_CONTACT
import com.hms.quickline.core.util.Constants.MEETING_ID
import com.hms.quickline.core.util.Constants.NAME
import com.hms.quickline.data.model.Users
import com.hms.quickline.databinding.FragmentContactsBinding
import com.hms.quickline.domain.repository.CloudDbWrapper
import com.hms.quickline.ui.call.VideoCallActivity
import com.hms.quickline.ui.call.VoiceCallActivity
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.cloud.database.CloudDBZone
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ContactsFragment : BaseFragment(R.layout.fragment_contacts),
    ContactsAdapter.ICallDialogAdapter {

    private val binding by viewBinding(FragmentContactsBinding::bind)
    private val viewModel: ContactsViewModel by viewModels()

    private lateinit var adapter: ContactsAdapter

    private var cloudDBZone: CloudDBZone? = null
    private lateinit var user: Users

    @Inject
    lateinit var agConnectAuth: AGConnectAuth

    private val TAG = "ContactsFragment"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cloudDBZone = CloudDbWrapper.cloudDBZone

        viewModel.getUserList()
        observeData()

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.getUserList()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        agConnectAuth.currentUser?.let {
            viewModel.getUser(it.uid)
        }
    }

    private fun observeData() {
        viewModel.getUserListLiveData().observe(viewLifecycleOwner) {

            binding.rvMeetingIdList.layoutManager = LinearLayoutManager(requireContext())
            adapter = ContactsAdapter(it, this)
            binding.rvMeetingIdList.adapter = adapter
        }
    }

    override fun onItemSelected(isVoiceCall: Boolean, user: Users) {
        val intent = if (isVoiceCall)
            Intent(requireActivity(), VoiceCallActivity::class.java)
        else
            Intent(requireActivity(), VideoCallActivity::class.java)
        intent.apply {
            putExtra(IS_MEETING_CONTACT, true)
            putExtra(MEETING_ID, user.uid)
            putExtra(NAME, user.name)
            putExtra(IS_JOIN, false)
        }

        startActivity(intent)

        user.isCalling = true
        user.callerName = this.user.name

        val upsertTask = cloudDBZone?.executeUpsert(user)
        upsertTask?.addOnSuccessListener { cloudDBZoneResult ->
            Log.i(TAG, "Calls Sdp Upsert success: $cloudDBZoneResult")
        }?.addOnFailureListener {
            Log.i(TAG, "Calls Sdp Upsert failed: ${it.message}")
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        Log.i(
            TAG,
            "onActivityResult requestCode $requestCode, resultCode $resultCode"
        )
    }
}