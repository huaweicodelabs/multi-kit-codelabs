package com.huawei.schooldairy.repository;

import com.huawei.schooldairy.model.TaskItem;

import java.util.List;

public interface CloudDBService {
    List<TaskItem> getTaskItems();
}
