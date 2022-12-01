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
package com.huawei.hms.imagebgcleaner

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.huawei.agconnect.AGConnectInstance
import com.huawei.agconnect.cloud.storage.core.AGCStorageManagement
import com.huawei.agconnect.cloud.storage.core.ListResult
import com.huawei.agconnect.cloud.storage.core.StorageReference
import com.huawei.hmf.tasks.Task
import com.huawei.hms.imagebgcleaner.adapter.DownloadListAdapter
import com.huawei.hms.imagebgcleaner.databinding.ActivityDownloadListBinding

class DownloadListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDownloadListBinding
    private lateinit var fileList: ArrayList<StorageReference>
    private lateinit var mAdapter: DownloadListAdapter
    private var mAGCStorageManagement: AGCStorageManagement? = null
    private val permissions = arrayOf<String>(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.title = "All Files"
        AGConnectInstance.initialize(applicationContext)
        ActivityCompat.requestPermissions(this@DownloadListActivity, permissions, 1)
        initAGCStorageManagement()
        setupRecyclerView()
        getAllList()

    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager =
            LinearLayoutManager(this@DownloadListActivity, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.setHasFixedSize(true)
    }

    private fun initAGCStorageManagement() {
        mAGCStorageManagement = AGCStorageManagement.getInstance("bucket name")
    }

    fun getAllList() {
        val path = "images/"
        val storageReference = mAGCStorageManagement?.getStorageReference(path)
        var listResultTask: Task<ListResult>? = null
        listResultTask = storageReference?.list(100)
        listResultTask?.addOnSuccessListener {
            fileList = ArrayList(it.fileList)
            mAdapter = DownloadListAdapter(
                this@DownloadListActivity,
                fileList = fileList,
                mAGCStorageManagement,
                this@DownloadListActivity
            )
            binding.recyclerView.adapter = mAdapter
            mAdapter.notifyDataSetChanged()

        }?.addOnFailureListener {
            Log.e("MYSTORAGE", "FAIL: ${it.printStackTrace()}")
        }
    }
}