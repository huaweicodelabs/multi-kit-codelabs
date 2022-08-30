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

package com.huawei.hmshomedecorapp.activity.ui.furniture;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.hmshomedecorapp.R;
import com.huawei.hmshomedecorapp.adapter.ItemAdapter;
import com.huawei.hmshomedecorapp.model.ItemModel;

import java.util.ArrayList;
import java.util.List;

public class FurnitureFragment extends Fragment {

    public static final String ARG_OBJECT = "object";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.furniture_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        List<ItemModel> itemModels = new ArrayList<ItemModel>();
        itemModels.add(new ItemModel(R.drawable.chair,"Chair"));
        itemModels.add(new ItemModel(R.drawable.garden_chair, "Garden Chair"));
        itemModels.add(new ItemModel(R.drawable.room_chair, "Room Chair"));
        itemModels.add(new ItemModel(R.drawable.office_chair,"Office Chair"));
        itemModels.add(new ItemModel(R.drawable.office_table, "Office Table"));
        ItemAdapter itemAdapter = new ItemAdapter(getActivity(),itemModels);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setAdapter(itemAdapter);
        recyclerView.setHasFixedSize(true);
    }
}
