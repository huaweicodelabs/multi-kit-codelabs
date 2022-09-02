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

package com.huawei.hms.knowmyboard.dtse.activity.fragments;

import static com.huawei.hms.knowmyboard.dtse.activity.util.Constants.OPEN_CAMERA;
import static com.huawei.hms.knowmyboard.dtse.activity.util.Constants.OPEN_GALLERY;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.huawei.hms.knowmyboard.dtse.R;
import com.huawei.hms.knowmyboard.dtse.activity.ml.TextRecognitionActivity;
import com.huawei.hms.knowmyboard.dtse.activity.viewmodel.LoginViewModel;
import com.huawei.hms.knowmyboard.dtse.databinding.FragmentMainFragmentBinding;

import java.util.ArrayList;

public class MainFragment extends Fragment {
    static String TAG = "TAG";
    FragmentMainFragmentBinding binding;
    LoginViewModel loginViewModel;

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main_fragment, container, false);
        loginViewModel = new ViewModelProvider(getActivity()).get(LoginViewModel.class);
        binding.setLoginViewModel(loginViewModel);

        binding.buttonScan.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog();
                    }
                });
        loginViewModel
                .getImagePath()
                .observeForever(
                        new Observer<Bitmap>() {
                            @Override
                            public void onChanged(Bitmap bitmap) {
                                try {
                                    binding.imageView.setImageBitmap(bitmap);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Log.e("TAG", "Error : " + e.getMessage());
                                }
                            }
                        });
        loginViewModel
                .getTextRecognized()
                .observeForever(
                        new Observer<ArrayList<String>>() {
                            @Override
                            public void onChanged(ArrayList<String> res) {
                                binding.textLanguage.setText("Language : " + getStringResourceByName(res.get(0)));
                                binding.textDetected.setText("Detected text : " + res.get(1));
                                binding.textTranslated.setText("Translated text : " + res.get(2));
                            }
                        });

        return binding.getRoot();
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        getActivity().startActivityForResult(intent, OPEN_CAMERA);
    }

    private String getStringResourceByName(String aString) {
        try {
            String packageName = getActivity().getPackageName();
            int resId = getResources().getIdentifier(aString, "string", packageName);
            if (resId == 0) {
                return aString;
            } else {
                return getString(resId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return aString;
        }
    }

    private void scan() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        getActivity().startActivityForResult(intent, OPEN_GALLERY);
    }

    public void dialog() {
        final Dialog dialog = new Dialog(getActivity(), R.style.AppTheme);
        dialog.setTitle("Choose");
        dialog.setContentView(R.layout.dialog_pop_up);

        TextView gallery = (TextView) dialog.findViewById(R.id.textView_gallery);
        TextView camera = (TextView) dialog.findViewById(R.id.textView_camera);

        gallery.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        scan();
                    }
                });
        camera.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        openCamera();
                    }
                });
        dialog.show();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_fragment_menu, menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_camera:
                getActivity().startActivityForResult(new Intent(getActivity(), TextRecognitionActivity.class), 1234);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
