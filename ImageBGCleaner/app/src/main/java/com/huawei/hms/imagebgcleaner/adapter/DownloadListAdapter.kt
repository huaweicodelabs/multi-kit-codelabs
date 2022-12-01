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
package com.huawei.hms.imagebgcleaner.adapter

import android.app.ProgressDialog
import android.content.Context
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.huawei.agconnect.cloud.storage.core.AGCStorageManagement
import com.huawei.agconnect.cloud.storage.core.StorageReference
import com.huawei.hms.imagebgcleaner.DownloadListActivity
import com.huawei.hms.imagebgcleaner.databinding.DownloadListRowBinding
import java.io.File
import java.sql.DriverManager

class DownloadListAdapter(
    private val context: Context,
    private val fileList: List<StorageReference>,
    private val mAGCStorageManagement: AGCStorageManagement? = null,
    private val activity: DownloadListActivity
) : RecyclerView.Adapter<DownloadListAdapter.DownloadListViewHolder>() {

    inner class DownloadListViewHolder(val binding: DownloadListRowBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DownloadListViewHolder {
        return DownloadListViewHolder(
            DownloadListRowBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: DownloadListViewHolder, position: Int) {
        val data = fileList[position]
        holder.binding.apply {
            tvFileName.text = data.name
            val path: String = data.parent?.path + data.name
            imgDownload.setOnClickListener {
                Toast.makeText(context, "Downloaded", Toast.LENGTH_SHORT).show()
                downloadFile(data.name, path)
            }
            imgDelete.setOnClickListener {
                deleteFile(path)
            }
        }

    }

    private fun downloadFile(fileName: String, path: String) {
        val progressDialog = ProgressDialog(context)
        progressDialog.setMessage("Downloading File....")
        progressDialog.setCancelable(false)
        progressDialog.show()
        val fileName = fileName
        val path = path
        val agcSdkDirPath = agcSdkDirPath2
        val file = File(agcSdkDirPath, fileName)
        val storageReference = mAGCStorageManagement!!.getStorageReference(path)
        val downloadTask = storageReference.getFile(file)
        downloadTask.addOnSuccessListener {
            Toast.makeText(context, "Download Succeed", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
        }.addOnFailureListener { e: Exception ->
            Toast.makeText(context, "Download Error", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
            Log.i("MYSTORAGE", "DOWNLOAD ERROR: $e")
        }
    }

    private fun deleteFile(path: String) {
        val path = path
        DriverManager.println("path=%s$path")
        val storageReference = mAGCStorageManagement!!.getStorageReference(path)
        val deleteTask = storageReference.delete()
        deleteTask.addOnSuccessListener {
            Toast.makeText(context, "Delete Succeed", Toast.LENGTH_SHORT).show()
            activity.getAllList()
            notifyDataSetChanged()
        }
            .addOnFailureListener { e: Exception ->
                Toast.makeText(context, "Delete Error:", Toast.LENGTH_SHORT).show()
                Log.i("MYSTORAGE", "DELETE ERROR: $e")
            }
    }

    private val agcSdkDirPath2: String
        get() {
            val path =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absoluteFile.toString()
            DriverManager.println("path=$path")
            val dir = File(path)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return path
        }

    override fun getItemCount(): Int {
        return fileList.size
    }
}