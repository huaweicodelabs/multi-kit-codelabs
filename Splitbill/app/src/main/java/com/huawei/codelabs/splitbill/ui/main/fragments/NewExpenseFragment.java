/*
 * Copyright 2022. Huawei Technologies Co., Ltd. All rights reserved.
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
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.agconnect.cloud.storage.core.StorageReference;
import com.huawei.codelabs.splitbill.R;
import com.huawei.codelabs.splitbill.databinding.FragmentNewExpenseBinding;
import com.huawei.codelabs.splitbill.ui.SplitBillApplication;
import com.huawei.codelabs.splitbill.ui.main.activities.MainActivity;
import com.huawei.codelabs.splitbill.ui.main.adapter.FriendsListAdapter;
import com.huawei.codelabs.splitbill.ui.main.helper.Constants;
import com.huawei.codelabs.splitbill.ui.main.models.Expense;
import com.huawei.codelabs.splitbill.ui.main.models.Friends;
import com.huawei.codelabs.splitbill.ui.main.viewmodels.CloudStorageViewModel;
import com.huawei.codelabs.splitbill.ui.main.viewmodels.ExpenseViewModel;
import com.huawei.codelabs.splitbill.ui.main.viewmodels.FriendsViewModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NewExpenseFragment extends Fragment implements View.OnClickListener{
    private static final String TAG = "NewExpenseFragment";
    public FriendsViewModel friendsViewModel;
    public FriendsListAdapter friendsListAdapter;
    FragmentNewExpenseBinding fragmentNewExpenseBinding;
    private String mParam3;
    private EditText expenseName;
    private EditText expenseDesc;
    private RecyclerView friendsList;
    private ExpenseViewModel expenseViewModel;
    private View view;
    private final String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentNewExpenseBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_new_expense, container, false);
        ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.addExpense));
        view = fragmentNewExpenseBinding.getRoot();
        expenseViewModel = ((MainActivity) getActivity()).createExpenseViewModel(this);
        expenseName = fragmentNewExpenseBinding.addExpenseName;
        expenseDesc = fragmentNewExpenseBinding.addExpenseDescription;
        ActivityCompat.requestPermissions(getActivity(), permissions, 1);
        fragmentNewExpenseBinding.expenseSubmitBtn.setOnClickListener(this);
        fragmentNewExpenseBinding.imgGroupPic.setOnClickListener(this);
        initView();
        spinnerSet();
        return view;
    }

    private void initView() {
        friendsList = fragmentNewExpenseBinding.addFriends;
        friendsViewModel = ((MainActivity) getActivity()).createFriendsViewModel(this);
        friendsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        friendsViewModel.getFriendsLiveData().observe(getActivity(), new Observer<List<Friends>>() {
            @Override
            public void onChanged(List<Friends> friends) {
                List<FriendsListAdapter.FriendsUI> friendsUIList = getFriendsListForUI(friends);
                friendsListAdapter = new FriendsListAdapter(friendsUIList, NewExpenseFragment.this, true);
                friendsList.setAdapter(friendsListAdapter);
                Log.d(TAG, "New Expense fragment recycleView Changed");
            }
        });
    }
    private void spinnerSet() {
        Spinner spinner = fragmentNewExpenseBinding.spinnerStatus;
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.members, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinner.setAdapter(adapter);
    }
    //gallery picture is used to fetch the image from gallery
    private void galleryPicture() {
        Uri filePath = Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath());
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(filePath, "image/*");
        activityResultLauncher.launch(intent);
    }
    //activityResultLauncher is getting the data from gallery and setting up the image in actGroupprofileIv image view.
    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    ImageView imageView = new ImageView(getActivity());
                    imageView.setImageURI(data.getData());
                    fragmentNewExpenseBinding.imgGroupPic.setImageURI(data.getData());
                    imageView.invalidate();
                    BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
                    Bitmap bitmap = drawable.getBitmap();
                    Uri tempUri = getImageUri(getContext(), bitmap);
                    File finalPath = new File(getRealPathFromURI(tempUri));
                    uploadImage(finalPath);
                }
            });

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
        CloudStorageViewModel cloudStorageViewModel = new ViewModelProvider(requireActivity()).get(CloudStorageViewModel.class);
        cloudStorageViewModel.uploadFileLiveData().observe(getActivity(), uri -> {
            addImage(String.valueOf(uri));
        });

        final String randomKey = UUID.randomUUID().toString();
        final String name = randomKey + ".png";
        final StorageReference storageReference = SplitBillApplication.getStorageManagement().getStorageReference(Constants.NEW_EXPENSE_PIC_PATH + name);
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

    private void addExpenseData(String s) {
        Expense expense = new Expense();
        String strExpenseName = expenseName.getText().toString();
        String strExpenseAmount = expenseDesc.getText().toString();
        expense.setId(0);
        expense.setName(strExpenseName);
        expense.setAttachment(s);
        expense.setAmount(Float.parseFloat((strExpenseAmount) + ".00"));
        expense.setPaid_user_id(0);
        expense.setStatus(1);
        expenseViewModel.upsertExpenseData(expense).observe(getActivity(), aBoolean -> {
            if (aBoolean.equals(true)) {
                Toast.makeText(getContext(), getString(R.string.add_expense_success) + " " + strExpenseName, Toast.LENGTH_LONG).show();

                //Navigation.findNavController(fragmentNewExpenseBinding.getRoot()).navigate(R.id.navigation_activity);
            } else {
                Toast.makeText(getContext(), getString(R.string.add_expense_failed), Toast.LENGTH_LONG).show();
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.img_group_pic:
                galleryPicture();
                break;
            case R.id.expense_submit_btn:
                addExpenseData(addImage(mParam3));
                break;
            default:
                break;
        }
    }
}