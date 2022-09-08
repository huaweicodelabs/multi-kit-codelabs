/* Copyright 2022. Huawei Technologies Co., Ltd. All rights reserved.
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
     http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.huawei.codelabs.splitbill.ui.main.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.agconnect.cloud.storage.core.StorageReference;
import com.huawei.codelabs.splitbill.R;
import com.huawei.codelabs.splitbill.databinding.CustomAddFriendBinding;
import com.huawei.codelabs.splitbill.databinding.FragmentCreateGroupBinding;
import com.huawei.codelabs.splitbill.ui.SplitBillApplication;
import com.huawei.codelabs.splitbill.ui.main.activities.MainActivity;
import com.huawei.codelabs.splitbill.ui.main.adapter.FriendsListAdapter;
import com.huawei.codelabs.splitbill.ui.main.helper.Constants;
import com.huawei.codelabs.splitbill.ui.main.models.Friends;
import com.huawei.codelabs.splitbill.ui.main.models.Group;
import com.huawei.codelabs.splitbill.ui.main.viewmodels.CloudStorageViewModel;
import com.huawei.codelabs.splitbill.ui.main.viewmodels.FriendsViewModel;
import com.huawei.codelabs.splitbill.ui.main.viewmodels.GroupViewModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CreateGroupFragment extends Fragment implements View.OnClickListener {
    public static final String TAG = "CreateGroupFragment";
    public FriendsViewModel friendsViewModel;
    public FriendsListAdapter friendsListAdapter;
    ProgressBar progressBar;
    boolean progressStats = true;
    private String mParam1 = "";
    private String mParam2 = "";
    private String mParam3;
    private CloudStorageViewModel cloudStorageViewModel;
    private Spinner spinner;
    private EditText groupName;
    private EditText groupDesc;
    private View mView;
    private RecyclerView friendsList;
    private FragmentCreateGroupBinding fragmentCreateGroupBinding;
    //activityResultLauncher is getting the data from gallery and setting up the image in actGroupprofileIv image view.
    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    ImageView imageView = new ImageView(getActivity());
                    imageView.setImageURI(data.getData());
                    fragmentCreateGroupBinding.actGroupprofileIv.setImageURI(data.getData());
                    imageView.invalidate();
                    BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
                    Bitmap bitmap = drawable.getBitmap();
                    Uri tempUri = getImageUri(getContext(), bitmap);
                    File finalPath = new File(getRealPathFromURI(tempUri));
                    uploadImage(finalPath);
                }
            });
    private CustomAddFriendBinding customAddFriendBinding;
    private GroupViewModel groupViewModel;
    private final String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentCreateGroupBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_create_group, container, false);
        mView = fragmentCreateGroupBinding.getRoot();
        groupViewModel = (GroupViewModel) ((MainActivity) getActivity()).createViewModel(this);
        ActivityCompat.requestPermissions(getActivity(), permissions, 1);
        initView();
        spinnerSet();
        setOnClickListener();
        progressBar = fragmentCreateGroupBinding.progressCreateGroupFragment;
        progressBar.setVisibility(View.INVISIBLE);
        groupName = fragmentCreateGroupBinding.actGroupNameRt;
        groupDesc = fragmentCreateGroupBinding.actGroupdecNameRt;
        return mView;
    }

    private void setOnClickListener() {
        fragmentCreateGroupBinding.imgGroupPicture.setOnClickListener(this);
        fragmentCreateGroupBinding.addingNewFriend.setOnClickListener(this);
        fragmentCreateGroupBinding.actSubmitBtn.setOnClickListener(this);
    }

    private void spinnerSet() {
        spinner = fragmentCreateGroupBinding.spinnerStatus;
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.languages, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);
    }

    private void initView() {
        friendsList = fragmentCreateGroupBinding.addFriends;
        friendsViewModel = ((MainActivity) getActivity()).createFriendsViewModel(this);
        friendsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        friendsViewModel.getFriendsLiveData().observe(getActivity(), new Observer<List<Friends>>() {
            @Override
            public void onChanged(List<Friends> friends) {
                List<FriendsListAdapter.FriendsUI> friendsUIList = getFriendsListForUI(friends);
                friendsListAdapter = new FriendsListAdapter(friendsUIList, CreateGroupFragment.this, true);
                friendsList.setAdapter(friendsListAdapter);
                Log.d(TAG, "Create group fragment recycleView Changed");
            }
        });
    }

    //gallery picture is used to fetch the image from gallery
    private void galleryPicture() {
        Uri filePath = Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath());
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(filePath, "image/*");
        activityResultLauncher.launch(intent);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        String path = "";
        if (getContext().getContentResolver() != null) {
            Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                path = cursor.getString(idx);
                cursor.close();
            }
        }
        return path;
    }

    public void uploadImage(File path) {
        cloudStorageViewModel = new ViewModelProvider(requireActivity()).get(CloudStorageViewModel.class);
        cloudStorageViewModel.uploadFileLiveData().observe(getActivity(), uri -> addImage(String.valueOf(uri)));

        final String randomKey = UUID.randomUUID().toString();
        final String name = randomKey + ".png";
        final StorageReference storageReference = SplitBillApplication.getStorageManagement().getStorageReference(Constants.GROUP_PIC_PATH + name);
        cloudStorageViewModel.uploadFile(storageReference,
                randomKey + ".png", path);
    }

    private String addImage(String valueOf) {
        mParam3 = valueOf;
        return mParam3;
    }

    private List<FriendsListAdapter.FriendsUI> getFriendsListForUI(List<Friends> friends) {
        List<FriendsListAdapter.FriendsUI> friendsUIList = new ArrayList<>();
        for (Friends friend : friends) {
            FriendsListAdapter.FriendsUI friendsUI = new FriendsListAdapter.FriendsUI();
            friendsUI.setFriendsName(friend.getContact_name());
            friendsUIList.add(friendsUI);
        }
        return friendsUIList;
    }

    private void showAddFriendsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        customAddFriendBinding = CustomAddFriendBinding.inflate(layoutInflater);
        builder.setView(customAddFriendBinding.getRoot());

        builder.setPositiveButton(getString(R.string.submit), (dialogInterface, i) -> addFriendToList());
        builder.setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.dismiss());
        builder.create();
        builder.show();
    }

    private void addFriendToList() {
        mParam1 = customAddFriendBinding.nameText.getText().toString().trim() + "";
        mParam2 = customAddFriendBinding.numberText.getText().toString().trim() + "";
        friendsViewModel.getFriendsId().observe(getActivity(), friendsId -> {
            Friends friends = new Friends();
            friends.setContact_name(mParam1);
            friends.setContact_phone(mParam2);
            friends.setContact_id(friendsId);
            insertFriendsDataInDB(friends);

        });

    }

    private void insertFriendsDataInDB(Friends friends) {
        friendsViewModel.upsertFriendsData(friends).observe(getActivity(), aBoolean -> {
            if (aBoolean.equals(true)) {
                Toast.makeText(getContext(), getString(R.string.add_friend_success) + " " + friends.getContact_name(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), getString(R.string.add_friend_failed), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onResume() {
        ((MainActivity) getActivity()).setActionBarTitle(MainActivity.fragmentName);
        super.onResume();
    }

    private void addGroupData(String s) {
        groupViewModel.getGroupId().observe(getActivity(), new Observer<Integer>() {
            @Override
            public void onChanged(Integer groupId) {
                Group group = new Group();

                String strGroupName = groupName.getText().toString();
                String strStatus = spinner.getSelectedItem().toString();
                group.setName(strGroupName);
                String strGroupDesc = groupDesc.getText().toString();
                group.setDescription(strGroupDesc);
                int status = Constants.STATUS_INACTIVE;
                if (strStatus.equals("Active")) {
                    status = Constants.STATUS_ACTIVE;
                }
                group.setProfile_pic(s);
                group.setStatus(status);
                group.setId(groupId);
                insertGroupDataInDB(group);
            }
        });
    }

    private void insertGroupDataInDB(Group group) {
        groupViewModel.upsertGroupData(group).observe(getActivity(), aBoolean -> {
            if (aBoolean.equals(true)) {
                progressStats = true;
                Toast.makeText(getContext(), getString(R.string.add_group_success) + " " + group.getName(), Toast.LENGTH_LONG).show();
                progress();
            } else {
                Toast.makeText(getContext(), getString(R.string.add_group_failed), Toast.LENGTH_LONG).show();
                progressStats = false;
            }
        });
    }

    private void progress() {
        Handler handler = new Handler();
        progressBar.setVisibility(View.VISIBLE);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (progressStats) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Navigation.findNavController(fragmentCreateGroupBinding.getRoot()).navigate(R.id.navigation_groups);
                } else if (!progressStats) {
                    progressBar.setVisibility(View.INVISIBLE);

                }
            }
        }, 1500);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addingNewFriend:
                showAddFriendsDialog();
                break;
            case R.id.img_group_picture:
                galleryPicture();
                break;
            case R.id.act_submit_btn:
                addGroupData(addImage(mParam3));
                break;
            default:
                break;

        }
    }
}