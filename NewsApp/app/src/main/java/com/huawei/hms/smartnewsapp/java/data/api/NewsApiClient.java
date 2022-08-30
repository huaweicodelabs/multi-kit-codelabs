package com.huawei.hms.smartnewsapp.java.data.api;

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
import android.util.Log;
import android.widget.Toast;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.huawei.hms.network.httpclient.Callback;
import com.huawei.hms.network.httpclient.Response;
import com.huawei.hms.network.httpclient.Submit;
import com.huawei.hms.network.restclient.RestClient;
import com.huawei.hms.searchkit.SearchKitInstance;
import com.huawei.hms.searchkit.bean.BaseSearchResponse;
import com.huawei.hms.searchkit.bean.CommonSearchRequest;
import com.huawei.hms.searchkit.bean.NewsItem;
import com.huawei.hms.searchkit.utils.Language;
import com.huawei.hms.searchkit.utils.Region;
import com.huawei.hms.smartnewsapp.java.data.model.Article;
import com.huawei.hms.smartnewsapp.java.data.model.Newsdata;
import com.huawei.hms.smartnewsapp.java.data.model.Source;
import com.huawei.hms.smartnewsapp.java.data.model.TokenModel;
import com.huawei.hms.smartnewsapp.java.util.Constants;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public class NewsApiClient {
    private static final String TAG = "NewsApiClient";

    @Inject
    RestClient restClient;

    private MutableLiveData<Newsdata> mNews;
    Newsdata newsStatus;
    Callback mcallback, searchCallback;
    Context context;

    @Inject
    public NewsApiClient() {
        mNews = new MutableLiveData<>();
    }

    public LiveData<Newsdata> getNews() {
        return mNews;
    }

    /**
     * Api call to fetch news when internet connection is available
     */
    /*public void getNewsApi() {
        newsStatus = new Newsdata();
        newsStatus.setStatus(Constants.STATUS_FAILED);
        callbackForNewsApi();
        Submit<String> newsCall = getNewsCall();
        newsCall.enqueue(mcallback);
    }*/

    /**
     * Api call to fetch news from Search Kit
     */
    public void getNewsSearchApi(String text) {
        newsStatus = new Newsdata();
        newsStatus.setStatus(Constants.STATUS_FAILED);
        searchCallback(text);
        Submit<String> searchcall = getNewsSearch();
        searchcall.enqueue(searchCallback);
    }



    /**
     * Api call to get Acccess Token for Search Kit
     */
    private Submit<String> getNewsSearch() {
        return restClient
                .create(AccessTokenService.class)
                .createAccessToken(Constants.GRANT_TYPE, Constants.CLIENT_SECRET, Constants.CLIENT_ID);
    }
    /**
     *  Use the request API object to send an asynchronous request
     */
    private void callbackForNewsApi() {
        mcallback =
                new Callback() {
                    @Override
                    public void onResponse(Submit submit, Response response) throws IOException {
                        if (response != null && response.getBody() != null) {
                            processBody(response);
                        }
                    }

                    @Override
                    public void onFailure(Submit submit, Throwable throwable) {
                        String errorMsg = "response onFailure : ";
                        Log.e(TAG, errorMsg);
                        Toast.makeText(context.getApplicationContext() , "No match for serach" ,Toast.LENGTH_SHORT).show();
                        if (throwable != null) {
                            errorMsg += throwable.getMessage();
                            if (throwable.getCause() != null) {
                                errorMsg += ", cause : " + throwable.getMessage();
                            }
                        }
                    }
                };
    }

    public void searchCallback(String text) {
        searchCallback =
                new Callback() {
                    @Override
                    public void onResponse(Submit submit, Response response) throws IOException {
                            Gson gson = new Gson();
                            TokenModel token = gson.fromJson(response.getBody().toString() , TokenModel.class);
                            searchForQuery(token.access_token, text);
                    }

                    @Override
                    public void onFailure(Submit submit, Throwable throwable) {
                        String errorMsg = "response onFailure : ";
                        Log.e(TAG, errorMsg);
                        if (throwable != null) {
                            errorMsg += throwable.getMessage();
                            if (throwable.getCause() != null) {
                                errorMsg += ", cause : " + throwable.getMessage();
                            }
                        }
                    }
                };
    }

    /**
     * Search for the Query enteredby the user
     */
    private void searchForQuery(String accessToken, String text) {
        CommonSearchRequest commonSearchRequest = new CommonSearchRequest();
        commonSearchRequest.setQ(text);
        commonSearchRequest.setLang(Language.ENGLISH);
        commonSearchRequest.setSregion(Region.UNITEDKINGDOM);
        commonSearchRequest.setPs(10);
        commonSearchRequest.setPn(1);
        SearchKitInstance.getInstance().setInstanceCredential(accessToken);
        SearchKitInstance.getInstance().getNewsSearcher().setTimeOut(10000);
        BaseSearchResponse<List<NewsItem>> response =
                SearchKitInstance.getInstance().getNewsSearcher().search(commonSearchRequest);
        if (response.getData().isEmpty() || response == null || response.equals("") ) {
            newsStatus.setStatus(Constants.STATUS_FAILED);
            mNews.postValue(newsStatus);
            return;
        }
        List<NewsItem> newinfo = response.getData();
        processbodyforsearch(newinfo);
    }

    private void processbodyforsearch(List<NewsItem> newinfo) {
        List<NewsItem> newsitem = newinfo;
        List<Article> arctileList = new ArrayList<>();
        for (int i = 0; i < newsitem.size(); i++) {
            arctileList.add(
                    new Article(
                            new Source(null, newsitem.get(i).provider.site_name),
                            null,
                            newsitem.get(i).title,
                            newsitem.get(i).title,
                            newsitem.get(i).click_url,
                            newsitem.get(i).provider.getLogo(),
                            newsitem.get(i).publish_time,
                            false));
        }
        newsStatus.setStatus(Constants.STATUS_OK);
        newsStatus.setTotalResults(newsitem.size());
        newsStatus.setArticle(arctileList);
        mNews.postValue(newsStatus);
    }

    private void processBody(Response response) {
        Gson gson = new Gson();
         Newsdata data = gson.fromJson(response.getBody().toString(), Newsdata.class);
          mNews.postValue(data);
    }
}
