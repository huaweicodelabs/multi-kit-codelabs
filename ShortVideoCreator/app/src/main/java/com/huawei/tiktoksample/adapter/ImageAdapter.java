/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
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

package com.huawei.tiktoksample.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.huawei.tiktoksample.HomeActivity;
import com.huawei.tiktoksample.R;
import com.huawei.tiktoksample.model.ImageModel;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final Context context;
    private final ArrayList<ImageModel> imageList;
    private final static int IMAGE_LIST = 0;
    private final static int IMAGE_PICKER = 1;

    public ImageAdapter(Context context, ArrayList<ImageModel> imageList) {
        this.context = context;
        this.imageList = imageList;
    }

    @Override
    public  RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_list, parent, false);
            return new ImageListViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return position < 2 ? IMAGE_PICKER : IMAGE_LIST;
    }

    @Override
    public  void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
            final ImageListViewHolder viewHolder = (ImageListViewHolder) holder;
            viewHolder.image.setImageBitmap(imageList.get(position).getImage());
            viewHolder.image.setVisibility(View.VISIBLE);
        viewHolder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url= imageList.get(position).getVideoURL();
                Intent myIntent = new Intent(context, HomeActivity.class);
                myIntent.putExtra("VideoUrl", url);
                myIntent.putExtra("VideoName", imageList.get(position).getVideoName());
                myIntent.putExtra("UserName", imageList.get(position).getUserName());
                myIntent.putExtra("UserMobile", imageList.get(position).getUserMobile());
                myIntent.putExtra("UserEmail", imageList.get(position).getUserEmail());
                myIntent.putExtra("NoOfComments", imageList.get(position).getNoOfComments());
                myIntent.putExtra("NoOfLikes", imageList.get(position).getNoOfLike());
                context.startActivity(myIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }
    public class ImageListViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        ImageView videoPreviewPlayButton;

        public ImageListViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image);
            videoPreviewPlayButton=itemView.findViewById(R.id.videoPreviewPlayButton);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
    }

    public interface OnItemClickListener {
        void onItemClick(int position, View v);
    }
}