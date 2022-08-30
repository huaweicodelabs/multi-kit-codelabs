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
package com.kotlin.mvvm.ui.news

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.huawei.hms.smartnewsapp.kotlin.data.repository.NetworkResponseCallback
import com.huawei.hms.smartnewsapp.kotlin.data.repository.NewsRepository
import com.kotlin.mvvm.repository.model.news.News
import javax.inject.Inject



/**
 * A container for call [News] API.
 */
class NewsViewModel @Inject constructor(
    private val newsRepository: NewsRepository
) : ViewModel() {
    private var mList: MutableLiveData<List<News>> =
        MutableLiveData<List<News>>().apply { value = emptyList() }

    val mShowProgressBar = MutableLiveData(true)
    val mShowApiError = MutableLiveData<String>()


    fun initSearch(text: String) {
        fetchNewsSearchApi(text)
    }

    fun fetchNewsSearchApi(text: String): MutableLiveData<List<News>> {
        mShowProgressBar.value = true
        mList = newsRepository.getNewsArticlesFromSearch(text, object : NetworkResponseCallback {
            override fun onNetworkSuccess() {
                mShowProgressBar.value = false
            }

            override fun onFailure(message:String) {
                mShowApiError.value = message
            }

        })
        return mList

    }
}