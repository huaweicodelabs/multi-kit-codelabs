/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2022. All rights reserved.
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
package com.huawei.schooldairy.ui.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.huawei.agconnect.auth.AGConnectAuth;
import com.huawei.agconnect.auth.AGConnectUser;
import com.huawei.agconnect.cloud.database.CloudDBZoneQuery;
import com.huawei.agconnect.cloud.database.Text;
import com.huawei.agconnect.cloud.storage.core.AGCStorageManagement;
import com.huawei.agconnect.cloud.storage.core.StorageReference;
import com.huawei.agconnect.cloud.storage.core.UploadTask;
import com.huawei.hmf.tasks.Task;
import com.huawei.schooldairy.databinding.ActivityTaskDetailBinding;
import com.huawei.schooldairy.model.CloudDBZoneWrapper;
import com.huawei.schooldairy.model.TaskItem;
import com.huawei.schooldairy.ui.adapters.AttachmentAdapter;
import com.huawei.schooldairy.ui.listeners.UiTaskCallBack;
import com.huawei.schooldairy.userutils.Constants;
import com.huawei.schooldairy.userutils.PrefUtil;
import com.huawei.schooldairy.userutils.UserUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Activity for handle the tasks, upload task images,submission and validation.
 * @author: Huawei
 * @since: 25-05-2021
 */
public class TaskDetailActivity extends AbstractBaseActivity implements UiTaskCallBack {

    private static final int GETTASK = 100;
    private static final int VALIDATE = 200;
    private static final int ATTACHMENT = 300;
    private static final int SUBMIT = 400;

    private AGConnectUser user;
    private CloudDBZoneWrapper mCloudDBZoneWrapper;
    private Handler mHandler;
    private String taskId = "", groupID = "";
    ActivityTaskDetailBinding binding;
    private int userType;

    //private Bitmap bitmap;
    private Uri imageUri;
    private static final int PICKFILE_REQUEST_CODE = 11111;
    private static final String TAG = TaskDetailActivity.class.getSimpleName();
    private AGCStorageManagement mAGCStorageManagement = AGCStorageManagement.getInstance();

    private String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };
    private ArrayList<String> attachmentList = new ArrayList<>();
    private AttachmentAdapter attachmentAdapter;
    private TaskItem taskItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        showBackArrow();

        if (PrefUtil.getInstance(TaskDetailActivity.this).getBool("IS_MAPPED")) {
            userType = PrefUtil.getInstance(TaskDetailActivity.this).getInt("USER_TYPE");
        }

        ActivityCompat.requestPermissions(this, permissions, 1);
        showProgressDialog("Loading...");

        attachmentAdapter = new AttachmentAdapter(TaskDetailActivity.this, attachmentList);
        binding.rcAttachmentList.setLayoutManager(new GridLayoutManager(this, 3));
        binding.rcAttachmentList.setAdapter(attachmentAdapter);

        if (getIntent().hasExtra("TaskId")) {
            taskId = getIntent().getStringExtra("TaskId");
            groupID = getIntent().getStringExtra("GroupID");
            initCloudDB();
        }
    }

    /**
     * Initialize cloud db and initiate the DB operation for getting TaskList
     */
    private void initCloudDB() {
        user = AGConnectAuth.getInstance().getCurrentUser();
        mCloudDBZoneWrapper = new CloudDBZoneWrapper();
        mHandler = new Handler(Looper.getMainLooper());
        mHandler.post(() -> {
            if (null != AGConnectAuth.getInstance().getCurrentUser()) {
                mCloudDBZoneWrapper.addTaskCallBacks(TaskDetailActivity.this);
                mCloudDBZoneWrapper.createObjectType();
                mCloudDBZoneWrapper.openCloudDBZoneV2(mCloudDBZone -> {
                    getSingleTask(taskId);
                });
            }
        });
    }


    /**
     * Call DB wrapper class method for getting the single task detail
     */
    private void getSingleTask(String taskId) {
        new Handler(Looper.getMainLooper()).post(() -> {
            mCloudDBZoneWrapper.queryTasks(CloudDBZoneQuery.where(TaskItem.class).equalTo("TaskID", taskId), GETTASK);
        });
    }


    /**
     * Get the comma separated URL list and convert into ArrayList
     * and update into the Adapter to list the attachments
     */
    private void setAttachmentsList(String item) {
        if (item == null)
            return;
        String[] urlArray = item.split(", ");
        for (int i = 0; i < urlArray.length; i++) {
            if (!urlArray[i].isEmpty() && !attachmentList.contains(urlArray[i]))
                attachmentList.add(urlArray[i]);
        }
        attachmentAdapter.updateList(attachmentList);
    }

    /**
     * Display the task details such as name, description, status, due date and attachments list
     * Display the buttons based on the user type
     */
    private void displayTaskDetail(TaskItem task) {
        taskItem = task;
        binding.txtTaskName.setText(taskItem.getTaskName());
        binding.txtTaskDesc.setText(taskItem.getTaskDescription());
        String dateString = UserUtil.dateToString(taskItem.getDueDate());
        binding.txtTaskDueDate.setText(UserUtil.utcToLocalString(dateString));
        binding.txtTaskStatus.setText(Constants.statusArray[taskItem.getStatus()]);
        binding.txtTaskStatus.setTextColor(Color.parseColor(Constants.statusColorArray[taskItem.getStatus()]));
        String ulrList = (taskItem.getAttachmentUrl() == null) ? "" : taskItem.getAttachmentUrl().get();
        setAttachmentsList(ulrList);

        binding.btnValidateTask.setVisibility(View.GONE);
        binding.btnSubmitTask.setVisibility(View.GONE);
        binding.btnUpload.setVisibility(View.GONE);

        if (userType == Constants.USER_TEACHER) {
            binding.btnValidateTask.setVisibility(View.VISIBLE);
            binding.btnValidateTask.setOnClickListener(v -> {
                if (validatePreValidation(taskItem)) {
                    updateValidateStatus(taskItem);
                }
            });
        } else if (userType == Constants.USER_STUDENT) {
            binding.btnSubmitTask.setVisibility(View.VISIBLE);
            binding.btnSubmitTask.setOnClickListener(v -> {
                if (submitPreValidation(taskItem)) {
                    updateSubmissionStatus(taskItem);
                }
            });
            binding.btnUpload.setVisibility((taskItem.getStatus() == Constants.STATUS_NEW) ? View.VISIBLE : View.GONE);
            binding.btnUpload.setOnClickListener(view -> uploadFile(view));
        }
    }

    /**
     * Validate the Task data whether this task can be evaluate by teacher user
     */
    private boolean validatePreValidation(TaskItem taskParam) {
        if (taskParam.getStatus() == Constants.STATUS_NEW) {
            showToast("Not submitted, Cannot evaluate");
            return false;
        } else if (taskParam.getStatus() == Constants.STATUS_EVALUATED) {
            showToast("Task already evaluated");
            return false;
        } else if (taskParam.getStatus() == Constants.STATUS_CLOSED) {
            showToast("Task Closed, Cannot evaluate");
            return false;
        } else if (taskParam.getAttachmentUrl() == null || taskParam.getAttachmentUrl().get().isEmpty()) {
            showToast("No Attachment, Cannot evaluate");
            return false;
        } else {
            return true;
        }
    }

    /**
     * Validate the Task data whether this task can be submit by student user
     */
    private boolean submitPreValidation(TaskItem taskParam) {
        if (taskParam.getStatus() == Constants.STATUS_SUBMITTED) {
            showToast("Task Already submitted");
            return false;
        } else if (taskParam.getStatus() == Constants.STATUS_EVALUATED) {
            showToast("Task evaluated, Cannot Submit");
            return false;
        } else if (taskParam.getStatus() == Constants.STATUS_CLOSED) {
            showToast("Task Closed, Cannot Submit");
            return false;
        } else if (taskParam.getAttachmentUrl() == null || taskParam.getAttachmentUrl().get().isEmpty()) {
            showToast("No Attachment, Cannot submit");
            return false;
        } else {
            return true;
        }
    }

    /**
     * Update the status of the task when the Teacher evaluate the task
     * by calling the DB wrapper class method upsertTaskItem
     */
    private void updateValidateStatus(TaskItem taskParam) {
        showProgressDialog("Updating Task status..");
        taskParam.setStatus(Constants.STATUS_EVALUATED);
        new Handler(Looper.getMainLooper()).post(() -> {
            mCloudDBZoneWrapper.upsertTaskItem(taskParam, VALIDATE);
        });
    }

    /**
     * Update the attachment URL in the task while student add the attachment
     * by calling the DB wrapper class method upsertTaskItem
     */
    private void updateAttachURL(String uploadUrL) {
        showProgressDialog("Updating your attachments..");

        String str = taskItem.getAttachmentUrl() == null ? "" : taskItem.getAttachmentUrl().get();
        str += (str.isEmpty()) ? uploadUrL : ", " + uploadUrL;
        taskItem.setAttachmentUrl(new Text(str)); // Attachment URL String here(Multiple URL with Comma Separated)

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            mCloudDBZoneWrapper.upsertTaskItem(taskItem, ATTACHMENT);
        }, 500);
    }

    /**
     * Update the status of the task when the Student submit the task
     * by calling the DB wrapper class method upsertTaskItem
     */
    private void updateSubmissionStatus(TaskItem task_Item) {
        showProgressDialog("Updating your attachments..");
        task_Item.setStatus(Constants.STATUS_SUBMITTED);
        new Handler(Looper.getMainLooper()).post(() -> {
            mCloudDBZoneWrapper.upsertTaskItem(task_Item, SUBMIT);
            Toast.makeText(this, "Url Updated successfully", Toast.LENGTH_SHORT).show();
        });
    }


    /**
     * Initialize the cloud storage instance
     */
    private void initAGCStorageManagement() {
        if (mAGCStorageManagement == null) {
            mAGCStorageManagement = AGCStorageManagement.getInstance();
        }
    }

    /**
     * Check the cloud storage initiated and
     * call the image picker method
     */
    public void uploadFile(View view) {
        if (mAGCStorageManagement == null) {
            initAGCStorageManagement();
        }
        pickImageFromGallery();
    }

    /**
     * Initiate image picker from the device gallery
     */
    private void pickImageFromGallery() {
        Uri filePath = Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath());
        //Uri filePath = Uri.parse("/storage/");
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(filePath, "image/*");
        startActivityForResult(intent, PICKFILE_REQUEST_CODE);
    }

    /**
     * Get the image picker result and convert as bitmap and
     * send it to Cloud storage upload method
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICKFILE_REQUEST_CODE && resultCode == RESULT_OK && null != data) {
            imageUri = data.getData();
            ImageView imageView = new ImageView(TaskDetailActivity.this);
            imageView.setImageURI(imageUri);
            imageView.invalidate();
            BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
            uploadImageToCloud(drawable.getBitmap());
        }
    }

    /**
     * Create random string for image name and
     * start the image upload async task
     */
    private void uploadImageToCloud(Bitmap bitmap) {
        showProgressDialog("Uploading... ");
        final String randomKey = UUID.randomUUID().toString();
        FileFromBitmap fileFromBitmap = new FileFromBitmap(bitmap, randomKey, TaskDetailActivity.this);
        fileFromBitmap.execute();
    }


    /**
     * Image upload AsyncTask class
     * doInBackground - Get the file by path from gallery
     * onPostExecute - Upload it to Cloud Storage
     * Get the uploaded file URL for display/view
     */
    class FileFromBitmap extends AsyncTask<Void, Integer, File> {
        Context context;
        Bitmap bitmap;
        String fileName;

        public FileFromBitmap(Bitmap bitmap, String fileName, Context context) {
            this.bitmap = bitmap;
            this.context = context;
            this.fileName = fileName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected File doInBackground(Void... voids) {
            File fileBackGround = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), fileName);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            byte[] bitmapdata = bos.toByteArray();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(fileBackGround);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                fos.write(bitmapdata);
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return fileBackGround;
        }

        @Override
        protected void onPostExecute(File file) {
            if (!file.exists()) {
                return;
            }
            StorageReference storageReference = mAGCStorageManagement.getStorageReference(file.getPath());
            //Upload file and get URL on success
            UploadTask uploadTask = storageReference.putFile(file);
            Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return storageReference.getDownloadUrl();
            });
            // Send URL on complete upload
            urlTask.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    System.out.println("Upload " + downloadUri);
                    hideDialog();
                    if (downloadUri != null) {
                        String photoStringLink = downloadUri.toString(); //Download URI is stored here
                        addImageInRecyclerView(photoStringLink);
                    }
                }
            });
            uploadTask.addOnSuccessListener(uploadResult -> hideDialog())
                    .addOnFailureListener(e -> hideDialog());
        }
    }

    /**
     * Get the image URL from async task and add into attachment RecyclerView
     * @param imageLink
     */
    private void addImageInRecyclerView(String imageLink) {
        System.out.println("Upload " + imageLink);
        if (!attachmentList.contains(imageLink)) {
            attachmentList.add(imageLink);
            attachmentAdapter.updateList(attachmentList);
            updateAttachURL(imageLink);
        }
    }

    /**
     * Listener method of DB operation
     * onResult method of retrieve the TaskItem list
     * @param taskItemList
     * @param tag
     */
    @Override
    public void onAddOrQuery(List<TaskItem> taskItemList, int tag) {
        // On Query Result Process
        if (taskItemList.size() > 0) {
            displayTaskDetail(taskItemList.get(0));
        } else {
            showToast("Invalid Task Id");
        }
        hideDialog();
    }

    /**
     * Listener method of DB operation
     * onError method of retrieve the TaskItem list
     * @param errorMessage
     */
    @Override
    public void updateUiOnError(String errorMessage) {
        hideDialog();
        showToast(errorMessage);
    }

    /**
     * Listener method of DB operation
     * onRefresh method of retrieve the TaskItem list
     * Display message based on the task status change
     * @param tag
     */
    @Override
    public void onRefresh(int tag) {
        hideDialog();
        if (tag == VALIDATE || tag == SUBMIT) {
            String msg = tag == VALIDATE ? "Task Validated." : "Task Submitted.";
            showAlertDialog(msg, () -> {
                HomeActivity.NEED_UPDATE = true;
                finish();
            });
        } else if (tag == ATTACHMENT) {
            showToast("File uploaded Successfully");
        }
    }
}