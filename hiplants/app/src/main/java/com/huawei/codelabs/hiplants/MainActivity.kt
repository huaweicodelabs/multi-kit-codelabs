package com.huawei.codelabs.hiplants


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.huawei.codelabs.hiplants.databinding.ActivityMainBinding
import com.huawei.codelabs.hiplants.model.WebSearchResults
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.classification.MLImageClassificationAnalyzer
import com.huawei.hms.mlsdk.common.LensEngine
import com.huawei.hms.mlsdk.common.MLException
import com.huawei.hms.mlsdk.custom.*
import com.huawei.hms.searchkit.SearchKitInstance
import java.io.IOException
import com.huawei.hms.searchkit.bean.WebSearchRequest
import com.huawei.hms.searchkit.utils.Language
import com.huawei.hms.searchkit.utils.Region
import java.nio.charset.Charset

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

class MainActivity : AppCompatActivity() {

    companion object {
        private const val RC_CHOOSE_PHOTO = 3
    }

    private var detector: ModelDetector? = null
    private var bitmap: Bitmap? = null

    val TAG = "MainActivity"
    val appID = "107265501"
    var token = ""


    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Initialize Search Kit.
        // appID is obtained after your app is created in AppGallery Connect. Its value is of the String type.
        SearchKitInstance.init(this, appID);

        postVolley()

        requestCameraPermissions()

        binding.getImageFromGalleryButton.setOnClickListener {
            openGallery()

        }

        binding.predictButton.setOnClickListener {

            detector = ModelDetector(this)
            detector!!.loadModelFromAssets()
            runOnClick()

        }


    }  //End of the onCreate()


    /**
     * Open Gallery and catch gallery result from onActivityResult
     */
    fun openGallery() {
        val intentToPickPic = Intent(Intent.ACTION_PICK, null)
        intentToPickPic.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        startActivityForResult(intentToPickPic, RC_CHOOSE_PHOTO)
    }


    /**
     * This function allows us to obtain camera permissions from the user.
     * @author Hüseyin Özkoç
     */
    fun requestCameraPermissions() {
        // Dynamically apply for required permissions if the API level is 28 or smaller.
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            Log.i("TAG", "android sdk <= 28 Q")
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val strings = arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                ActivityCompat.requestPermissions(this, strings, 1)
            }
        } else {
            // Dynamically apply for required permissions if the API level is greater than 28. The android.permission.ACCESS_BACKGROUND_LOCATION permission is required.
            val strings = arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
            )
            ActivityCompat.requestPermissions(this, strings, 2)

        }


    } //End of requestCameraPermissions()


    /**
     * This function checks whether the user has defined permissions.
     * @author Hüseyin Özkoç
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1) {
            if (grantResults.size > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {

                //initAnalyzer()
                Log.i(
                    "TAG", "onRequestPermissionsResult: apply CAMERA PERMISSION successful"
                )
            } else {
                Log.i(
                    "TAG", "onRequestPermissionsResult: apply CAMERA PERMISSION  failed"
                )
            }
        }
        if (requestCode == 2) {
            if (grantResults.size > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                //initAnalyzer()

                Log.i(
                    "TAG", "onRequestPermissionsResult: apply CAMERA successful"
                )
            } else {

                Log.i(
                    "TAG", "onRequestPermissionsResult: apply CAMERA  failed"
                )
            }
        }

    }   //End Of onRequestPermissionsResult()


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_CHOOSE_PHOTO && resultCode == RESULT_OK) {
            Log.d(TAG, "BAŞARILI BİR ŞEKİLDE SEÇİLDİ.")
            bitmap = processIntent(requestCode, resultCode, data)
            binding.imageView.setImageBitmap(bitmap)
        }

    }

    private fun processIntent(requestCode: Int, resultCode: Int, data: Intent?): Bitmap? {
        if (requestCode == RC_CHOOSE_PHOTO) {
            if (data == null) {
                return null
            }
            val uri: Uri? = data.data
            val filePath = FileUtil.getFilePathByUri(this, uri!!)
            if (!TextUtils.isEmpty(filePath)) {
                Log.e(TAG, "file is $filePath")
                return BitmapFactory.decodeFile(filePath)
            }
        }
        return null
    }

    private fun runOnClick() {
        detector!!.predict(bitmap,
            { mlModelOutputs ->
                Log.i(TAG, "interpret get result")
                val result = detector!!.resultPostProcess(mlModelOutputs!!)
                showResult(result)

                val words = result.split("\\P{L}+".toRegex())

                Log.i(TAG, "result1 : $words")
                Log.i(TAG, "result2 : " + words[1])
                Log.i(TAG, "result3  : $result")
                searchRequest(words[1] + " flower")

            }) { e ->
            e.printStackTrace()
            Log.e(TAG, "interpret failed, because " + e.message)
            Toast.makeText(
                this@MainActivity,
                "interpret failed, because" + e.message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showResult(result: String?) {
        binding.results.text = result
        binding.results.visibility = View.VISIBLE
        // Toast.makeText(this, result, Toast.LENGTH_LONG).show()
    }


    fun postVolley() {
        val queue = Volley.newRequestQueue(this)
        val url = "https://oauth-login.cloud.huawei.com/oauth2/v3/token"

        val stringReq: StringRequest =
            object : StringRequest(Method.POST, url,
                Response.Listener { response ->
                    // response
                    var strResp = response.toString()

                    val gson = Gson()
                    val mapType = object : TypeToken<Map<String, Any>>() {}.type

                    var tutorialMap: Map<String, Any> =
                        gson.fromJson(strResp, object : TypeToken<Map<String, Any>>() {}.type)
                    tutorialMap.forEach { println(it) }
                    var access_token = tutorialMap.getValue("access_token").toString()
                    token = access_token
                    Log.d("API1", access_token)
                    Log.d("API1", strResp)

                },
                Response.ErrorListener { error ->
                    Log.d("API", "error => $error")
                }
            ) {
                override fun getParams(): MutableMap<String, String> {
                    var params: MutableMap<String, String> = HashMap<String, String>()
                    params.put("grant_type", "client_credentials")
                    params.put("client_id", "107265501")
                    params.put(
                        "client_secret",
                        "5940291352a24ec965f721419fe1899dc075ad9d38c6691a2afbc7ca69debfb9"
                    )
                    return params
                }
            }
        queue.add(stringReq)
    }


    fun searchRequest(word: String): String {


        val webSearchRequest = WebSearchRequest()
        // Set the search keyword. (The following uses test as an example. You can set other keywords as required.)
        webSearchRequest.setQ(word)
        // Set the language for search.
        webSearchRequest.setLang(Language.ENGLISH)
        // Set the region for search.
        webSearchRequest.setSregion(Region.UNITEDKINGDOM)
        // Set the number of search results returned on a page.
        webSearchRequest.setPs(10)
        // Set the page number.
        webSearchRequest.setPn(1)

        //webSearchRequest.setWithin("wikipedia.com");


        SearchKitInstance.getInstance().setInstanceCredential(token)
        val webSearchResponse =
            SearchKitInstance.getInstance().webSearcher.search(webSearchRequest)

        Log.d(
            TAG,
            "Response success ${webSearchResponse?.getCode()} || ${webSearchResponse?.getMsg()}"
        )
        Log.d(
            TAG,
            "Response success ${webSearchResponse?.getCode()} || ${webSearchResponse?.getData()}"
        )
        Log.d(
            TAG,
            "Response success ${webSearchResponse?.getCode()} || ${webSearchResponse?.data}"
        )


        val resultList: ArrayList<WebSearchResults> = ArrayList()

        for (i in webSearchResponse.getData()) {
            Log.i(
                TAG, "site_name : " + i.site_name + "\n"
                        + "getSnippet : " + i.getSnippet() + "\n"
                        + "siteName : " + i.siteName + "\n"
                        + "title : " + i.title + "\n"
                        + "clickUrl : " + i.clickUrl + "\n"
                        + "click_url : " + i.click_url + "\n"
                        + "getTitle : " + i.getTitle()
            )

            val result = WebSearchResults(i.title, i.title, i.clickUrl)
            resultList.add(result)

        }

        for (i in resultList) {
            Log.d(
                TAG, "title : " + i.title + "\n"
                        + "snippet : " + i.snippet + "\n"
                        + "url : " + i.url + "\n"
            )
        }

        val dialog = ResultDialog(resultList)
        dialog.show(supportFragmentManager, "ResultDialogFragment")



        return ""
    }


}