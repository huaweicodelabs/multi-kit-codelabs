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

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.hms.mlplugin.asr.MLAsrCaptureActivity;
import com.huawei.hms.mlplugin.asr.MLAsrCaptureConstants;
import com.huawei.hms.mlsdk.asr.MLAsrRecognizer;
import com.huawei.hms.smartnewsapp.R;
import com.huawei.hms.smartnewsapp.java.ui.ViewModel.NewsResource;
import com.huawei.hms.smartnewsapp.java.ui.ViewModel.NewsViewModel;
import com.huawei.hms.smartnewsapp.java.ui.ViewModel.ViewModelProviderFactory;
import com.huawei.hms.smartnewsapp.java.data.model.Article;
import com.huawei.hms.smartnewsapp.java.data.model.Newsdata;
import com.huawei.hms.smartnewsapp.java.ui.Adapter.NewsListAdapter;
import com.huawei.hms.smartnewsapp.java.util.Constants;
import com.huawei.hms.smartnewsapp.java.util.NetworkUtil;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import dagger.android.AndroidInjection;
import dagger.android.support.DaggerAppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventListener;
import java.util.List;
import javax.inject.Inject;

/**
 * Activity to display the list of news
 */
public class NewsActivity extends DaggerAppCompatActivity
        implements NewsListAdapter.OnNewsClickListener, EventListener {
    private static final int REQUEST_MICROPHONE = 10;
    private static final int REQUEST_CODE_ASR = 100;
    private static final String TAG = "NewsActivity";

    @Inject
    ViewModelProviderFactory providerFactory;
    private NewsViewModel viewModel;
    private ProgressBar progressBar;
    @Inject
    NetworkUtil networkUtil;
    private RecyclerView recyclerView;
    private List<Article> articles = new ArrayList<>();
    private NewsListAdapter adapter;
    private TextView newsError;
    BottomNavigationView bottomNavigationView;
    MLAsrRecognizer mSpeechRecognizer;
    SearchView search;
    Menu menu;
    private static final String[] ALL_PERMISSION =
            new String[] {Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newslist);
        initViewModel();
        progressBar = findViewById(R.id.progress_bar);
        newsError = findViewById(R.id.news_error);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        mSpeechRecognizer = MLAsrRecognizer.createAsrRecognizer(NewsActivity.this); // Use Intent for recognition settings.
        initBottomNaviagtion();
        getNewsList();
        setupRecyclerView();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            checkPermission();
        }
    }

    /**
     * Check the permissions required by the SDK.
     */
    private void checkPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            ArrayList<String> permissionsList = new ArrayList<>();
            for (String perm : getAllPermission()) {
                if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, perm)) {
                    permissionsList.add(perm);
                }
            }
            if (!permissionsList.isEmpty()) {
                ActivityCompat.requestPermissions(this, permissionsList.toArray(new String[0]), REQUEST_MICROPHONE);
            }
        }
    }

    public static List<String> getAllPermission() {
        return Collections.unmodifiableList(Arrays.asList(ALL_PERMISSION));
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this, providerFactory).get(NewsViewModel.class);
        viewModel.init();
    }

    /**
     * Set up Recycler view for new article
     */
    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);
    }

    /**
     * fetch news based on internet connection status
     */
    private void getNewsList() {
        showProgressBar(true);


        viewModel
                .getNewsList()
                .observe(
                        this,
                        new Observer<NewsResource<Newsdata>>() {
                            @Override
                            public void onChanged(NewsResource<Newsdata> newsNewsResource) {
                                switch (newsNewsResource.status) {
                                    case SUCCESS:
                                        recyclerView.setVisibility(View.VISIBLE);
                                        showProgressBar(false);
                                        if (newsNewsResource.data != null
                                                && newsNewsResource.data.getStatus().equals(Constants.STATUS_OK)) {
                                            newsError.setVisibility(View.GONE);
                                            if (!articles.isEmpty()) {
                                                articles.clear();
                                            }
                                            articles = newsNewsResource.data.getArticle();
                                            if (articles.size() != 0) {
                                                adapter =
                                                        new NewsListAdapter(
                                                                articles, NewsActivity.this, NewsActivity.this);
                                                recyclerView.setAdapter(adapter);
                                                adapter.notifyDataSetChanged();
                                            } else newsError.setVisibility(View.VISIBLE);
                                        } else {
                                            recyclerView.setVisibility(View.GONE);
                                            newsError.setVisibility(View.VISIBLE);
                                        }
                                        break;
                                    case ERROR:
                                        showProgressBar(false);
                                        recyclerView.setVisibility(View.GONE);
                                        newsError.setVisibility(View.VISIBLE);
                                        break;
                                }
                            }
                        });



    }

    private void showProgressBar(boolean isVisible) {
        progressBar.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    /**
     * To navigate to news details page when clicked on news
     */
    @Override
    public void onNewsClick(int position) {
        Intent newsDetailsIntent =
                NewsDetailsActivity.newIntent(
                        this,
                        articles.get(position).getSource().getName(),
                        articles.get(position).getUrl(),
                        articles.get(position).getDescription());
        startActivity(newsDetailsIntent);
    }

    /**
     * Save or delete the selected article from sqlite storage
     */
    @Override
    public void onSaveClick(int position) {
        boolean savedState = viewModel.getArticleSavedState(position);
        if (!savedState) {
            viewModel
                    .saveNewsItem(position)
                    .observe(
                            this,
                            new Observer<Boolean>() {
                                @Override
                                public void onChanged(Boolean isSuccessfullySaved) {
                                    if (isSuccessfullySaved) {
                                        Toast.makeText(getBaseContext(), "News Article Saved", Toast.LENGTH_SHORT)
                                                .show();
                                    } else {
                                        Toast.makeText(
                                                        getBaseContext(),
                                                        "Failed to Save News Article",
                                                        Toast.LENGTH_SHORT)
                                                .show();
                                    }
                                }
                            });
            articles.get(position).setArticleSaved(true);
            adapter.notifyItemChanged(position);
        } else {
            if (viewModel.deleteNewsArticle(articles.get(position))) {
                if (networkUtil.isNetworkConnected()) {
                    articles.get(position).setArticleSaved(false);
                    adapter.notifyItemChanged(position);
                } else {
                    articles.remove(position);
                    adapter.notifyItemRemoved(position);
                }
                if (articles.size() == 0) {
                    newsError.setVisibility(View.VISIBLE);
                }
            } else {
                Toast.makeText(
                                getBaseContext(),
                                "News Article Successfully deleted from local storage",
                                Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    /**
     * Initialise bottom navigation
     */
    public void initBottomNaviagtion() {
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        if (item.getItemId() == R.id.lang_setting) {
                            Intent intent = new Intent(NewsActivity.this, Settings.class);
                            startActivity(intent);
                        }
                        return true;
                    }
                });
    }

    /**
     * Initialise option Menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.searchmenu, menu);
        this.menu = menu;
        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        search = (SearchView) menu.findItem(R.id.search).getActionView();
        search.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
        search.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        search.clearFocus();
                        viewModel.initSearch(query);
                        menu.findItem(R.id.back).setIcon(R.drawable.ic_baseline_arrow_back_24);
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String query) {
                        return true;
                    }
                });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.back) {
            menu.findItem(R.id.back).setIcon(R.drawable.ic_baseline_refresh_24);
            refreshpage();
        }

        if (item.getItemId() == R.id.mic) {
            menu.findItem(R.id.back).setIcon(R.drawable.ic_baseline_arrow_back_24);
            startASR();
        }
        return super.onOptionsItemSelected(item);
    }

    public void refreshpage() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    /**
     * Start ASR and Use Intent for recognition settings.
     *
     */
    public void startASR() {
        search.clearFocus();
        Intent intent =
                new Intent(this, MLAsrCaptureActivity.class)
                        .putExtra(MLAsrCaptureConstants.LANGUAGE, "en-US")
                        .putExtra(MLAsrCaptureConstants.FEATURE, MLAsrCaptureConstants.FEATURE_WORDFLUX);
        startActivityForResult(intent, REQUEST_CODE_ASR);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String text = "";
        if (requestCode == REQUEST_CODE_ASR) {
            switch (resultCode) {
                case MLAsrCaptureConstants.ASR_SUCCESS:
                    if (data != null) {
                        Bundle bundle = data.getExtras();
                        if (bundle.containsKey(MLAsrCaptureConstants.ASR_RESULT)) {
                            text = bundle.getString(MLAsrCaptureConstants.ASR_RESULT);
                            viewModel.initSearch(text);
                        }
                    }
                    break;
                case MLAsrCaptureConstants.ASR_FAILURE:
                    if (data != null) {
                        Bundle bundle = data.getExtras();
                        if (bundle.containsKey(MLAsrCaptureConstants.ASR_ERROR_CODE)) {
                            Log.e(TAG, getApplication().getResources().getString(R.string.asr_errorCode));
                        }
                    }
                default:
                    break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.destroy();
        }
    }
}
