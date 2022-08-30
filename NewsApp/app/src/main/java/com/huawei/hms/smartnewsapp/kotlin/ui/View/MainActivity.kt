/*
 *
 *  * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package com.huawei.hms.smartnewsapp.kotlin.ui.View

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.huawei.agconnect.crash.AGConnectCrash
import com.huawei.hmf.tasks.Task
import com.huawei.hms.smartnewsapp.R
import com.huawei.hms.smartnewsapp.kotlin.util.Constants
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper
import com.huawei.hms.support.hwid.result.AuthHuaweiId
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService
import com.huawei.hms.support.hwid.ui.HuaweiIdAuthButton
import com.kotlin.mvvm.ui.news.NewsActivity

/**
 * Activity that displays the silent login screen
 */
class MainActivity : AppCompatActivity() {
    lateinit var service: HuaweiIdAuthService
    lateinit var authParams: HuaweiIdAuthParams
    lateinit var huawei_SignIn: HuaweiIdAuthButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)
        huawei_SignIn = findViewById(R.id.hwi_sign_in)
        val agConnectCrash = AGConnectCrash.getInstance()
        agConnectCrash.enableCrashCollection(true)
        authParams = HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM).setAuthorizationCode().setEmail().createParams()
        service = HuaweiIdAuthManager.getService(this@MainActivity, authParams)
        silentIn()
        huawei_SignIn.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@MainActivity, NewsActivity::class.java)
            startActivity(intent)
        })
    }

    private fun silentIn() {
        val task: Task<AuthHuaweiId> = service.silentSignIn()

        task.addOnSuccessListener { authAccount ->
            val intent = Intent(this@MainActivity, NewsActivity::class.java)
            startActivity(intent)
            finish();
        }
        task.addOnFailureListener { e ->
            huawei_SignIn.isVisible = true
        }
    }

    /*
     * sign in
     *
     */
    private fun handleHuaweiSignIn() {

        startActivityForResult(service.signInIntent, Constants.REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Process the authorization result and obtain the authorization code from AuthHuaweiId.
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_CODE) {
            val authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data)
            if (authHuaweiIdTask.isSuccessful) {
                // The sign-in is successful, and the user's HUAWEI ID information and authorization code are obtained.
                val huaweiAccount = authHuaweiIdTask.result
                val name = huaweiAccount.displayName
                val email = huaweiAccount.email
                val editor = getSharedPreferences(Constants.MY_PREFS_NAME, MODE_PRIVATE).edit()
                editor.putBoolean("login", true)
                editor.putString("name", name)
                editor.putString("email", email)
                editor.apply()
                editor.commit()
                val intent = Intent(this@MainActivity, NewsActivity::class.java)
                startActivity(intent)

                Toast.makeText(this, applicationContext.resources.getString(R.string.able_to_login), Toast.LENGTH_LONG).show()
                finish()
            } else {
                // The sign-in failed.
                Log.e(TAG, application.resources.getString(R.string.sigin_failed))
                Toast.makeText(this, applicationContext.resources.getString(R.string.unable_to_login), Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        const val TAG = "SmartNewsApp"
    }
}