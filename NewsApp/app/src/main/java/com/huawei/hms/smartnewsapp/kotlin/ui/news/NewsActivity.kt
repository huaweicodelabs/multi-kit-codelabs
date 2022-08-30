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

import android.Manifest
import android.app.SearchManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.huawei.hms.mlplugin.asr.MLAsrCaptureActivity
import com.huawei.hms.mlplugin.asr.MLAsrCaptureConstants
import com.huawei.hms.smartnewsapp.R
import com.huawei.hms.smartnewsapp.kotlin.DaggerActivity
import com.huawei.hms.smartnewsapp.kotlin.ui.View.NewsDetailsActivity
import com.huawei.hms.smartnewsapp.kotlin.ui.View.Settings
import com.huawei.hms.smartnewsapp.kotlin.ui.news.NewsListAdapter
import com.kotlin.mvvm.repository.model.news.News
import java.util.*


/**
 * Activity that displays the News Info
 */
class NewsActivity : DaggerActivity(), NewsListAdapter.OnNewsClickListener {

    private lateinit var progressBar: ProgressBar

    private lateinit var newsError: TextView
    private var mAdapter: NewsListAdapter? = null
    private var recyclerView: RecyclerView? = null
    private var latestNews: List<News> = ArrayList()
    var bottomNavigationView: BottomNavigationView? = null
    var search: SearchView? = null
    var menu: Menu? = null
    private val REQUEST_CODE_ASR = 100
    private val AUDIO_PERMISSION_CODE = 1


    private val newsArticleViewModel: NewsViewModel by viewModels {
        viewModelFactory
    }

    /**
     * On Create Of Activity
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.newslist)
        progressBar = (findViewById(R.id.progress_bar))!!
        newsError = findViewById(R.id.news_error)
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        initBottomNaviagtion()
        setupRecyclerView()
        getNews()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            this.requestAudioPermission();
        }
    }

    /**
     * Check the permissions required by the SDK.
     */
    private fun requestAudioPermission() {
        val permissions =
            arrayOf(Manifest.permission.RECORD_AUDIO)
        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.RECORD_AUDIO
            )
        ) {
            ActivityCompat.requestPermissions(this, permissions, AUDIO_PERMISSION_CODE)
            return
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode != AUDIO_PERMISSION_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView?.layoutManager = LinearLayoutManager(this)
        recyclerView?.itemAnimator = DefaultItemAnimator()
        recyclerView?.isNestedScrollingEnabled = false
    }



    private fun getNews() {

        newsArticleViewModel.fetchNewsSearchApi("Today News").observe(this, Observer {
            latestNews = it
            if (latestNews.isNotEmpty()){
                recyclerView!!.visibility = View.VISIBLE
                newsError.visibility = View.GONE
                mAdapter = NewsListAdapter(
                    latestNews,
                    this@NewsActivity,
                    this@NewsActivity
                )
                recyclerView!!.adapter = mAdapter
                mAdapter!!.notifyDataSetChanged()
            }

        })
        newsArticleViewModel.mShowApiError.observe(this, Observer {
            recyclerView!!.visibility = View.GONE
            newsError.visibility = View.VISIBLE
            Toast.makeText(this, it, Toast.LENGTH_LONG).show()
        })

        newsArticleViewModel.mShowProgressBar.observe(this, Observer { bt ->
            if (bt) {
                showProgressBar(true)
            } else {
                showProgressBar(false)
            }
        })
    }

    override fun onNewsClick(position: Int) {
        val newsDetailsIntent = NewsDetailsActivity.newIntent(
            this,
            latestNews[position].author,
            latestNews[position].url,
            latestNews[position].description
        )
        startActivity(newsDetailsIntent)
    }


    private fun showProgressBar(isVisible: Boolean) {
        progressBar.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    /**
     * Initialise bottom navigation
     */
    fun initBottomNaviagtion() {
        bottomNavigationView!!.setOnNavigationItemSelectedListener { item ->
            if (item.itemId == R.id.lang_setting) {
                val intent = Intent(this@NewsActivity, Settings::class.java)
                startActivity(intent)
            }
            true
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.searchmenu, menu)
        this.menu = menu
        val manager = getSystemService(SEARCH_SERVICE) as SearchManager
        search = menu.findItem(R.id.search).actionView as SearchView
        search!!.setSearchableInfo(manager.getSearchableInfo(componentName))
        search!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                menu.findItem(R.id.back).setIcon(R.drawable.ic_baseline_arrow_back_24);
                search!!.clearFocus()
                newsArticleViewModel.initSearch(query)
                return true
            }

            override fun onQueryTextChange(query: String): Boolean {
                return true
            }
        })
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.getItemId() == R.id.back) {
            menu?.findItem(R.id.back)?.setIcon(R.drawable.ic_baseline_refresh_24);
            refreshpage();
        }

        if (item.getItemId() == R.id.mic) {
            menu?.findItem(R.id.back)?.setIcon(R.drawable.ic_baseline_arrow_back_24);
            startASR()
        }
        return super.onOptionsItemSelected(item);
    }

    fun refreshpage() {
        val intent = intent
        finish()
        startActivity(intent)
    }

    /**
     * Start ASR and Use Intent for recognition settings.
     *
     */
    fun startASR() {
        search!!.clearFocus()
        val intent = Intent(this, MLAsrCaptureActivity::class.java)
            .putExtra(MLAsrCaptureConstants.LANGUAGE, "en-US")
            .putExtra(MLAsrCaptureConstants.FEATURE, MLAsrCaptureConstants.FEATURE_WORDFLUX)
        startActivityForResult(intent, REQUEST_CODE_ASR)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        var text = ""
        if (requestCode == REQUEST_CODE_ASR) {
            when (resultCode) {
                MLAsrCaptureConstants.ASR_SUCCESS -> if (data != null) {
                    val bundle = data.extras
                    if (bundle!!.containsKey(MLAsrCaptureConstants.ASR_RESULT)) {
                        text = bundle.getString(MLAsrCaptureConstants.ASR_RESULT).toString()
                        if(text!=""){
                        newsArticleViewModel.initSearch(text)
                        }
                    }
                }
                MLAsrCaptureConstants.ASR_FAILURE -> if (data != null) {
                    val bundle = data.extras
                    if (bundle!!.containsKey(MLAsrCaptureConstants.ASR_ERROR_CODE)) {
                        val errorCode = bundle.getInt(MLAsrCaptureConstants.ASR_ERROR_CODE)
                        Log.e("TAG", application.resources.getString(R.string.sigin_failed))
                    }
                    if (bundle.containsKey(MLAsrCaptureConstants.ASR_ERROR_MESSAGE)) {
                        val errorMsg = bundle.getString(MLAsrCaptureConstants.ASR_ERROR_MESSAGE)
                    }
                    if (bundle.containsKey(MLAsrCaptureConstants.ASR_SUB_ERROR_CODE)) {
                        val subErrorCode = bundle.getInt(MLAsrCaptureConstants.ASR_SUB_ERROR_CODE)
                    }
                }
                else -> {
                }
            }
        }
    }
}
