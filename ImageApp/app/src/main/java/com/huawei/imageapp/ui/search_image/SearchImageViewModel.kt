/*
 * Copyright 2022. Huawei Technologies Co., Ltd. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.huawei.imageapp.ui.search_image

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huawei.imageapp.core.common.Result
import com.huawei.imageapp.domain.model.ImageClassification
import com.huawei.imageapp.domain.usecase.ClassifyImageUseCase
import com.huawei.imageapp.domain.usecase.SearchImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchImageViewModel @Inject constructor(
    private val searchImageUseCase: SearchImageUseCase,
    private val classifyImageUseCase: ClassifyImageUseCase
) : ViewModel() {

    private val _imageState = MutableStateFlow<ImageState>(ImageState.None)
    val imageState = _imageState.asStateFlow()

    private val _appBarState = MutableStateFlow<SearchImageAppBarState>(SearchImageAppBarState.DefaultAppBar)
    val appBarState = _appBarState.asStateFlow()

    private val _dialogState = MutableStateFlow<SearchImageDialogState>(SearchImageDialogState.Gone)
    val dialogState = _dialogState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    fun searchImage(query:String) = viewModelScope.launch {
        searchImageUseCase(query).collect { result ->
            when(result){
                is Result.Success -> _imageState.value = ImageState.Success(result.data)
                is Result.Error -> _imageState.value = ImageState.Error(result.message)
                is Result.Loading -> _imageState.value = ImageState.Loading
            }
        }
    }

    fun getClassifiedImageResult(bitmap: Bitmap?) = viewModelScope.launch {
        classifyImageUseCase(bitmap).collect { result ->
            when(result){
                is Result.Success -> showClassificationDialog(result.data)
                is Result.Error -> hideClassificationDialog()
                is Result.Loading -> _dialogState.value = SearchImageDialogState.Loading
            }
        }
    }

    fun setSearchQuery(text:String){
        _searchQuery.value = text
    }

    private fun showClassificationDialog(imageClassifications: List<ImageClassification>){
        _dialogState.value = SearchImageDialogState.Visible(imageClassifications)
    }

    fun hideClassificationDialog(){
        _dialogState.value = SearchImageDialogState.Gone
    }

    fun setDefaultAppBar(){
        _appBarState.value = SearchImageAppBarState.DefaultAppBar
    }

    fun setSearchAppBar(){
        _appBarState.value = SearchImageAppBarState.SearchAppBar
    }
}