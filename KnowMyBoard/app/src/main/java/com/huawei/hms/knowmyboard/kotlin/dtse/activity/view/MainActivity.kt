package com.huawei.hms.knowmyboard.dtse.activity.view

import androidx.navigation.Navigation.findNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.huawei.hms.knowmyboard.dtse.activity.viewmodel.LoginViewModel
import android.graphics.Bitmap
import com.huawei.hms.mlsdk.langdetect.local.MLLocalLangDetector
import com.huawei.hms.mlsdk.translate.local.MLLocalTranslator
import android.app.ProgressDialog
import androidx.navigation.NavController
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.huawei.hms.knowmyboard.dtse.activity.app.MyApplication
import android.content.Intent
import com.huawei.hms.support.account.AccountAuthManager
import android.provider.MediaStore
import com.huawei.hmf.tasks.OnSuccessListener
import com.huawei.hms.mlsdk.langdetect.MLLangDetectorFactory
import com.huawei.hms.mlsdk.langdetect.local.MLLocalLangDetectorSetting
import com.huawei.hmf.tasks.OnFailureListener
import android.content.DialogInterface
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.huawei.hms.common.ApiException
import com.huawei.hms.knowmyboard.dtse.R
import com.huawei.hms.knowmyboard.dtse.activity.model.UserData
import com.huawei.hms.knowmyboard.dtse.activity.util.Constants
import com.huawei.hms.knowmyboard.dtse.databinding.ActivityMainBinding
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.common.MLApplication
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.translate.local.MLLocalTranslateSetting
import com.huawei.hms.mlsdk.translate.MLTranslatorFactory
import com.huawei.hms.mlsdk.model.download.MLModelDownloadStrategy
import com.huawei.hms.mlsdk.model.download.MLModelDownloadListener
import com.huawei.hms.mlsdk.text.MLLocalTextSetting
import com.huawei.hms.mlsdk.text.MLText
import com.huawei.hms.mlsdk.text.MLTextAnalyzer
import java.io.IOException
import java.lang.Exception
import java.util.ArrayList

class MainActivity() : AppCompatActivity() {
    var loginViewModel: LoginViewModel? = null
    private var mTextAnalyzer: MLTextAnalyzer? = null
    var imagePath: Uri? = null
    var bitmap: Bitmap? = null
    var result = ArrayList<String>()
    var myLocalLangDetector: MLLocalLangDetector? = null
    var myLocalTranslator: MLLocalTranslator? = null
    var textRecognized: String? = null
    var progressDialog: ProgressDialog? = null
    var navController: NavController? = null
    var activityMainBinding: ActivityMainBinding? = null
    var bottomNavigationView: BottomNavigationView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding =
            DataBindingUtil.setContentView(this@MainActivity, R.layout.activity_main)
        loginViewModel = ViewModelProvider(this@MainActivity).get(
            LoginViewModel::class.java
        )
        navController = findNavController(this@MainActivity, R.id.nav_host_fragment)
        MyApplication.activity = this
        progressDialog = ProgressDialog(this)
        progressDialog!!.setCancelable(false)
        bottomNavigationView = activityMainBinding!!.bottomNavigation
        setupWithNavController(bottomNavigationView!!, navController!!)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Process the authorization result to obtain the authorization code from AuthAccount.
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1234) {
            Log.e("TAG", " Result can be pulled")
        }
        if (requestCode == 8888) {
            val authAccountTask = AccountAuthManager.parseAuthResultFromIntent(data)
            if (authAccountTask.isSuccessful) {
                // The sign-in is successful, and the user's ID information and authorization code are obtained.
                val authAccount = authAccountTask.result
                val userData = UserData()
                userData.accessToken = authAccount.accessToken
                userData.countryCode = authAccount.countryCode
                userData.displayName = authAccount.displayName
                userData.email = authAccount.email
                userData.familyName = authAccount.familyName
                userData.givenName = authAccount.givenName
                userData.idToken = authAccount.idToken
                userData.openId = authAccount.openId
                userData.uid = authAccount.uid
                userData.photoUriString = authAccount.avatarUri.toString()
                userData.unionId = authAccount.unionId
                loginViewModel = ViewModelProvider(this@MainActivity).get(
                    LoginViewModel::class.java
                )
                loginViewModel!!.sendData(authAccount.displayName)
            } else {
                // The sign-in failed.
                Log.e(
                    "TAG",
                    "sign in failed:" + (authAccountTask.exception as ApiException).statusCode
                )
            }
        }
        if ((requestCode == 2323) && (resultCode == RESULT_OK) && (data != null)) {
            progressDialog!!.setMessage("Initializing text detection..")
            progressDialog!!.show()
            imagePath = data.data
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imagePath)
                asyncAnalyzeText(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
                Log.e("TAG", " BITMAP ERROR")
            }
        }
        if ((requestCode == 2424) && (resultCode == RESULT_OK) && (data != null)) {
            progressDialog!!.setMessage("Initializing text detection..")
            progressDialog!!.show()
            try {
                bitmap = data.extras!!["data"] as Bitmap?
                asyncAnalyzeText(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("TAG", " BITMAP ERROR")
            }
        }
    }

    private fun asyncAnalyzeText(bitmap: Bitmap?) {
        if (mTextAnalyzer == null) {
            createMLTextAnalyzer()
        }
        val frame = MLFrame.fromBitmap(bitmap)
        val task = mTextAnalyzer!!.asyncAnalyseFrame(frame)
        task.addOnSuccessListener(object : OnSuccessListener<MLText> {
            override fun onSuccess(text: MLText) {
                progressDialog!!.setMessage("Initializing language detection..")
                textRecognized = text.stringValue.trim { it <= ' ' }
                if (!textRecognized!!.isEmpty()) {
                    // Create a local language detector.
                    val factory = MLLangDetectorFactory.getInstance()
                    val setting =
                        MLLocalLangDetectorSetting.Factory() // Set the minimum confidence threshold for language detection.
                            .setTrustedThreshold(0.01f)
                            .create()
                    myLocalLangDetector = factory.getLocalLangDetector(setting)
                    val firstBestDetectTask = myLocalLangDetector!!.firstBestDetect(textRecognized)
                    firstBestDetectTask.addOnSuccessListener(OnSuccessListener { languageDetected ->
                        progressDialog!!.setMessage("Initializing text translation..")
                        // Processing logic for detection success.
                        textTranslate(languageDetected, textRecognized!!, bitmap)
                    }).addOnFailureListener(object : OnFailureListener {
                        override fun onFailure(e: Exception) {
                            // Processing logic for detection failure.
                            Log.e("TAG", "Lang detect error:" + e.message)
                        }
                    })
                } else {
                    progressDialog!!.dismiss()
                    showErrorDialog("Failed to recognize text.")
                }
            }
        }).addOnFailureListener(object : OnFailureListener {
            override fun onFailure(e: Exception) {
                Log.e("TAG", "#==>" + e.message)
            }
        })
    }

    private fun showErrorDialog(msg: String) {
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle("Error")
        alertDialog.setMessage(msg)
        alertDialog.setButton(
            AlertDialog.BUTTON_POSITIVE,
            "OK",
            object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface, which: Int) {
                    dialog.dismiss()
                }
            })
        alertDialog.show()
    }

    private fun textTranslate(languageDetected: String, textRecognized: String, uri: Bitmap?) {
        MLApplication.initialize(application)
        MLApplication.getInstance().apiKey = Constants.API_KEY

        // Create an offline translator.
        val setting =
            MLLocalTranslateSetting.Factory() // Set the source language code. The ISO 639-1 standard is used. This parameter is mandatory. If this parameter is not set, an error may occur.
                .setSourceLangCode(languageDetected) // Set the target language code. The ISO 639-1 standard is used. This parameter is mandatory. If this parameter is not set, an error may occur.
                .setTargetLangCode("en")
                .create()
        myLocalTranslator = MLTranslatorFactory.getInstance().getLocalTranslator(setting)
        // Set the model download policy.
        val downloadStrategy = MLModelDownloadStrategy.Factory()
            .needWifi() // It is recommended that you download the package in a Wi-Fi environment.
            .create()
        // Create a download progress listener.
        val modelDownloadListener: MLModelDownloadListener = object : MLModelDownloadListener {
            override fun onProcess(alreadyDownLength: Long, totalLength: Long) {
                runOnUiThread(object : Runnable {
                    override fun run() {
                        // Display the download progress or perform other operations.
                    }
                })
            }
        }
        myLocalTranslator!!.preparedModel(downloadStrategy, modelDownloadListener)
            .addOnSuccessListener(object : OnSuccessListener<Void?> {
                override fun onSuccess(aVoid: Void?) {
                    // Called when the model package is successfully downloaded.
                    // input is a string of less than 5000 characters.
                    val task = myLocalTranslator!!.asyncTranslate(textRecognized)
                    // Before translation, ensure that the models have been successfully downloaded.
                    task.addOnSuccessListener(object : OnSuccessListener<String> {
                        override fun onSuccess(translated: String) {
                            // Processing logic for detection success.
                            result.clear()
                            result.add(languageDetected.trim { it <= ' ' })
                            result.add(textRecognized.trim { it <= ' ' })
                            result.add(translated.trim { it <= ' ' })
                            loginViewModel!!.setImage(uri!!)
                            loginViewModel!!.setTextRecongnized(result)
                            progressDialog!!.dismiss()
                        }
                    }).addOnFailureListener(object : OnFailureListener {
                        override fun onFailure(e: Exception) {
                            // Processing logic for detection failure.
                            progressDialog!!.dismiss()
                        }
                    })
                }
            }).addOnFailureListener(object : OnFailureListener {
            override fun onFailure(e: Exception) {
                // Called when the model package fails to be downloaded.
                progressDialog!!.dismiss()
            }
        })
    }

    private fun createMLTextAnalyzer() {
        val setting = MLLocalTextSetting.Factory()
            .setOCRMode(MLLocalTextSetting.OCR_DETECT_MODE)
            .create()
        mTextAnalyzer = MLAnalyzerFactory.getInstance().getLocalTextAnalyzer(setting)
    }

    override fun onStop() {
        if (myLocalLangDetector != null) {
            myLocalLangDetector!!.stop()
        }
        if (myLocalTranslator != null) {
            myLocalTranslator!!.stop()
        }
        if (progressDialog != null) {
            progressDialog!!.dismiss()
        }
        super.onStop()
    }

    companion object {
        var TAG = "TAG"
    }
}