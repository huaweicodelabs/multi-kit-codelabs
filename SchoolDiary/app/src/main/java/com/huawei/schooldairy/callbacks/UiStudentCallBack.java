package com.huawei.schooldairy.callbacks;

import android.util.Log;

import com.huawei.schooldairy.model.UserData;

import java.util.List;

public interface UiStudentCallBack {
        UiStudentCallBack DEFAULT = new UiStudentCallBack() {
            @Override
            public void onAddOrQuery(List<UserData> taskItemList) {
                Log.i("UiCallBack", "Using default onAddOrQuery");
            }

            @Override
            public void onSubscribe(List<UserData> taskItemList) {
                Log.i("UiCallBack", "Using default onSubscribe");
            }

            @Override
            public void onDelete(List<UserData> taskItemList) {
                Log.i("UiCallBack", "Using default onDelete");
            }

            @Override
            public void updateUiOnError(String errorMessage) {
                Log.i("UiCallBack", "Using default updateUiOnError");
            }
        };

        void onAddOrQuery(List<UserData> taskItemList);

        void onSubscribe(List<UserData> taskItemList);

        void onDelete(List<UserData> taskItemList);

        void updateUiOnError(String errorMessage);
    }