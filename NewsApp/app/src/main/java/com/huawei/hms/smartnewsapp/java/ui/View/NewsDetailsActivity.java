package com.huawei.hms.smartnewsapp.java.ui.View;

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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.ActionBar;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.mlsdk.common.MLException;
import com.huawei.hms.mlsdk.translate.MLTranslatorFactory;
import com.huawei.hms.mlsdk.translate.cloud.MLRemoteTranslateSetting;
import com.huawei.hms.mlsdk.translate.cloud.MLRemoteTranslator;
import com.huawei.hms.mlsdk.tts.MLTtsConfig;
import com.huawei.hms.mlsdk.tts.MLTtsConstants;
import com.huawei.hms.mlsdk.tts.MLTtsEngine;
import com.huawei.hms.smartnewsapp.R;
import com.huawei.hms.smartnewsapp.java.util.Constants;

import dagger.android.support.DaggerAppCompatActivity;

/**
 * Activity to ddetailed news
 */
public class NewsDetailsActivity extends DaggerAppCompatActivity {
    private static final String EXTRA_SELECTED_NEWS_PUBLISHER = "EXTRA_SELECTED_NEWS_PUBLISHER";
    private static final String EXTRA_NEWS_URL = "EXTRA_NEWS_URL";
    private static final String EXTRA_NEWS_DESCRIPITION = "EXTRA_NEWS_DESCRIPTION";
    MLTtsEngine mlTtsEngine;
    MLTtsConfig mlTtsConfig;
    private String selectedNewsTitle;
    private String newsUrl;
    private String desc;
    private String selected_lang;
    public static final String TAG = "NewsDetailsActivity";
    private static final String API_KEY = "client/api_key";
    private Boolean readisClicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_details);
        getExtrasFromIntent(getIntent());
        ActionBar actionBar = getSupportActionBar();
        setActionBar(actionBar);
        initWebView(newsUrl);
        confMLkitTTS();
    }

    /**
     * gets the user selected language
     *
     * @return string for selected language
     */
    private String getSelectedLanguage() {
        SharedPreferences prefs = getSharedPreferences(Constants.MY_PREFS_NAME, MODE_PRIVATE);
        selected_lang = prefs.getString("language", "English");
        return selected_lang;
    }

    private void getExtrasFromIntent(Intent intent) {
        selectedNewsTitle = intent.getStringExtra(EXTRA_SELECTED_NEWS_PUBLISHER);
        newsUrl = intent.getStringExtra(EXTRA_NEWS_URL);
        desc = intent.getStringExtra(EXTRA_NEWS_DESCRIPITION);
        if (desc == null || desc.equals("")) {
            desc = "No enough content to be read";
        }
    }

    /**
     * to pass extras values about the article to newsdetails Activity
     *
     * @return intent
     */
    public static Intent newIntent(Context context, String articleTitle, String articleUrl, String desc) {
        Intent intent = new Intent(context, NewsDetailsActivity.class);
        intent.putExtra(EXTRA_SELECTED_NEWS_PUBLISHER, articleTitle);
        intent.putExtra(EXTRA_NEWS_URL, articleUrl);
        intent.putExtra(EXTRA_NEWS_DESCRIPITION, desc);
        return intent;
    }

    /**
     * ActionBar
     */
    private void setActionBar(ActionBar actionBar) {
        if (actionBar != null) {
            actionBar.setTitle(selectedNewsTitle);
            actionBar.setSubtitle(newsUrl);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Initialise the webview
     */
    private void initWebView(String url) {
        WebView webView = findViewById(R.id.web_view_news_details);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        supportFinishAfterTransition();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.audio) {
            setLanguage(getSelectedLanguage());
        } else if (item.getItemId() == android.R.id.home) {
            if (mlTtsEngine != null) {
                mlTtsEngine.shutdown();
            }
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_news_details, menu);
        return true;
    }

    /**
     * custom configuration class MLTtsConfig to create a text to speech engine.
     **/
    public void confMLkitTTS() {
        mlTtsConfig = new MLTtsConfig().setSpeed(1.0f).setVolume(1.0f);
        mlTtsEngine = new MLTtsEngine(mlTtsConfig);
        mlTtsEngine.updateConfig(mlTtsConfig);
    }

    /**
     * set language selected by user for Text to speech
     *
     * @param lang to convert the text  speech  in specified language
     */
    private void setLanguage(String lang) {
        switch (lang) {
            case "English":
                mlTtsConfig.setLanguage(MLTtsConstants.TTS_EN_US);
                mlTtsConfig.setPerson(MLTtsConstants.TTS_SPEAKER_FEMALE_EN);
                mlTtsEngine.speak(desc, MLTtsEngine.QUEUE_APPEND);
                break;
            case "Chinese":
                mlTtsConfig.setLanguage(MLTtsConstants.TTS_ZH_HANS);
                mlTtsConfig.setPerson(MLTtsConstants.TTS_SPEAKER_FEMALE_ZH);
                translateText(desc, "zh");
                break;
            case "French":
                mlTtsConfig.setLanguage(MLTtsConstants.TTS_LAN_FR_FR);
                mlTtsConfig.setPerson(MLTtsConstants.TTS_SPEAKER_FEMALE_FR);
                translateText(desc, "fr");
                break;
            case "Italian":
                mlTtsConfig.setLanguage(MLTtsConstants.TTS_LAN_IT_IT);
                mlTtsConfig.setPerson(MLTtsConstants.TTS_SPEAKER_FEMALE_IT);
                translateText(desc, "it");
                break;
            case "German":
                mlTtsConfig.setLanguage(MLTtsConstants.TTS_LAN_DE_DE);
                mlTtsConfig.setPerson(MLTtsConstants.TTS_SPEAKER_FEMALE_DE);
                translateText(desc, "de");
                break;
            case "Spanish":
                mlTtsConfig.setLanguage(MLTtsConstants.TTS_LAN_ES_ES);
                mlTtsConfig.setPerson(MLTtsConstants.TTS_SPEAKER_FEMALE_ES);
                translateText(desc, "es");
                break;
            default:
        }
    }

    /**
     * Translate the text to speech according to the language selected by the user
     *
     * @param text from the news and language selected by the user
     */
    public void translateText(String text, String lang) {
        MLRemoteTranslateSetting setting = new MLRemoteTranslateSetting.Factory().setTargetLangCode(lang).create();
        MLRemoteTranslator mlRemoteTranslator = MLTranslatorFactory.getInstance().getRemoteTranslator(setting);
        final Task<String> task = mlRemoteTranslator.asyncTranslate(text);
        task.addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(String text) {
                                mlTtsEngine.speak(text, MLTtsEngine.QUEUE_APPEND);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                try {
                                    MLException mlException = (MLException) e;
                                    Log.e(TAG, "failure to convert TTS");
                                } catch (Exception error) {
                                }
                            }
                        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mlTtsEngine != null) {
            mlTtsEngine.shutdown();
        }
    }
}
