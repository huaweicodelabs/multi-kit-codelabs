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


package com.huawei.imageapp.ui.image_detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.huawei.imageapp.core.common.Result
import com.huawei.imageapp.domain.usecase.DetectDescriptionLanguageUseCase
import com.huawei.imageapp.domain.usecase.GetSupportedLanguagesUseCase
import com.huawei.imageapp.domain.usecase.TranslateDescriptionUseCase
import com.huawei.imageapp.ui.image_detail.ImageDetailLanguageDetectionState.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImageDetailViewModel @Inject constructor(
    private val translateDescriptionUseCase: TranslateDescriptionUseCase,
    private val detectDescriptionLanguageUseCase: DetectDescriptionLanguageUseCase,
    private val getSupportedLanguagesUseCase: GetSupportedLanguagesUseCase
) : ViewModel() {

    private val _descriptionLanguageState = MutableStateFlow<ImageDetailLanguageDetectionState>(
        NotDetected
    )
    val descriptionLanguageState = _descriptionLanguageState.asStateFlow()

    private val _descriptionLanguage = MutableStateFlow<String?>(null)
    val descriptionLanguage = _descriptionLanguage.asStateFlow()

    private val _translationState = MutableStateFlow<ImageDetailTranslationState>(
        ImageDetailTranslationState.Passive()
    )
    val translationState = _translationState.asStateFlow()

    private val _textToSpeechState = MutableStateFlow<ImageDetailTextToSpeechState>(
        ImageDetailTextToSpeechState.STOP
    )
    val textToSpeechState = _textToSpeechState.asStateFlow()

    private val _languageState = MutableStateFlow(ImageDetailLanguageState())
    val languageState = _languageState.asStateFlow()

    init {
        getLanguages()
    }

    private fun getLanguages() = viewModelScope.launch {
        getSupportedLanguagesUseCase().collect { result ->
            when (result) {
                is Result.Success -> _languageState.value = _languageState.value.copy(languages = result.data)
                is Result.Error -> Unit
                is Result.Loading -> Unit
            }
        }
    }

    fun getDetectedDescriptionLanguage(description: String?) = viewModelScope.launch {
        detectDescriptionLanguageUseCase(description).collect { result ->
            when (result) {
                is Result.Success -> {
                    _descriptionLanguage.value = result.data
                    _descriptionLanguageState.value = Detected
                }
                is Result.Error -> _descriptionLanguageState.value = NotDetected
                is Result.Loading -> _descriptionLanguageState.value = Loading
            }
        }
    }

    fun getTranslatedDescription(
        description: String?,
        sourceLanguage: String?,
        targetLanguage: String
    ) = viewModelScope.launch {
        translateDescriptionUseCase(description, sourceLanguage,targetLanguage).collect { result ->
            when (result) {
                is Result.Success -> setTranslationActive(result.data,targetLanguage)
                is Result.Error -> setTranslationPassive(language = targetLanguage)
                is Result.Loading -> _translationState.value = ImageDetailTranslationState.Loading
            }
        }
    }

    fun setTextToSpeechState(imageDetailTextToSpeechState: ImageDetailTextToSpeechState) {
        _textToSpeechState.value = imageDetailTextToSpeechState
    }

    fun setTranslationActive(translatedText: String, language: String) {
        _translationState.value = ImageDetailTranslationState.Active(
            translatedText,
            language
        )
    }

    fun setTranslationPassive(translatedText: String? = null, language: String) {
        _translationState.value = ImageDetailTranslationState.Passive(
            translatedText,
            language
        )
    }

    fun changeDropdownVisibility(visibility: Boolean){
        _languageState.value = _languageState.value.copy(
            languageDropdownVisibility = visibility
        )
    }

    fun selectLanguage(language: String){
        _languageState.value = _languageState.value.copy(
            languageDropdownVisibility = false,
            selectedLanguage = language
        )
    }
}