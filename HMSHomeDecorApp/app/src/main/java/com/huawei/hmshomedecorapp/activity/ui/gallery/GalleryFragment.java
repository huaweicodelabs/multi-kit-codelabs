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

package com.huawei.hmshomedecorapp.activity.ui.gallery;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.huawei.hmshomedecorapp.R;
import com.huawei.hmshomedecorapp.activity.MainActivityWithDrawer;
import com.huawei.hmshomedecorapp.activity.TryProduct;
import com.huawei.hmshomedecorapp.databinding.FragmentGalleryBinding;
import com.huawei.hmshomedecorapp.utils.Constants;
import com.huawei.hmshomedecorapp.utils.SharedPreferenceUtilClass;

public class GalleryFragment extends Fragment {
    private FragmentGalleryBinding binding;
    Bundle bundle;
    SharedPreferenceUtilClass sharedPreferenceUtilClass;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        GalleryViewModel galleryViewModel =
                new ViewModelProvider(this).get(GalleryViewModel.class);

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        sharedPreferenceUtilClass = SharedPreferenceUtilClass.getInstance(getActivity());
        bundle = getArguments();
        if(bundle != null) {
            binding.myImg.setImageResource(bundle.getInt("Item Image", android.R.drawable.picture_frame));
            binding.addToCart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(sharedPreferenceUtilClass.getData(Constants.ADD_TO_CART_KEY).equalsIgnoreCase("")) {
                        sharedPreferenceUtilClass.saveData(Constants.ADD_TO_CART_KEY,"1");
                        String cartValueInString = sharedPreferenceUtilClass.getData(Constants.ADD_TO_CART_KEY);
                        ((MainActivityWithDrawer)getActivity()).setCount(getActivity(),String.valueOf(cartValueInString));
                    }
                    else {
                        String cartValueInString = sharedPreferenceUtilClass.getData(Constants.ADD_TO_CART_KEY);
                        int cartValueInInt = Integer.parseInt(cartValueInString);
                        cartValueInInt++;
                        sharedPreferenceUtilClass.saveData(Constants.ADD_TO_CART_KEY,String.valueOf(cartValueInInt));
                        ((MainActivityWithDrawer)getActivity()).setCount(getActivity(),String.valueOf(cartValueInInt));
                    }
                }
            });

            binding.tryThreeDimensional.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity() , TryProduct.class);
                    intent.putExtra("ThreeDFileName",bundle.getString("Item ThreeDFile"));
                    startActivity(intent);
                }
            });

            binding.itemTitle.setText(bundle.getString(getString(R.string.item_key), getString(R.string.item_default))
            +System.lineSeparator()+getString(R.string.dimension_lable)+System.lineSeparator()+getString(R.string.dimension_values));
        }
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}