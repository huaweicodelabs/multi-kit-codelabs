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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.math.BigDecimal;

import com.huawei.tiktoksample.HomeActivity;
import com.huawei.tiktoksample.R;
import com.huawei.tiktoksample.db.clouddb.VideoUpload;
import com.huawei.tiktoksample.ui.HomeFragment;

import java.util.List;

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.VideoViewHolder> {
    private List<VideoUpload> mVideoItems;
    static HomeFragment homeFragment;
    Context context;
    public VideosAdapter(List<VideoUpload> videoItems, HomeFragment homeFragment, Context context) {
        mVideoItems = videoItems;
        VideosAdapter.homeFragment = homeFragment;
        this.context=context;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VideoViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_videos_container, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        holder.setVideoData(mVideoItems.get(position),context);
    }

    @Override
    public int getItemCount() {
        return mVideoItems.size();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        VideoView mVideoView;
        TextView titleText;
        TextView descText;
        ProgressBar mProgressBar;
        ImageView myImage;
        ImageView commentVideoImage;
        ImageView shareVideoImage;
        TextView likeCount;
        Boolean clicked = true;
        int count = 1;


        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);

            mVideoView = itemView.findViewById(R.id.videoView);
            titleText = itemView.findViewById(R.id.txtTitle);
            descText = itemView.findViewById(R.id.txtDesc);
            mProgressBar = itemView.findViewById(R.id.progressBar);
            myImage = itemView.findViewById(R.id.myimage);
            commentVideoImage = itemView.findViewById(R.id.comment_video);
            shareVideoImage = itemView.findViewById(R.id.share_video);
            likeCount = itemView.findViewById(R.id.likecount);
        }

        @SuppressLint("SetTextI18n")
        void setVideoData(VideoUpload videoItem, Context context) {
            titleText.setText(videoItem.getVideoName());
            descText.setText(videoItem.getUserPhone());

            mVideoView.setVideoPath(videoItem.getVideoUploadLink());
            likeCount.setText(videoItem.getVideoNoOfLikes()+"");
            mProgressBar.setVisibility(View.VISIBLE);

            myImage.setOnClickListener(v -> {
                final int likeNoCount = videoItem.getVideoNoOfLikes() + 1;
                videoItem.setVideoNoOfLikes(likeNoCount);
                likeCount.setText(likeNoCount+"");
                myImage.setEnabled(false);
                homeFragment.updateVideoLike(videoItem);

            });
            commentVideoImage.setOnClickListener(v -> {
                ((HomeActivity)context).openVideoCommentDilog(videoItem.getUserEmail(),videoItem.getVideoId());
            });
            shareVideoImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, videoItem.getVideoUploadLink()+" Please check this video.");
                    sendIntent.setType("text/plain");

                    Intent shareIntent = Intent.createChooser(sendIntent, null);
                    context.startActivity(shareIntent);
                }
            });
            mVideoView.setOnPreparedListener(mp -> {
                mProgressBar.setVisibility(View.GONE);

                mp.start();

                float videoRatio = mp.getVideoWidth() / (float) mp.getVideoHeight();
                float screenRatio = mVideoView.getWidth() / (float) mVideoView.getHeight();
                float scale = videoRatio / screenRatio;
                BigDecimal floatDecimalOnce = new BigDecimal("1.0");
                if (scale >= floatDecimalOnce.floatValue()) {
                    mVideoView.setScaleX(scale);
                } else {
                    mVideoView.setScaleY(floatDecimalOnce.floatValue() / scale);
                }
            });

            mVideoView.setOnCompletionListener(mp -> mp.start());

            mVideoView.setOnErrorListener((mp, what, extra) -> {
                mp.stop();
                mp.release();
                return false;
            });

            mVideoView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> mProgressBar.setVisibility(View.VISIBLE));

            mVideoView.setOnFocusChangeListener((v, hasFocus) -> mProgressBar.setVisibility(View.VISIBLE));
        }

    }

}
