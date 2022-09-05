package com.huawei.schooldairy.callbacks;

import android.util.Log;

import com.huawei.schooldairy.model.TaskItem;

import java.util.List;

public interface UiTaskCallBack {
        UiTaskCallBack DEFAULT = new UiTaskCallBack() {
            @Override
            public void onAddOrQuery(List<TaskItem> taskItemList) {
                Log.i("UiCallBack", "Using default onAddOrQuery");
            }

            @Override
            public void onSubscribe(List<TaskItem> taskItemList) {
                Log.i("UiCallBack", "Using default onSubscribe");
            }

            @Override
            public void onDelete(List<TaskItem> taskItemList) {
                Log.i("UiCallBack", "Using default onDelete");
            }

            @Override
            public void updateUiOnError(String errorMessage) {
                Log.i("UiCallBack", "Using default updateUiOnError");
            }
        };

        void onAddOrQuery(List<TaskItem> taskItemList);

        void onSubscribe(List<TaskItem> taskItemList);

        void onDelete(List<TaskItem> taskItemList);

        void updateUiOnError(String errorMessage);
    }