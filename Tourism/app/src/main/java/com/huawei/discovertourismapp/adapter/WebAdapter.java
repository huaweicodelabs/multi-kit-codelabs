/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huawei.discovertourismapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.huawei.discovertourismapp.R;
import com.huawei.discovertourismapp.bean.ListBean;

import java.util.ArrayList;
import java.util.List;

public class WebAdapter extends RecyclerView.Adapter<WebAdapter.WebViewHolder> {
    private List<ListBean> list = new ArrayList<>();
    private Context context;

    OnItemClickListener onItemClickListener;
    public interface OnItemClickListener {
        void click(int position,String type,String image);
    }
    public WebAdapter(Context context, List<ListBean> list,OnItemClickListener onClickListener) {
        this.context = context;
        this.list = list;
        this.onItemClickListener = onClickListener;
    }

    @NonNull
    @Override
    public WebViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_web, parent, false);
        return new WebViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull WebViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        if (list != null && !list.isEmpty()) {
            if (list.get(position).getTitle() != null) {
                if (list.get(position).getClick_url() != null) {
                    SpannableString spannableString = new SpannableString(list.get(position).getTitle());
                    spannableString.setSpan(
                            new URLSpan(list.get(position).getClick_url()),
                            0,
                            list.get(position).getTitle().length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    spannableString.setSpan(
                            new ForegroundColorSpan(Color.BLACK),
                            0,
                            list.get(position).getTitle().length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    holder.tvTitle.setText(spannableString);
                    holder.tvTitle.setMovementMethod(LinkMovementMethod.getInstance());
                } else {
                    holder.tvTitle.setText(list.get(position).getTitle());
                }
            } else {
                holder.tvTitle.setText(R.string.get_title_error);
            }
            if (context != null) {
                Glide.with(context)
                        .load(list.get(position).getUrl())
                        .error(R.drawable.net_error)
                        .dontAnimate()
                        .into(holder.img);
            }
            holder.tvDetail.setOnClickListener(
                    v -> onItemClickListener.click(position,"text",list.get(position).getUrl()));
            holder.img.setOnClickListener(
                    v -> onItemClickListener.click(position,"image",list.get(position).getUrl()));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class WebViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle, tvDetail;
        private ImageView img;

        public WebViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.text_title);
            img = itemView.findViewById(R.id.img);
            tvDetail = itemView.findViewById(R.id.tv_detail);
        }
    }

    public void refresh(List<ListBean> newList) {
        if (list != null && !list.isEmpty()) {
            list.clear();
            if (newList != null && !newList.isEmpty()) {
                list.addAll(newList);
                notifyDataSetChanged();
            }
        }
    }

}
