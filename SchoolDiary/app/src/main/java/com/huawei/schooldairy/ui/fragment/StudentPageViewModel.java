package com.huawei.schooldairy.ui.fragment;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.schooldairy.callbacks.UiStudentCallBack;
import com.huawei.schooldairy.data.CloudDBZoneWrapper;
import com.huawei.schooldairy.model.UserData;
import com.huawei.schooldairy.userutils.Constants;

import java.util.List;

public class StudentPageViewModel extends ViewModel implements UiStudentCallBack {

    private CloudDBZoneWrapper cloudDBZoneWrapper;
    private MutableLiveData<Integer> mIndex = new MutableLiveData<>();
    private LiveData<List<UserData>> studentsList = new MutableLiveData<>();


    public void setIndex(int index) {
        mIndex.setValue(index);
        getTaskList(index);
    }

    public void setCloudDBZoneWrapper(CloudDBZoneWrapper cloudDBZoneWrapper) {
        this.cloudDBZoneWrapper = cloudDBZoneWrapper;
        this.cloudDBZoneWrapper.addStudentCallBacks(this);
    }

    public LiveData<List<UserData>> getStudentsList() {
        return studentsList;
    }

    private void getListData(CloudDBZoneQuery<UserData> query) {
        cloudDBZoneWrapper.queryStudents(query);

    }

    private void getTaskList(int inputValue) {
        CloudDBZoneQuery<UserData> query;
        if (inputValue == Constants.TASK_ITEM) {
            query = CloudDBZoneQuery.where(UserData.class).equalTo("UserType", "");
            getListData(query);
        } else{
            query = CloudDBZoneQuery.where(UserData.class);
            getListData(query);
        }
    }


    @Override
    public void onAddOrQuery(List<UserData> studentList) {
        if (mIndex.getValue() == Constants.TASK_HISTORY_ITEM) {
            studentsList.getValue().addAll(studentList);
        }
    }

    @Override
    public void onSubscribe(List<UserData> taskItemList) {

    }

    @Override
    public void onDelete(List<UserData> taskItemList) {

    }

    @Override
    public void updateUiOnError(String errorMessage) {

    }
}