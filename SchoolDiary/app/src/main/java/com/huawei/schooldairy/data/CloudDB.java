//package com.huawei.schooldairy.data;
//
//import static android.content.ContentValues.TAG;
//
//import android.content.Context;
//import android.util.Log;
//
//import com.huawei.agconnect.AGCRoutePolicy;
//import com.huawei.agconnect.AGConnectInstance;
//import com.huawei.agconnect.AGConnectOptionsBuilder;
//import com.huawei.agconnect.auth.AGConnectAuth;
//import com.huawei.agconnect.cloud.database.AGConnectCloudDB;
//import com.huawei.agconnect.cloud.database.CloudDBZone;
//import com.huawei.agconnect.cloud.database.CloudDBZoneConfig;
//import com.huawei.agconnect.cloud.database.CloudDBZoneObjectList;
//import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
//import com.huawei.agconnect.cloud.database.CloudDBZoneSnapshot;
//import com.huawei.agconnect.cloud.database.exceptions.AGConnectCloudDBException;
//import com.huawei.hmf.tasks.OnFailureListener;
//import com.huawei.hmf.tasks.OnSuccessListener;
//import com.huawei.hmf.tasks.Task;
//import com.huawei.schooldairy.callbacks.UiTaskCallBack;
//import com.huawei.schooldairy.model.TaskItem;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class CloudDB {
//
//    private Context context;
//    private static CloudDB instance;
//    private AGConnectCloudDB agcCloudDB;
//    private CloudDBZoneConfig mConfig;
//    private CloudDBZone mCloudDBZone;
//    private UiTaskCallBack mUiTaskCallBack = UiTaskCallBack.DEFAULT;
//
//    public CloudDBZone getmCloudDBZone() {
//        return mCloudDBZone;
//    }
//
//    private CloudDB(Context context) {
//        this.context = context;
//    }
//
//    public static CloudDB getInstance(Context context) {
//        if (instance == null)
//            instance = new CloudDB(context);
//        return instance;
//    }
//
//    public CloudDB initAGConnectCloudDB() {
//        AGConnectCloudDB.initialize(context);
//        return this;
//    }
//
//    public CloudDB createCloudDb() throws AGConnectCloudDBException {
//        AGConnectInstance instance = AGConnectInstance
//                .buildInstance(
//                        new AGConnectOptionsBuilder()
//                                .setRoutePolicy(AGCRoutePolicy.CHINA)
//                                .build(context)
//                );
//        agcCloudDB = AGConnectCloudDB.getInstance(instance, AGConnectAuth.getInstance(instance));
//        agcCloudDB.createObjectType(ObjectTypeInfoHelper.getObjectTypeInfo());
//        return this;
//    }
//
//    public void configCloudDb() {
//        mConfig = new CloudDBZoneConfig("QuickStartDemo",
//                CloudDBZoneConfig.CloudDBZoneSyncProperty.CLOUDDBZONE_CLOUD_CACHE,
//                CloudDBZoneConfig.CloudDBZoneAccessProperty.CLOUDDBZONE_PUBLIC);
//        mConfig.setPersistenceEnabled(true);
//        Task<CloudDBZone> openDBZoneTask = agcCloudDB.openCloudDBZone2(mConfig, true);
//        openDBZoneTask.addOnSuccessListener(new OnSuccessListener<CloudDBZone>() {
//            @Override
//            public void onSuccess(CloudDBZone cloudDBZone) {
//                Log.i(TAG, "open cloudDBZone success");
//                mCloudDBZone = cloudDBZone;
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(Exception e) {
//                Log.w(TAG, "open cloudDBZone failed for " + e.getMessage());
//            }
//        });
//    }
//
//    public void upsertBookInfos(TaskItem taskItem) {
//        if (mCloudDBZone == null) {
//            Log.w(TAG, "CloudDBZone is null, try re-open it");
//            return;
//        }
//        Task<Integer> upsertTask = mCloudDBZone.executeUpsert(taskItem);
//        upsertTask.addOnSuccessListener(new OnSuccessListener<Integer>() {
//            @Override
//            public void onSuccess(Integer cloudDBZoneResult) {
//                Log.i(TAG, "Upsert " + cloudDBZoneResult + " records");
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(Exception e) {
//                mUiTaskCallBack.updateUiOnError("Insert book info failed");
//            }
//        });
//    }
//
//
//    /**
//     * Query all books in storage from cloud side with CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY
//     */
//    public void queryAllTasks() {
//        if (mCloudDBZone == null) {
//            Log.w(TAG, "CloudDBZone is null, try re-open it");
//            return;
//        }
//        Task<CloudDBZoneSnapshot<TaskItem>> queryTask = mCloudDBZone.executeQuery(
//                CloudDBZoneQuery.where(TaskItem.class),
//                CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY);
//        queryTask.addOnSuccessListener(snapshot -> {
//                    processQueryResult(snapshot);
//                })
//                .addOnFailureListener(e -> {
//                    mUiTaskCallBack.updateUiOnError("Query book list from cloud failed");
//                });
//    }
//
//    private void processQueryResult(CloudDBZoneSnapshot<TaskItem> snapshot) {
//        CloudDBZoneObjectList<TaskItem> TasklistCursor = snapshot.getSnapshotObjects();
//        List<TaskItem> taskItemList = new ArrayList<>();
//        try {
//            while (TasklistCursor.hasNext()) {
//                TaskItem TaskItem = TasklistCursor.next();
//                taskItemList.add(TaskItem);
//            }
//        } catch (AGConnectCloudDBException e) {
//            Log.w(TAG, "processQueryResult: " + e.getMessage());
//        } finally {
//            snapshot.release();
//        }
//        mUiTaskCallBack.onAddOrQuery(taskItemList);
//    }
//
//   /* public void viewCloudDbData(){
//        OnSnapshotListener<TaskItem> mSnapshotListener = new OnSnapshotListener<TaskItem>() {
//            @Override
//            public void onSnapshot(CloudDBZoneSnapshot<TaskItem> cloudDBZoneSnapshot, AGConnectCloudDBException e) {
//                if (e != null) {
//                    Log.w(TAG, "onSnapshot: " + e.getMessage());
//                    return;
//                }
//                CloudDBZoneObjectList<TaskItem> snapshotObjects = cloudDBZoneSnapshot.getSnapshotObjects();
//                List<TaskItem> bookInfos = new ArrayList<>();
//                try {
//                    if (snapshotObjects != null) {
//                        while (snapshotObjects.hasNext()) {
//                            TaskItem bookInfo = snapshotObjects.next();
//                            bookInfos.add(bookInfo);
//                            updateTaskIndex(bookInfo);
//                        }
//                    }
//                    mUiCallBack.onSubscribe(bookInfos);
//                } catch (AGConnectCloudDBException snapshotException) {
//                    Log.w(TAG, "onSnapshot:(getObject) " + snapshotException.getMessage());
//                } finally {
//                    cloudDBZoneSnapshot.release();
//                }
//            }
//        };
//
//
//    }
//
//    public void addSubscription() {
//        if (mCloudDBZone == null) {
//            Log.w(TAG, "CloudDBZone is null, try re-open it");
//            return;
//        }
//
//        try {
//            CloudDBZoneQuery<TaskInfo> snapshotQuery = CloudDBZoneQuery.where(TaskInfo.class)
//                    .equalTo(TaskEditFields.SHADOW_FLAG, true);
//            mRegister = mCloudDBZone.subscribeSnapshot(snapshotQuery,
//                    CloudDBZoneQuery.CloudDBZoneQueryPolicy.POLICY_QUERY_FROM_CLOUD_ONLY, mSnapshotListener);
//        } catch (AGConnectCloudDBException e) {
//            Log.w(TAG, "subscribeSnapshot: " + e.getMessage());
//        }
//    }*/
//
//    public void closeCloudDBZone() {
//        try {
//            agcCloudDB.closeCloudDBZone(mCloudDBZone);
//        } catch (AGConnectCloudDBException e) {
//            Log.w(TAG, "closeCloudDBZone: " + e.getMessage());
//        }
//    }
//}
