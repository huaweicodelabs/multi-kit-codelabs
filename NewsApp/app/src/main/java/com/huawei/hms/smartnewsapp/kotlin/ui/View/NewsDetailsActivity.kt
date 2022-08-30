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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.ActionBar
import com.huawei.agconnect.config.AGConnectServicesConfig
import com.huawei.hms.mlsdk.common.MLApplication
import com.huawei.hms.mlsdk.common.MLException
import com.huawei.hms.mlsdk.translate.MLTranslatorFactory
import com.huawei.hms.mlsdk.translate.cloud.MLRemoteTranslateSetting
import com.huawei.hms.mlsdk.tts.MLTtsConfig
import com.huawei.hms.mlsdk.tts.MLTtsConstants
import com.huawei.hms.mlsdk.tts.MLTtsEngine
import com.huawei.hms.smartnewsapp.R
import com.huawei.hms.smartnewsapp.kotlin.DaggerActivity
import com.huawei.hms.smartnewsapp.kotlin.util.Constants

/**
 * Activity to ddetailed news
 */
class NewsDetailsActivity : DaggerActivity() {
    var mlTtsEngine: MLTtsEngine? = null
    var mlTtsConfig: MLTtsConfig? = null
    private var selectedNewsTitle: String? = null
    private var newsUrl: String? = null
    private var desc: String? = null
    private var selected_lang: String? = null
    private val readisClicked = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_details)
        getExtrasFromIntent(intent)
        val actionBar = supportActionBar
        setActionBar(actionBar)
        initWebView(newsUrl)
        val config = AGConnectServicesConfig.fromContext(this@NewsDetailsActivity)
        MLApplication.getInstance().apiKey = config.getString(API_KEY)
        confMLkitTTS()
    }

    /**
     * gets the user selected language
     *
     * @return string for selected language
     */
    private val selectedLanguage: String?
        private get() {
            val prefs = getSharedPreferences(Constants.MY_PREFS_NAME, MODE_PRIVATE)
            selected_lang = prefs.getString("language", "English")
            return selected_lang
        }

    private fun getExtrasFromIntent(intent: Intent) {
        selectedNewsTitle = intent.getStringExtra(EXTRA_SELECTED_NEWS_PUBLISHER)
        newsUrl = intent.getStringExtra(EXTRA_NEWS_URL)
        desc = intent.getStringExtra(EXTRA_NEWS_DESCRIPITION)
        if (desc == null || desc == "") {
            desc = "No enough content to be read"
        }
    }

    /**
     * ActionBar
     */
    private fun setActionBar(actionBar: ActionBar?) {
        if (actionBar != null) {
            actionBar.title = selectedNewsTitle
            actionBar.subtitle = newsUrl
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
    }

    /**
     * Initialise the webview
     */
    private fun initWebView(url: String?) {
        val webView = findViewById<WebView>(R.id.web_view_news_details)
        webView.settings.loadsImagesAutomatically = true
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.setSupportZoom(true)
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        webView.scrollBarStyle = View.SCROLLBARS_INSIDE_OVERLAY
        webView.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        webView.webViewClient = WebViewClient()
        webView.loadUrl(url)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        supportFinishAfterTransition()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.audio) {
            Setlanguage(selectedLanguage)
        } else if (item.itemId == android.R.id.home) {
            if (mlTtsEngine != null) {
                mlTtsEngine!!.shutdown()
            }
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_news_details, menu)
        return true
    }

    /**
     * custom configuration class MLTtsConfig to create a text to speech engine.
     **/
    fun confMLkitTTS() {
        mlTtsConfig = MLTtsConfig()
                .setSpeed(1.0f)
                .setVolume(1.0f)
        mlTtsEngine = MLTtsEngine(mlTtsConfig)
        mlTtsEngine!!.updateConfig(mlTtsConfig)
    }

    /**
     * set language selected by user for Text to speech
     *
     * @param lang to convert the text  speech  in specified language
     */
    private fun Setlanguage(lang: String?) {
        when (lang) {
            "English" -> {
                mlTtsConfig!!.language = MLTtsConstants.TTS_EN_US
                mlTtsConfig!!.person = MLTtsConstants.TTS_SPEAKER_FEMALE_EN
                mlTtsEngine!!.speak(desc, MLTtsEngine.QUEUE_APPEND)
            }
            "Chinese" -> {
                mlTtsConfig!!.language = MLTtsConstants.TTS_ZH_HANS
                mlTtsConfig!!.person = MLTtsConstants.TTS_SPEAKER_FEMALE_ZH
                translateText(desc, "zh")
            }
            "French" -> {
                mlTtsConfig!!.language = MLTtsConstants.TTS_LAN_FR_FR
                mlTtsConfig!!.person = MLTtsConstants.TTS_SPEAKER_FEMALE_FR
                translateText(desc, "fr")
            }
            "Italian" -> {
                mlTtsConfig!!.language = MLTtsConstants.TTS_LAN_IT_IT
                mlTtsConfig!!.person = MLTtsConstants.TTS_SPEAKER_FEMALE_IT
                translateText(desc, "it")
            }
            "German" -> {
                mlTtsConfig!!.language = MLTtsConstants.TTS_LAN_DE_DE
                mlTtsConfig!!.person = MLTtsConstants.TTS_SPEAKER_FEMALE_DE
                translateText(desc, "de")
            }
            "Spanish" -> {
                mlTtsConfig!!.language = MLTtsConstants.TTS_LAN_ES_ES
                mlTtsConfig!!.person = MLTtsConstants.TTS_SPEAKER_FEMALE_ES
                translateText(desc, "es")
            }
            else -> {
            }
        }
    }

    /**
     * Translate the text to speech according to the language selected by the user
     *
     * @param text from the news and language selected by the user
     */
    fun translateText(text: String?, lang: String?) {
        val setting = MLRemoteTranslateSetting.Factory()
                .setTargetLangCode(lang)
                .create()
        val mlRemoteTranslator = MLTranslatorFactory.getInstance().getRemoteTranslator(setting)
        val task = mlRemoteTranslator.asyncTranslate(text)
        task.addOnSuccessListener { text -> mlTtsEngine!!.speak(text, MLTtsEngine.QUEUE_APPEND) }.addOnFailureListener { e ->
            try {
                val mlException = e as MLException
            } catch (error: Exception) {
                Log.e(TAG, "failure to convert TTS")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mlTtsEngine != null) {
            mlTtsEngine!!.shutdown()
        }
    }

    companion object {
        private const val EXTRA_SELECTED_NEWS_PUBLISHER = "EXTRA_SELECTED_NEWS_PUBLISHER"
        private const val EXTRA_NEWS_URL = "EXTRA_NEWS_URL"
        private const val EXTRA_NEWS_DESCRIPITION = "EXTRA_NEWS_DESCRIPTION"
        const val TAG = "NewsDetailsActivity"
        private const val API_KEY = "client/api_key"

        /**
         * to pass extras values about the article to newsdetails Activity
         *
         * @return intent
         */
        fun newIntent(context: Context?, articleTitle: String?, articleUrl: String?, desc: String?): Intent {
            val intent = Intent(context, NewsDetailsActivity::class.java)
            intent.putExtra(EXTRA_SELECTED_NEWS_PUBLISHER, articleTitle)
            intent.putExtra(EXTRA_NEWS_URL, articleUrl)
            intent.putExtra(EXTRA_NEWS_DESCRIPITION, desc)
            return intent
        }
    }
}