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
package com.huawei.hms.smartnewsapp.kotlin.data.repository

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.huawei.hms.network.httpclient.Callback
import com.huawei.hms.network.httpclient.Response
import com.huawei.hms.network.httpclient.Submit
import com.huawei.hms.searchkit.SearchKitInstance
import com.huawei.hms.searchkit.bean.CommonSearchRequest
import com.huawei.hms.searchkit.utils.Language
import com.huawei.hms.searchkit.utils.Region
import com.huawei.hms.smartnewsapp.kotlin.data.api.ApiService
import com.huawei.hms.smartnewsapp.kotlin.data.model.news.TokenModel
import com.huawei.hms.smartnewsapp.kotlin.util.Constants
import com.kotlin.mvvm.repository.model.news.News
import com.kotlin.mvvm.repository.model.news.NewsSource
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Repository abstracts the logic of fetching the data
 * They are the data source as the single source of truth.
 */

@Singleton
class NewsRepository @Inject constructor(
    private val apiServices: ApiService,
    private val context: Context
) {
    lateinit var mCallback: NetworkResponseCallback
    private var mCountryList: MutableLiveData<List<News>> =
        MutableLiveData<List<News>>().apply { value = emptyList() }


    fun getNewsArticlesFromSearch(
        text: String,
        callback: NetworkResponseCallback
    ): MutableLiveData<List<News>> {
        mCallback = callback
        apiServices.getAccessToken(
            Constants.GRANT_TYPE,
            Constants.CLIENT_SECRET,
        Constants.CLIENT_ID
        )?.enqueue(object : Callback<String?>() {
            override fun onResponse(body: Submit<String?>?, response: Response<String?>?) {
                Handler(Looper.getMainLooper()).postDelayed({
                    val gson = Gson()
                    val tokenModel: TokenModel =
                        gson.fromJson(response!!.body, TokenModel::class.java)
                   val mSearchNews = searchForQuery(tokenModel.access_token, context, text)
                    mCountryList.value = mSearchNews
                    mCallback.onNetworkSuccess()

                }, 1000)
            }

            override fun onFailure(p0: Submit<String?>?, p1: Throwable) {
                mCallback.onFailure("Api Failure")
            }
        })
        return mCountryList
    }

    /**
     * Search for the Query enteredby the user
     */
    private fun searchForQuery(
        accessToken: String?,
        context: Context,
        text: String
    ): ArrayList<News> {
        var mSearchNews = ArrayList<News>()
        val commonSearchRequest = CommonSearchRequest()
        commonSearchRequest.setQ(text)
        commonSearchRequest.setLang(Language.ENGLISH)
        commonSearchRequest.setSregion(Region.UNITEDKINGDOM)
        commonSearchRequest.setPs(10)
        commonSearchRequest.setPn(1)
        SearchKitInstance.init(context, Constants.CLIENT_ID)
        SearchKitInstance.getInstance().setInstanceCredential(accessToken)
        SearchKitInstance.getInstance().newsSearcher.setTimeOut(10000)
        val response = SearchKitInstance.getInstance().newsSearcher.search(commonSearchRequest)
        if (response.getData().isEmpty() || !(!response.equals("") || response.getData() != null)) {
            mSearchNews = arrayListOf()
            mCallback.onFailure("No data Found")
        } else {
            val newsitem = response!!.getData()
            for (i in newsitem.indices) {
                mSearchNews.add(
                    News(
                        null,
                        newsitem[i].title,
                            newsitem[i].title,
                        newsitem[i].clickUrl,
                        newsitem[i].provider.logo,
                        newsitem[i].publish_time
                    )
                )
            }
            mCallback.onNetworkSuccess()
        }
        return mSearchNews;
    }
}