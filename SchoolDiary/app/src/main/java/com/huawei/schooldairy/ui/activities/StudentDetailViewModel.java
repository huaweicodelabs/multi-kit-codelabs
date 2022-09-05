package com.huawei.schooldairy.ui.activities;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.schooldairy.callbacks.UiTaskCallBack;
import com.huawei.schooldairy.data.CloudDBZoneWrapper;
import com.huawei.schooldairy.model.TaskItem;

import java.util.List;

public class StudentDetailViewModel extends ViewModel implements UiTaskCallBack {

    private MutableLiveData<Integer> studentId = new MutableLiveData<>();
    private LiveData<List<TaskItem>> studentTaskStatusList = new MutableLiveData<>();
    private CloudDBZoneWrapper wrapper;

    public void setWrapper(CloudDBZoneWrapper wrapper) {
        this.wrapper = wrapper;
        this.wrapper.addTaskCallBacks(this);
    }

    public void setStudentId(int asmtNo) {
        studentId.setValue(asmtNo);
        getTaskList(studentId.getValue());

    }

    public LiveData<List<TaskItem>> getStudentTaskStatusList() {
        return studentTaskStatusList;
    }


    private void getListData(CloudDBZoneQuery<TaskItem> query) {
        wrapper.queryTasks(query);
    }

    private void getTaskList(int studentId) {
        CloudDBZoneQuery<TaskItem> query;
        query = CloudDBZoneQuery.where(TaskItem.class).equalTo("StudentID", studentId);
        getListData(query);

    }

    @Override
    public void onAddOrQuery(List<TaskItem> taskItemList) {
        studentTaskStatusList.getValue().addAll(taskItemList);
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
