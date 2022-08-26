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

package com.huawei.tiktoksample.ui;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.VideoView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.huawei.agconnect.cloud.database.CloudDBZoneObjectList;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.tiktoksample.HomeActivity;
import com.huawei.tiktoksample.R;
import com.huawei.tiktoksample.adapter.CommentAdapter;
import com.huawei.tiktoksample.adapter.VideosAdapter;
import com.huawei.tiktoksample.db.clouddb.VideoComments;
import com.huawei.tiktoksample.db.clouddb.VideoUpload;
import com.huawei.tiktoksample.db.dao.CloudDBHelper;
import com.huawei.tiktoksample.util.AppLog;
import com.huawei.tiktoksample.util.OnApiError;
import com.huawei.tiktoksample.viewmodel.TikTokSampleViewModel;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements View.OnClickListener {
    private ViewPager2 videosViewPager;
    private TikTokSampleViewModel tikTokSampleViewModel;
    public static List<VideoUpload> videoInfoList = new ArrayList<>();
    private View view;
    private static final String MY_PREFS_NAME = "MyPrefsFile";
    private List<VideoComments> commentList = new ArrayList<>();
    private RecyclerView rvComment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.home_list_item, container, false);
        tikTokSampleViewModel = new ViewModelProvider(requireActivity()).get(TikTokSampleViewModel.class);

        getVideoList();
        videosViewPager = view.findViewById(R.id.viewPagerVideos);
        final SwipeRefreshLayout pullToRefresh = view.findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getVideoList();
                pullToRefresh.setRefreshing(false);
            }
        });
        VideoView videoView = view.findViewById(R.id.videoView);

        tikTokSampleViewModel.insertCommentForVideo().observe(requireActivity(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {

            }
        });

        tikTokSampleViewModel.updateLikeForVideo().observe(requireActivity(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {

            }
        });


        return view;
    }

    public void getVideoList() {
        CloudDBHelper.getInstance().openDb((isConnected, cloudDBZone) -> {
            if (cloudDBZone == null) {
                AppLog.logE("TAG", "CloudDBZone is null, try re-open it");
                return;
            }
            Task<CloudDBZoneSnapshot<VideoUpload>> queryTask = cloudDBZone.executeQuery(
                    CloudDBZoneQuery.where(VideoUpload.class),
                    CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
            queryTask.addOnSuccessListener(snapshot -> processQueryResult(snapshot))
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            AppLog.logE(getClass().getName(), "Error in getting data from user table");
                        }
                    });

        });
    }
    public void getComments(Long videoId) {
        CloudDBHelper.getInstance().openDb((isConnected, cloudDBZone) -> {
            if (cloudDBZone == null) {
                AppLog.logE("TAG", "CloudDBZone is null, try re-open it");
                return;
            }
            Task<CloudDBZoneSnapshot<VideoComments>> queryTask = cloudDBZone.executeQuery(
                    CloudDBZoneQuery.where(VideoComments.class).equalTo("video_id",videoId),
                    CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
            queryTask.addOnSuccessListener(snapshot -> processQueryCommentsResult(snapshot))
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            AppLog.logE(getClass().getName(), "Error in getting data from user table");
                        }
                    });

        });
    }
    private void processQueryCommentsResult(CloudDBZoneSnapshot<VideoComments> snapshot) {
        CloudDBZoneObjectList<VideoComments> videoInfoCursor = snapshot.getSnapshotObjects();
        commentList = new ArrayList<>();
        try {
            while (videoInfoCursor.hasNext()) {
                VideoComments videoComments = videoInfoCursor.next();
                commentList.add(videoComments);
            }

            CommentAdapter adapter = new CommentAdapter((commentList), getActivity());
            rvComment.setLayoutManager(new LinearLayoutManager(getActivity()));
            rvComment.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        } catch (AGConnectCloudDBException e) {
            AppLog.logE("TAG", "processQueryResult: " + e.getMessage());
        }
        AppLog.logE(getClass().getName(), "We got user data " + videoInfoList.toString());
        snapshot.release();
        Log.e("new test", "new test");
    }
    private void processQueryResult(CloudDBZoneSnapshot<VideoUpload> snapshot) {
        CloudDBZoneObjectList<VideoUpload> videoInfoCursor = snapshot.getSnapshotObjects();
        videoInfoList = new ArrayList<>();
        try {
            while (videoInfoCursor.hasNext()) {
                VideoUpload videoUpload = videoInfoCursor.next();
                videoInfoList.add(videoUpload);
            }
            try {
                if(((HomeActivity) getActivity()).intentValue==1){
                    VideoUpload videoUpload = new VideoUpload();
                    videoUpload.setVideoUploadLink(((HomeActivity) getActivity()).videoUrl);
                    videoUpload.setVideoName(((HomeActivity) getActivity()).videName);
                    videoUpload.setUserEmail(((HomeActivity) getActivity()).userEmail);
                    videoUpload.setUserName(((HomeActivity) getActivity()).userName);
                    videoUpload.setUserPhone(((HomeActivity) getActivity()).userMobile);
                    videoUpload.setVideoNoOfLikes(((HomeActivity) getActivity()).noOfLikes);
                    videoUpload.setVideoNoOfComments(((HomeActivity) getActivity()).noOfComments);
                    videoInfoList.add(0, videoUpload);
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
            videosViewPager.setAdapter(new VideosAdapter(videoInfoList, HomeFragment.this, getActivity()));
            videosViewPager.requestFocusFromTouch();

        } catch (AGConnectCloudDBException e) {
            AppLog.logE("TAG", "processQueryResult: " + e.getMessage());
        }
        AppLog.logE(getClass().getName(), "We got user data " + videoInfoList.toString());
        snapshot.release();
        Log.e("new test", "new test");
    }


    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.videoView) {

        } else if (v.getId() == R.id.myimage) {

        } else if (v.getId() == R.id.comment_video) {

        } else if (v.getId() == R.id.share_video) {

        }
    }

    public void updateVideoLike(VideoUpload videoUpload){
        tikTokSampleViewModel.updateLikeCount(videoUpload);
        VideoComments videoComments = new VideoComments();
        videoComments.setCommentCreateTime(System.currentTimeMillis());
        videoComments.setVideoId(videoUpload.getVideoId());
        videoComments.setCommentShadowFlag(true);
        videoComments.setCommentId(System.currentTimeMillis());
        videoComments.setCommentText("This is test comment");
        videoComments.setUserEmail(videoUpload.getUserEmail());
        videoComments.setUserProfilePic("");

        tikTokSampleViewModel.enterCommentForVideo(videoComments, new OnApiError() {
            @Override
            public void onError(String errorMessage, Throwable e) {

            }
        });
    }

    public void openVideoCommentDilog(Long video_id) {
        getComments(video_id);
        Rect displayRectangle = new Rect();
        Window window = getActivity().getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.CustomAlertDialog);
        ViewGroup viewGroup = view.findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(getActivity()).inflate(R.layout.customview, viewGroup, false);
        dialogView.setMinimumWidth((int)(displayRectangle.width() * 1f));
        dialogView.setMinimumHeight((int)(displayRectangle.height() * 1f));
        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();
        Button buttonOk=(Button) dialogView.findViewById(R.id.buttonOk);
        EditText etComments=(EditText) dialogView.findViewById(R.id.etComments);
        rvComment=(RecyclerView) dialogView.findViewById(R.id.commentList);
        etComments.requestFocus();

        SharedPreferences prefs = getActivity().getSharedPreferences(MY_PREFS_NAME, getActivity().MODE_PRIVATE);

        String image = prefs.getString("profileImageURL", "0");


        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((HomeActivity)getActivity()).callSubmitData(video_id,etComments.getText().toString());
                VideoComments comment= new VideoComments();
                comment.setUserProfilePic(image);
                comment.setCommentText(etComments.getText().toString());
                commentList.add(comment);
                CommentAdapter adapter = new CommentAdapter((commentList),getActivity());
                rvComment.setLayoutManager(new LinearLayoutManager(getActivity()));
                rvComment.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                etComments.setText("");
            }
        });
        alertDialog.show();
    }
}
