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
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.huawei.agconnect.AGConnectInstance
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.cloud.storage.core.AGCStorageManagement
import com.huawei.hmf.tasks.Task
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentation
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentationAnalyzer
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentationScene
import com.huawei.hms.mlsdk.imgseg.MLImageSegmentationSetting
import com.huawei.hms.imagebgcleaner.databinding.ActivityMainBinding
import java.io.File
import java.io.OutputStream
import java.sql.DriverManager
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var analyzer: MLImageSegmentationAnalyzer
    private lateinit var imageUri: Uri
    private var bitmap: Bitmap? = null
    private var path: String? = null
    private var mAGCStorageManagement: AGCStorageManagement? = null
    private val permissions = arrayOf<String>(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    companion object {
        private const val IMAGE_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AGConnectInstance.initialize(applicationContext)
        ActivityCompat.requestPermissions(this@MainActivity, permissions, 1)
        login()
        buttonPickImage()
        buttonRemoveBG()
        initAGCStorageManagement()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.saveImage -> {
                Toast.makeText(this, "SAVE IMAGE", Toast.LENGTH_SHORT).show()
                val bitmap = getImageOfView(binding.imageView)
                if (bitmap != null) {
                    saveImageToGallery()
                }
            }
            R.id.uploadImage -> {
                uploadFile()
                Toast.makeText(this, "UPLOAD IMAGE", Toast.LENGTH_SHORT).show()
            }
            R.id.downloadFile -> {
                val intent = Intent(this, DownloadListActivity::class.java)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun login() {
        if (AGConnectAuth.getInstance().currentUser != null) {
            DriverManager.println("already sign a user")
            return
        } else {
            AGConnectAuth.getInstance().signInAnonymously()
                .addOnSuccessListener {
                    DriverManager.println("AGConnect OnSuccess")
                }
                .addOnFailureListener { e ->
                    DriverManager.println("AGConnect OnFail: " + e.message)
                }
        }
    }

    private fun initAGCStorageManagement() {
        mAGCStorageManagement = AGCStorageManagement.getInstance("bucket name")
    }

    private fun uploadFile() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Uploading File....")
        progressDialog.setCancelable(false)
        progressDialog.show()
        val systemCurrentTime: Long = System.currentTimeMillis()
        val path = "/images/${systemCurrentTime}.png"
        val agcSdkDirPath = agcSdkDirPath
        val file = File(agcSdkDirPath)
        if (!file.exists()) {
            return
        } else {
            val storageReference =
                mAGCStorageManagement!!.getStorageReference(path)
            val uploadTask = storageReference.putFile(file)

            uploadTask.addOnSuccessListener {
                progressDialog.dismiss()
            }
                .addOnFailureListener { e: Exception ->
                    progressDialog.dismiss()
                }
        }
    }

    private fun saveImageToGallery() {
        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this@MainActivity, permissions, 1)
            val images: Uri
            val contentResolver = contentResolver
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                images = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
            val contentValues: ContentValues = ContentValues()
            contentValues.put(
                MediaStore.Images.Media.DISPLAY_NAME,
                "${System.currentTimeMillis()}.png"
            )
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "images/*")
            val uri: Uri? = contentResolver.insert(images, contentValues)

            try {
                val bitmapDrawable: BitmapDrawable = binding.imageView.drawable as BitmapDrawable
                val bitmap: Bitmap = bitmapDrawable.bitmap
                val outputStream: OutputStream? =
                    contentResolver.openOutputStream(Objects.requireNonNull(uri!!))
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                Objects.requireNonNull(outputStream)

                Toast.makeText(this, "Image saved successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Image not saved, try again", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private val agcSdkDirPath: String
        get() {
            DriverManager.println("path=$path")
            val dir = File(path)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return path!!
        }

    private fun getImageOfView(view: ImageView): Bitmap? {
        var image: Bitmap? = null
        try {
            image = Bitmap.createBitmap(
                view.measuredWidth,
                view.measuredHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(image)
            view.draw(canvas)
        } catch (e: Exception) {
            Log.e("SAVE IMAGE", "Cannot Saved")
        }
        return image
    }

    private fun buttonRemoveBG() {
        binding.btnRemoveBg.setOnClickListener {
            createAnalyzer()
        }
    }

    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //Android is 11(R) is above
            Environment.isExternalStorageManager()
        } else {
            //Android is 11(R) is below
            val write =
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read =
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_GRANTED

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(this, "Please provide required permission", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }

    private fun buttonPickImage() {
        binding.btnSetImg.setOnClickListener {
            if (checkPermission()) {
                pickImageGallery()
            } else {
                alertDialog()
            }
        }
    }

    private fun alertDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(resources.getString(R.string.alert_permisson_title))
        builder.setMessage(R.string.alert_permission_message)
        builder.setPositiveButton("Open Settings") { dialog, which ->
            openSettings()
        }
        builder.setNegativeButton("Exit App") { dialog, which ->
            this.finish()
        }
        builder.show()
    }

    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val uri = Uri.fromParts("package", packageName, null)
        intent.setData(uri)
        startActivity(intent)
    }

    private fun pickImageGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_PICK
        startActivityForResult(intent, IMAGE_REQUEST_CODE)
    }

    private fun createAnalyzer() {
        var setting = MLImageSegmentationSetting.Factory()
            .setExact(true)
            .setAnalyzerType(MLImageSegmentationSetting.BODY_SEG)
            .setScene(MLImageSegmentationScene.FOREGROUND_ONLY)
            .create()
        analyzer = MLAnalyzerFactory.getInstance().getImageSegmentationAnalyzer(setting)
        val drawable: BitmapDrawable = binding.imageView.drawable as BitmapDrawable
        bitmap = drawable.bitmap
        val mlFrame = MLFrame.Creator().setBitmap(bitmap).create()
        val task: Task<MLImageSegmentation> = analyzer.asyncAnalyseFrame(mlFrame)
        task.addOnSuccessListener {
            removeBackGround(it)
        }.addOnFailureListener {
        }
    }

    private fun removeBackGround(mlImageSegmentation: MLImageSegmentation?) {
        if (mlImageSegmentation != null) {
            if (bitmap == null) {
                return
            }
            val bitmapFore: Bitmap = mlImageSegmentation.getForeground()
            if (bitmapFore != null) {
                binding.imageView.setImageBitmap(bitmapFore)
            } else {
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            imageUri = data?.data!!
            binding.imageView.setImageURI(imageUri)
            path = RealPathUtil.getRealPath(this@MainActivity, imageUri)
            bitmap = BitmapFactory.decodeFile(path)
            binding.imageView.setImageBitmap(bitmap)
        }
    }
}