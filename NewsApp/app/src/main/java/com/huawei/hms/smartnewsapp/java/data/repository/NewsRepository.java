package com.huawei.hms.smartnewsapp.java.data.repository;

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

import androidx.lifecycle.LiveData;

import com.huawei.hms.smartnewsapp.java.data.api.NewsApiClient;
import com.huawei.hms.smartnewsapp.java.data.model.Article;
import com.huawei.hms.smartnewsapp.java.data.model.Newsdata;

import javax.inject.Inject;


public class NewsRepository {
    private NewsDatabaseHelper newsDatabaseHelper;
    private NewsApiClient newsApiClient;

    @Inject
    public NewsRepository(NewsDatabaseHelper newsDatabaseHelper, NewsApiClient newsApiClient) {
        this.newsDatabaseHelper = newsDatabaseHelper;
        this.newsApiClient = newsApiClient;
    }

    /**
     * To save selected news article from db
     *
     * @param saveArticle to save the article
     * @return value for saved status
     */
    public boolean saveNewsItem(Article saveArticle) {
        return newsDatabaseHelper.insertNews(saveArticle);
    }

    /**
     * To delete  news article from db
     *
     * @param deleteArticle to delete article
     * @return integer
     */
    public int deleteNewsArticle(Article deleteArticle) {
        return newsDatabaseHelper.deleteNewsArticle(deleteArticle);
    }

   /* public void getNewsApi() {
        newsApiClient.getNewsApi();
    }*/

    /*
     * To fetch news
     *
     * @return  news flowable
     */

    public LiveData<Newsdata> getNews() {
        return newsApiClient.getNews();
    }

    /*
     * To fetch news for search
     *
     * @return  news flowable
     */

    public void getNewsSearchApi(String text) {
        newsApiClient.getNewsSearchApi(text);
    }
}
