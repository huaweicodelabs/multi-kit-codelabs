package com.huawei.hms.smartnewsapp.kotlin.ui.news;

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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.BannerAdSize;
import com.huawei.hms.ads.banner.BannerView;
import com.huawei.hms.smartnewsapp.R;
import com.kotlin.mvvm.repository.model.news.News;

import java.util.List;

/**
 * Recycler adapter for news list
 */
public class NewsListAdapter extends RecyclerView.Adapter<NewsListAdapter.ViewHolder> {
    private List<News> articles;
    private Context context;
    private OnNewsClickListener onNewsClickListener;
    BannerView bottomBannerView;
    RelativeLayout rootView;

    public NewsListAdapter(List<News> articles, Context context, OnNewsClickListener onNewsClickListener) {
        this.articles = articles;
        this.context = context;
        this.onNewsClickListener = onNewsClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.news, parent, false);
        return new ViewHolder(view, onNewsClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holders, int position) {
        News article = articles.get(position);
        holders.bindTo(article);
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public interface OnNewsClickListener {
        void onNewsClick(int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView desc;
        TextView author;
        TextView source;
        ImageView imageView;
        ProgressBar progressBar;

        public ViewHolder(View itemView, final OnNewsClickListener onNewsClickListener) {
            super(itemView);
            itemView.setOnClickListener(view -> {
                if (onNewsClickListener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    onNewsClickListener.onNewsClick(getAdapterPosition());
                }
            });
            title = itemView.findViewById(R.id.title);
            desc = itemView.findViewById(R.id.desc);
            author = itemView.findViewById(R.id.author);
            source = itemView.findViewById(R.id.source);
            imageView = itemView.findViewById(R.id.img);
            progressBar = itemView.findViewById(R.id.prograss_load_photo);

            bottomBannerView = itemView.findViewById(R.id.hw_banner_view);
            rootView = itemView.findViewById(R.id.root_view);

            AdParam adParam = new AdParam.Builder().build();
            bottomBannerView.loadAd(adParam);

            // Call new BannerView(Context context) to create a BannerView class.
            BannerView topBannerView = new BannerView(context);
            topBannerView.setBannerAdSize(BannerAdSize.BANNER_SIZE_360_57);
            topBannerView.loadAd(adParam);
            rootView.addView(topBannerView);
        }

        public void bindTo(News article) {

            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(new ColorDrawable(Color.parseColor("#f9f9fa")))
                    .error(new ColorDrawable(Color.parseColor("#f9f9fa")))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop();

            Glide.with(context)
                    .load(article.getUrlToImage()!=null?article.getUrlToImage():R.drawable.news_icon)
                    .apply(requestOptions)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView);

            title.setText(article.getTitle());
            desc.setText(article.getDescription());
            author.setText(article.getAuthor());
        }

    }
}
