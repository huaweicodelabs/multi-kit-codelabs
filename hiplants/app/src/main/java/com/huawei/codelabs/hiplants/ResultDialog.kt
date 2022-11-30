package com.huawei.codelabs.hiplants

import android.content.Context
import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.huawei.codelabs.hiplants.adapter.WebSearchRecyclerViewAdapter
import com.huawei.codelabs.hiplants.databinding.ResultDialogFragmentBinding
import com.huawei.codelabs.hiplants.model.WebSearchResults

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

class ResultDialog(var resultList: ArrayList<WebSearchResults>) : DialogFragment() {

    private lateinit var binding: ResultDialogFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = ResultDialogFragmentBinding.inflate(inflater, container, false)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireActivity())

        val adapter =
            WebSearchRecyclerViewAdapter(resultList)
        binding.recyclerView.adapter = adapter

        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }


    override fun onAttach(context: Context) {
        super.onAttach(context)


    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
    }


    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onDetach() {
        super.onDetach()
    }


}