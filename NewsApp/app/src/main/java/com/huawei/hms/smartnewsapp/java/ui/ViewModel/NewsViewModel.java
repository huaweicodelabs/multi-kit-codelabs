package com.huawei.hms.smartnewsapp.java.ui.ViewModel;

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

import android.webkit.WebView;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.huawei.hms.smartnewsapp.java.data.model.Article;
import com.huawei.hms.smartnewsapp.java.data.model.Newsdata;
import com.huawei.hms.smartnewsapp.java.data.repository.NewsRepository;
import com.huawei.hms.smartnewsapp.java.util.Constants;
import com.huawei.hms.smartnewsapp.java.util.NetworkUtil;


import java.util.List;
import javax.inject.Inject;

/**
 * View model class to fetch news
 */
public class NewsViewModel extends ViewModel {
    private static final String TAG = "NewsViewModel";

    @Inject
    WebView webView;

    @Inject
    NetworkUtil networkUtil;

    private NewsRepository repository;
    private MutableLiveData<List<Article>> articleList = new MutableLiveData<>();
    private MutableLiveData<Boolean> saveNewsSuccess = new MutableLiveData<>();
    private LiveData<NewsResource<Newsdata>> newsResourceLiveData;
    private LiveData<Newsdata> newsLiveData;

    @Inject
    public NewsViewModel(NewsRepository newsRepository) {
        this.repository = newsRepository;
    }

    /**
     * View model initialiser
     */
    public void init() {
        if (newsResourceLiveData != null) {
            return;
        }
//        fetchNewsFromApi();
        initSearch("Today News");
    }

    public void initSearch(String text) {
        fetchNewsSearchApi(text);
    }

    public void fetchNewsSearchApi(String text) {
        repository.getNewsSearchApi(text);
        newsResourceLiveData =
                Transformations.map(
                        repository.getNews(),
                        new Function<Newsdata, NewsResource<Newsdata>>() {
                            @Override
                            public NewsResource<Newsdata> apply(Newsdata news) {
                                if (news.getStatus().equals(Constants.STATUS_FAILED)) {
                                    return NewsResource.error("Could not fetch from Search", null);
                                }
                                articleList.postValue(news.getArticle());
                                return NewsResource.success(news);
                            }
                        });


    }

    /**
     * fetch latest news from NEWSAPI
     */
    private void fetchNewsFromApi() {
//        repository.getNewsApi();
        newsResourceLiveData =
                Transformations.map(
                        repository.getNews(),
                        new Function<Newsdata, NewsResource<Newsdata>>() {
                            @Override
                            public NewsResource<Newsdata> apply(Newsdata news) {
                                if (news.getStatus().equals(Constants.STATUS_FAILED)) {
                                    return NewsResource.error("Could not fetch", null);
                                }
                                articleList.postValue(news.getArticle());
                                return NewsResource.success(news);
                            }
                        });
    }

    /**
     * get the list of news
     *
     * @return flowable news data
     */
    public LiveData<NewsResource<Newsdata>> getNewsList() {
        return newsResourceLiveData;
    }
    /**
     * status of the saved article
     *
     * @param position vlaue of the article in news list
     * @return boolean value for saved news
     */
    public boolean getArticleSavedState(int position) {
        return articleList.getValue().get(position).getArticleSaved();
    }

    /**
     * Save the selected news article
     *
     * @param position vlaue of the article in news list
     * @return boolean flowable for saved news
     */
    public LiveData<Boolean> saveNewsItem(int position) {
        boolean isSuccessfullySaved = repository.saveNewsItem(articleList.getValue().get(position));
        saveNewsSuccess.postValue(isSuccessfullySaved);
        if (isSuccessfullySaved) {
            saveWebViewCache(articleList.getValue().get(position).getUrl());
        }
        return saveNewsSuccess;
    }

    /**
     * delete the save article
     *
     * @param article that was saved
     * @return boolean value based on the delete status
     */
    public boolean deleteNewsArticle(Article article) {
        int result = repository.deleteNewsArticle(article);
        return result != -1;
    }

    private void saveWebViewCache(String url) {
        if (networkUtil.isNetworkConnected()) {
            webView.loadUrl(url);
        }
    }
}
