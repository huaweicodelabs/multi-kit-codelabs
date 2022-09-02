package com.huawei.discovertourismapp.activity;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.discovertourismapp.R;
import com.huawei.discovertourismapp.adapter.WebAdapter;
import com.huawei.discovertourismapp.bean.ListBean;
import com.huawei.discovertourismapp.bean.TokenResponse;
import com.huawei.discovertourismapp.network.NetworkManager;
import com.huawei.discovertourismapp.network.QueryService;
import com.huawei.discovertourismapp.utils.Util;
import com.huawei.hms.searchkit.SearchKitInstance;
import com.huawei.hms.searchkit.bean.AutoSuggestResponse;
import com.huawei.hms.searchkit.bean.BaseSearchResponse;
import com.huawei.hms.searchkit.bean.CommonSearchRequest;
import com.huawei.hms.searchkit.bean.ImageItem;
import com.huawei.hms.searchkit.bean.SpellCheckResponse;
import com.huawei.hms.searchkit.bean.WebSearchRequest;
import com.huawei.hms.searchkit.utils.Language;
import com.huawei.hms.searchkit.utils.Region;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class SearchPlaces extends BaseActivity {
    public static final WebSearchRequest webRequest = new WebSearchRequest();
    public static final CommonSearchRequest commonRequest = new CommonSearchRequest();
    ImageView landmark_image;
    RecyclerView recyclerView;
    byte[] byteArray;
    WebAdapter adapter;
    String landamark_str;
    Util util;
    TextView landMark_text;
    WebAdapter.OnItemClickListener onItemClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_places_layout);
        Bundle bundle = getIntent().getExtras();
        landamark_str = bundle.getString("location_name");
        byteArray = getIntent().getByteArrayExtra("bitmap");
        Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        landmark_image = findViewById(R.id.landmark_image);
        recyclerView = findViewById(R.id.recycler_near_place);
        landMark_text = findViewById(R.id.landMark_text);
        landmark_image.setImageBitmap(bmp);
        util = new Util();
        SearchKitInstance.enableLog();
        SearchKitInstance.init(this, getResources().getString(R.string.app_id));
        landMark_text.setText(landamark_str);

        initRetrofit();
        landmark_image.setOnClickListener(view -> {
            Intent intent = new Intent(SearchPlaces.this, PanoramaDisplay.class);
            intent.putExtra("bitmap", byteArray);
            startActivity(intent);
        });

        onItemClickListener = (position, type, image) -> {

        };
    }

    public void initRetrofit() {
        util.showProgressBar(this);
        ApplicationInfo appInfo = null;
        String baseUrl = "";
        try {
            appInfo = getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            baseUrl = appInfo.metaData.getString("baseUrl");
        } catch (PackageManager.NameNotFoundException e) {
        }
        QueryService service = NetworkManager.getInstance().createService(this, baseUrl);
        service.getRequestToken(
                        "client_credentials",
                        getResources().getString(R.string.app_id),
                        getResources().getString(R.string.client_secret_site))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<TokenResponse>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(TokenResponse tokenResponse) {
                        if (tokenResponse != null) {
                            if (tokenResponse.getAccess_token() != null) {
                                // Log.e(TAG, tokenResponse.getBody().getAccess_token());
                                Log.e(TAG, "token response" + tokenResponse.getAccess_token());

                                SearchKitInstance.getInstance().setInstanceCredential(tokenResponse.getAccess_token());
                            } else {
                                Log.e(TAG, "get responseBody token is null");
                            }
                        } else {
                            Log.e(TAG, "get responseBody is null");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "get token error: " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        getSuggest(landamark_str);
                        getSpellCheck(landamark_str);
                    }
                });
    }

    private static class StaticUtils {
        private static class MyConsumer implements Consumer<Throwable> {
            @Override
            public void accept(Throwable throwable) {
                Log.e(TAG, "do search error: " + throwable.getMessage());
            }
        }

        private static Consumer<Throwable> consumer = new MyConsumer();

        private static class MyObservable implements ObservableOnSubscribe<BaseSearchResponse> {
            @Override
            public void subscribe(ObservableEmitter<BaseSearchResponse> emitter) throws Exception {
                BaseSearchResponse<List<ImageItem>> imageResponse =
                        SearchKitInstance.getInstance().getImageSearcher().search(commonRequest);
                emitter.onNext(imageResponse);
            }
        }

        private static ObservableOnSubscribe<BaseSearchResponse> observable = new MyObservable();
    }

    private void getSuggest(final String query) {
        Observable.create(
                        (ObservableOnSubscribe<List<String>>) emitter -> {
                            AutoSuggestResponse response =
                                    SearchKitInstance.getInstance()
                                            .getSearchHelper()
                                            .suggest(query, Language.ENGLISH);
                            List<String> list = new ArrayList<String>();
                            if (response != null) {
                                if (response.getSuggestions() != null && !response.getSuggestions().isEmpty()) {
                                    for (int i = 0; i < response.getSuggestions().size(); i++) {
                                        list.add(response.getSuggestions().get(i).getName());
                                    }
                                    emitter.onNext(list);
                                }
                            }
                            emitter.onComplete();
                        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        list -> doSearch(landamark_str),
                        StaticUtils.consumer);
    }

    private void doSearch(String query) {
        webRequest.setQ(query);
        webRequest.setLang(Language.ENGLISH);
        webRequest.setSregion(Region.UNITEDKINGDOM);
        webRequest.setPn(1);
        webRequest.setPs(10);
        webRequest.setWithin("www.amazon.com");

        commonRequest.setQ(query);
        commonRequest.setLang(Language.ENGLISH);
        commonRequest.setSregion(Region.UNITEDKINGDOM);
        commonRequest.setPn(1);
        commonRequest.setPs(10);

        Observable.create(StaticUtils.observable)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        baseSearchResponse -> {
                            if (baseSearchResponse != null && baseSearchResponse.getData() != null) {
                                setValue((List<ImageItem>) baseSearchResponse.getData());
                            }
                        },
                        StaticUtils.consumer);
    }

    private void getSpellCheck(final String query) {
        Observable.create(
                        (ObservableOnSubscribe<String>) emitter -> {
                            SpellCheckResponse response =
                                    SearchKitInstance.getInstance()
                                            .getSearchHelper()
                                            .spellCheck(query, Language.ENGLISH);
                            if (response != null && response.getCorrectedQuery() != null) {
                                emitter.onNext(response.getCorrectedQuery());
                            } else {
                                Log.e(TAG, "spell error");
                                emitter.onNext("");
                            }
                        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        s -> {
                            if (!TextUtils.isEmpty(s)) {
                                Toast.makeText(SearchPlaces.this, " " + s, Toast.LENGTH_SHORT).show();
                            }
                            doSearch(query);
                        });
    }

    public void setValue(List<ImageItem> list) {
        List<ListBean> listBeans = new ArrayList<>();
        if (list != null && !list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                ListBean bean = new ListBean();
                if (list.get(i).getTitle() != null) {
                    bean.setTitle(list.get(i).getTitle());
                }
                if (list.get(i).getThumbnail() != null && list.get(i).getThumbnail().getUrl() != null) {
                    bean.setUrl(list.get(i).getThumbnail().getUrl());
                } else {
                    bean.setUrl("");
                }
                if (list.get(i).getClickUrl() != null) {
                    bean.setClick_url(list.get(i).getClickUrl());
                }
                listBeans.add(bean);
            }
            if (adapter != null) {
                adapter.refresh(listBeans);
            } else {
                adapter = new WebAdapter(SearchPlaces.this, listBeans, onItemClickListener);
                if (recyclerView != null) {
                    recyclerView.setLayoutManager(new LinearLayoutManager(SearchPlaces.this));
                    recyclerView.setAdapter(adapter);
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        } else {
            if (recyclerView != null) {
                recyclerView.setVisibility(View.GONE);
            }
        }
        util.stopProgressBar();
    }

}
