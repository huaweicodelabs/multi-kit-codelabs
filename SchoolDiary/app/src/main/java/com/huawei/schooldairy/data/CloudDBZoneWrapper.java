package com.huawei.schooldairy.data;

import android.content.Context;
import android.util.Log;

import com.huawei.agconnect.AGConnectInstance;
import com.huawei.agconnect.AGConnectOptionsBuilder;
import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.cloud.database.AGConnectCloudDB;
import com.huawei.agconnect.cloud.database.CloudDBZone;
import com.huawei.agconnect.cloud.database.CloudDBZoneConfig;
import com.huawei.agconnect.cloud.database.CloudDBZoneObjectList;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
import com.huawei.agconnect.cloud.database.ListenerHandler;
import com.huawei.agconnect.cloud.database.OnSnapshotListener;
import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.schooldairy.SchoolDiaryApplication;
import com.huawei.schooldairy.callbacks.UiStudentCallBack;
import com.huawei.schooldairy.callbacks.UiTaskCallBack;
import com.huawei.schooldairy.model.TaskItem;
import com.huawei.schooldairy.model.UserData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CloudDBZoneWrapper {
    private static final String TAG = "CloudDBZoneWrapper";

    private AGConnectCloudDB mCloudDB;

    private CloudDBZone mCloudDBZone;

    private ListenerHandler mRegister;

    private CloudDBZoneConfig mConfig;

    private UiTaskCallBack mUiTaskCallBack = UiTaskCallBack.DEFAULT;
    private UiStudentCallBack mUiStudentCallBack = UiStudentCallBack.DEFAULT;

    /**
     * Mark max id of task info. id is the primary key of {@link TaskItem}, so we must provide an value for it
     * when upserting to database.
     */
    private int mTaskIndex = 0;

    private ReadWriteLock mReadWriteLock = new ReentrantReadWriteLock();

    /**
     * Monitor data change from database. Update task info list if data have changed
     */
    private OnSnapshotListener<TaskItem> mSnapshotListener = new OnSnapshotListener<TaskItem>() {
        @Override
        public void onSnapshot(CloudDBZoneSnapshot<TaskItem> cloudDBZoneSnapshot, AGConnectCloudDBException e) {
            if (e != null) {
                Log.w(TAG, "onSnapshot: " + e.getMessage());
                return;
            }
            CloudDBZoneObjectList<TaskItem> snapshotObjects = cloudDBZoneSnapshot.getSnapshotObjects();
            List<TaskItem> taskItemList = new ArrayList<>();
            try {
                if (snapshotObjects != null) {
                    while (snapshotObjects.hasNext()) {
                        TaskItem taskItem = snapshotObjects.next();
                        taskItemList.add(taskItem);
                        //updateTaskIndex(taskItem);
                    }
                }
                mUiTaskCallBack.onSubscribe(taskItemList);
            } catch (AGConnectCloudDBException snapshotException) {
                Log.w(TAG, "onSnapshot:(getObject) " + snapshotException.getMessage());
            } finally {
                cloudDBZoneSnapshot.release();
            }
        }
    };

    public CloudDBZoneWrapper() {
        SchoolDiaryApplication.setRegionRoutePolicy(
                AGConnectInstance.getInstance().getOptions().getRoutePolicy());
        mCloudDB = AGConnectCloudDB.getInstance();
    }

    public void setStorageLocation(Context context) {
        if (mCloudDBZone != null) {
            closeCloudDBZone();
        }
        AGConnectOptionsBuilder builder = new AGConnectOptionsBuilder()
                .setRoutePolicy(SchoolDiaryApplication.getRegionRoutePolicy());
        AGConnectInstance instance = AGConnectInstance.buildInstance(builder.build(context));
        mCloudDB = AGConnectCloudDB.getInstance(instance, AGConnectAuth.getInstance());
    }

    /**
     * Init AGConnectCloudDB in Application
     * @param context application context
     */
    public static void initAGConnectCloudDB(Context context) {
        AGConnectCloudDB.initialize(context);
    }

    /**
     * Call AGConnectCloudDB.createObjectType to init schema
     */
    public void createObjectType() {
        try {
            mCloudDB.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo());
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, "createObjectType: " + e.getMessage());
        }
    }

    /**
     * Call AGConnectCloudDB.openCloudDBZone to open a cloudDBZone.
     * We set it with cloud cache mode, and data can be store in local storage
     */
    public void openCloudDBZone() {
        mConfig = new CloudDBZoneConfig("QuickStartDemo",
                CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
                CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC);
        mConfig.setPersistenceEnabled(true);
        try {
            mCloudDBZone = mCloudDB.openCloudDBZone(mConfig, true);
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, "openCloudDBZone: " + e.getMessage());
        }
    }

    public void openCloudDBZoneV2() {
        mConfig = new CloudDBZoneConfig("QuickStartDemo",
                CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
                CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC);
        mConfig.setPersistenceEnabled(true);
        Task<CloudDBZone> openDBZoneTask = mCloudDB.openCloudDBZone2(mConfig, true);
        openDBZoneTask.addOnSuccessListener(new OnSuccessListener<CloudDBZone>() {
            @Override
            public void onSuccess(CloudDBZone cloudDBZone) {
                Log.i(TAG, "Open cloudDBZone success");
                mCloudDBZone = cloudDBZone;
                // Add subscription after opening cloudDBZone success
                addSubscription();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Log.w(TAG, "Open cloudDBZone failed for " + e.getMessage());
            }
        });
    }

    /**
     * Call AGConnectCloudDB.closeCloudDBZone
     */
    public void closeCloudDBZone() {
        try {
            mRegister.remove();
            mCloudDB.closeCloudDBZone(mCloudDBZone);
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, "closeCloudDBZone: " + e.getMessage());
        }
    }

    /**
     * Call AGConnectCloudDB.deleteCloudDBZone
     */
    public void deleteCloudDBZone() {
        try {
            mCloudDB.deleteCloudDBZone(mConfig.getCloudDBZoneName());
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, "deleteCloudDBZone: " + e.getMessage());
        }
    }

    /**
     * Add a callback to update task info list
     * @param uiTaskCallBack callback to update task list
     */
    public void addTaskCallBacks(UiTaskCallBack uiTaskCallBack) {
        this.mUiTaskCallBack = uiTaskCallBack;
    }

    public void addStudentCallBacks(UiStudentCallBack uiStudentCallBack) {
        this.mUiStudentCallBack = uiStudentCallBack;
    }

    /**
     * Add mSnapshotListener to monitor data changes from storage
     */
    public void addSubscription() {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }

        try {
            CloudDBZoneQuery<TaskItem> snapshotQuery = CloudDBZoneQuery.where(TaskItem.class);
            //.equalTo(TaskEditFields.SHADOW_FLAG, true);
            mRegister = mCloudDBZone.subscribeSnapshot(snapshotQuery,
                    CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY,
                    mSnapshotListener);
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, "subscribeSnapshot: " + e.getMessage());
        }
    }

    /**
     * Query all tasks in storage from cloud side with CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
     */
    public void queryAllTasks() {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }
        Task<CloudDBZoneSnapshot<TaskItem>> queryTask = mCloudDBZone.executeQuery(
                CloudDBZoneQuery.where(TaskItem.class),
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(new OnSuccessListener<CloudDBZoneSnapshot<TaskItem>>() {
            @Override
            public void onSuccess(CloudDBZoneSnapshot<TaskItem> snapshot) {
                processQueryResult(snapshot);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                mUiTaskCallBack.updateUiOnError("Query task list from cloud failed");
            }
        });
    }

    /**
     * Query tasks with condition
     * @param query query condition
     */
    public void queryTasks(CloudDBZoneQuery<TaskItem> query) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }

        Task<CloudDBZoneSnapshot<TaskItem>> queryTask = mCloudDBZone.executeQuery(query,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(new OnSuccessListener<CloudDBZoneSnapshot<TaskItem>>() {
            @Override
            public void onSuccess(CloudDBZoneSnapshot<TaskItem> snapshot) {
                processQueryResult(snapshot);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                mUiTaskCallBack.updateUiOnError("Query failed");
            }
        });
    }
    public void queryStudents(CloudDBZoneQuery<UserData> query) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }

        Task<CloudDBZoneSnapshot<UserData>> queryTask = mCloudDBZone.executeQuery(query,
                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
        queryTask.addOnSuccessListener(new OnSuccessListener<CloudDBZoneSnapshot<UserData>>() {
            @Override
            public void onSuccess(CloudDBZoneSnapshot<UserData> snapshot) {
                processStudentListResult(snapshot);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                mUiTaskCallBack.updateUiOnError("Query failed");
            }
        });
    }
    private void processStudentListResult(CloudDBZoneSnapshot<UserData> snapshot) {
        CloudDBZoneObjectList<UserData> taskItemCursor = snapshot.getSnapshotObjects();
        List<UserData> studentItemList = new ArrayList<>();
        try {
            while (taskItemCursor.hasNext()) {
                UserData studentItem = taskItemCursor.next();
                studentItemList.add(studentItem);
            }
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, "processQueryResult: " + e.getMessage());
        } finally {
            snapshot.release();
        }
        mUiStudentCallBack.onAddOrQuery(studentItemList);
    }

    private void processQueryResult(CloudDBZoneSnapshot<TaskItem> snapshot) {
        CloudDBZoneObjectList<TaskItem> taskItemCursor = snapshot.getSnapshotObjects();
        List<TaskItem> taskItemList = new ArrayList<>();
        try {
            while (taskItemCursor.hasNext()) {
                TaskItem taskItem = taskItemCursor.next();
                taskItemList.add(taskItem);
            }
        } catch (AGConnectCloudDBException e) {
            Log.w(TAG, "processQueryResult: " + e.getMessage());
        } finally {
            snapshot.release();
        }
        mUiTaskCallBack.onAddOrQuery(taskItemList);
    }

    /**
     * Upsert taskinfo
     * @param taskItem taskinfo added or modified from local
     */
    public void upsertTaskItems(TaskItem taskItem) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }

        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(taskItem);
        upsertTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
            @Override
            public void onSuccess(Integer cloudDBZoneResult) {
                Log.i(TAG, "Upsert " + cloudDBZoneResult + " records");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                mUiTaskCallBack.updateUiOnError("Insert task info failed");
            }
        });
    }

    /**
     * Delete taskinfo
     * @param taskItemList tasks selected by user
     */
    public void deleteTaskItems(List<TaskItem> taskItemList) {
        if (mCloudDBZone == null) {
            Log.w(TAG, "CloudDBZone is null, try re-open it");
            return;
        }

        Task<Integer> deleteTask = mCloudDBZone.executeDelete(taskItemList);
        if (deleteTask.getException() != null) {
            mUiTaskCallBack.updateUiOnError("Delete task info failed");
            return;
        }
        mUiTaskCallBack.onDelete(taskItemList);
    }

    /*private void updateTaskIndex(TaskItem taskItem) {
        try {
            mReadWriteLock.writeLock().lock();
            if (mTaskIndex < taskItem.getTaskID()) {
                mTaskIndex = taskItem.getTaskID();
            }
        } finally {
            mReadWriteLock.writeLock().unlock();
        }
    }*/

    /**
     * Get max id of taskinfos
     * @return max task info id
     */
    public int getTaskIndex() {
        try {
            mReadWriteLock.readLock().lock();
            return mTaskIndex;
        } finally {
            mReadWriteLock.readLock().unlock();
        }
    }

}