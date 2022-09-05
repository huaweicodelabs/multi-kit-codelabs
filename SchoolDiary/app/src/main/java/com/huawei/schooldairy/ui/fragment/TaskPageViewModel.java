package com.huawei.schooldairy.ui.fragment;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.schooldairy.callbacks.UiTaskCallBack;
import com.huawei.schooldairy.data.CloudDBZoneWrapper;
import com.huawei.schooldairy.model.TaskItem;
import com.huawei.schooldairy.model.UserData;
import com.huawei.schooldairy.userutils.Constants;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TaskPageViewModel extends ViewModel implements UiTaskCallBack {

    private CloudDBZoneWrapper cloudDBZoneWrapper;
    private MutableLiveData<Integer> mIndex = new MutableLiveData<>();
    private LiveData<List<TaskItem>> homePageTaskList = new MutableLiveData<>();
    private LiveData<List<TaskItem>> homePageHistoryList = new MutableLiveData<>();
    private LiveData<List<UserData>> studentsList = new MutableLiveData<>();

    public void setIndex(int index) {
        mIndex.setValue(index);
        getTaskList(index);
    }

    public void setCloudDBZoneWrapper(CloudDBZoneWrapper cloudDBZoneWrapper) {
        this.cloudDBZoneWrapper = cloudDBZoneWrapper;
        this.cloudDBZoneWrapper.addTaskCallBacks(this);
    }

    public LiveData<List<TaskItem>> getTaskList() {
        return homePageTaskList;
    }

    public LiveData<List<TaskItem>> getTaskHistory() {
        return homePageHistoryList;
    }

    public LiveData<List<UserData>> getStudentsList() {
        return studentsList;
    }

    private void getListData(CloudDBZoneQuery<TaskItem> query) {
        cloudDBZoneWrapper.queryTasks(query);
    }

    private void getTaskList(int inputValue) {
        CloudDBZoneQuery<TaskItem> query;
        if (inputValue == Constants.TASK_ITEM) {
            Date date = Calendar.getInstance().getTime();
            query = CloudDBZoneQuery.where(TaskItem.class).greaterThanOrEqualTo("DueDate", date);
            getListData(query);
        } else if (inputValue == Constants.TASK_HISTORY_ITEM){
            Date date = Calendar.getInstance().getTime();
            query = CloudDBZoneQuery.where(TaskItem.class).lessThanOrEqualTo("DueDate", date);
            getListData(query);
        }else{
            query = CloudDBZoneQuery.where(TaskItem.class);
            getListData(query);
        }
    }

    @Override
    public void onAddOrQuery(List<TaskItem> taskItemList) {
        if (mIndex.getValue() == Constants.TASK_ITEM) {
            homePageTaskList.getValue().addAll(taskItemList);
        } else {
            homePageHistoryList.getValue().addAll(taskItemList);
        }
    }

    @Override
    public void onSubscribe(List<TaskItem> taskItemList) {

    }

    @Override
    public void onDelete(List<TaskItem> taskItemList) {

    }

    @Override
    public void updateUiOnError(String errorMessage) {

    }
}