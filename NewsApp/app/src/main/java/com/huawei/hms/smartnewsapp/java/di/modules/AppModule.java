package com.huawei.hms.smartnewsapp.java.di.modules;

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

import android.app.Application;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.huawei.hms.network.httpclient.HttpClient;
import com.huawei.hms.network.restclient.RestClient;
import com.huawei.hms.smartnewsapp.java.data.repository.NewsDatabaseHelper;


import com.google.gson.Gson;
import com.huawei.hms.smartnewsapp.java.util.Constants;
import com.huawei.hms.smartnewsapp.java.util.NetworkUtil;

import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;


@Module(includes = NewsViewModelsModule.class)
public class AppModule {
    @Singleton
    @Provides
    static NewsDatabaseHelper provideNewsDatabaseHelper(Application application, Gson gson) {
        return new NewsDatabaseHelper(application, gson);
    }

    @Singleton
    @Provides
    static NetworkUtil provideNetworkUtil(Application application) {
        return new NetworkUtil(application);
    }

    @Singleton
    @Provides
    static WebView provideWebView(Application application) {
        WebView webView = new WebView(application.getApplicationContext());
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAppCachePath(application.getCacheDir().getAbsolutePath());
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.setWebViewClient(new WebViewClient());
        return webView;
    }

    @Singleton
    @Provides
    static Gson provideGson() {
        return new Gson();
    }

    /**
     * Provides a rest client service from Network kit.
     */
    @Singleton
    @Provides
    static RestClient provideNetworkService() {
        return new RestClient.Builder().baseUrl(Constants.BASE_URL).httpClient(getHttpClient()).build();
    }

    private static HttpClient getHttpClient() {
        return new HttpClient.Builder()
                .connectTimeout(Constants.CONNECT_TIMEOUT)
                .readTimeout(Constants.TIMEOUT)
                .writeTimeout(Constants.WRITE_TIMEOUT)
                .build();
    }
}
