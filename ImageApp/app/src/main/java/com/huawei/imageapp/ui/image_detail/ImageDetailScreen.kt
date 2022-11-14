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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.huawei.imageapp.core.designsystem.component.HorizontalSpacer
import com.huawei.imageapp.core.designsystem.component.LoadingScreen
import com.huawei.imageapp.core.designsystem.component.VerticalSpacer
import com.huawei.imageapp.domain.model.ExternalImage
import com.huawei.imageapp.text_to_speech.TextToSpeechCallback
import com.huawei.imageapp.text_to_speech.TextToSpeechManager

@Composable
fun ImageDetailScreen(
    externalImage: ExternalImage?,
    goPreviousScreen: () -> Boolean,
    modifier: Modifier = Modifier,
    viewModel: ImageDetailViewModel = hiltViewModel()
) {
    val languageDetectionState by viewModel.descriptionLanguageState.collectAsState()
    val descriptionLanguage by viewModel.descriptionLanguage.collectAsState()
    val translationState by viewModel.translationState.collectAsState()
    val languageState by viewModel.languageState.collectAsState()
    val textToSpeechState by viewModel.textToSpeechState.collectAsState()
    val textToSpeechManager = remember { TextToSpeechManager() }

    DisposableEffect(key1 = true) {
        viewModel.getDetectedDescriptionLanguage(externalImage?.description)
        textToSpeechManager.createTextToSpeechInstance(object : TextToSpeechCallback {
            override fun onStart() {
                viewModel.setTextToSpeechState(ImageDetailTextToSpeechState.START)
            }

            override fun onResume() {
                viewModel.setTextToSpeechState(ImageDetailTextToSpeechState.RESUME)
            }

            override fun onPause() {
                viewModel.setTextToSpeechState(ImageDetailTextToSpeechState.PAUSE)
            }

            override fun onStop() {
                viewModel.setTextToSpeechState(ImageDetailTextToSpeechState.STOP)
            }
        })
        onDispose {
            textToSpeechManager.stopSpeaking()
            textToSpeechManager.shutDownTextToSpeech()
        }
    }

    ImageDetailContent(
        externalImage = externalImage,
        translationState = translationState,
        languageState = languageState,
        languageDetectionState = languageDetectionState,
        descriptionLanguage = descriptionLanguage,
        textToSpeechState = textToSpeechState,
        getTranslatedDescription = viewModel::getTranslatedDescription,
        setTranslationActive = viewModel::setTranslationActive,
        setTranslationPassive = viewModel::setTranslationPassive,
        changeDropdownVisibility = viewModel::changeDropdownVisibility,
        selectLanguage = viewModel::selectLanguage,
        startSpeaking = textToSpeechManager::startSpeaking,
        resumeSpeaking = textToSpeechManager::resumeSpeaking,
        pauseSpeaking = textToSpeechManager::pauseSpeaking,
        stopSpeaking = textToSpeechManager::stopSpeaking,
        goPreviousScreen = goPreviousScreen,
        modifier = modifier,
    )
}

@Composable
fun ImageDetailContent(
    externalImage: ExternalImage?,
    translationState: ImageDetailTranslationState,
    languageState: ImageDetailLanguageState,
    languageDetectionState: ImageDetailLanguageDetectionState,
    descriptionLanguage: String?,
    textToSpeechState: ImageDetailTextToSpeechState,
    getTranslatedDescription: (description: String, sourceLanguage: String?, targetLanguage: String) -> Unit,
    setTranslationActive: (translatedText: String, language: String) -> Unit,
    setTranslationPassive: (translatedText: String, language: String) -> Unit,
    changeDropdownVisibility: (visibility: Boolean) -> Unit,
    selectLanguage: (language: String) -> Unit,
    startSpeaking: (text: String) -> Unit,
    resumeSpeaking: () -> Unit,
    pauseSpeaking: () -> Unit,
    stopSpeaking: () -> Unit,
    goPreviousScreen: () -> Boolean,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        externalImage?.let { image ->
            ImageHeader(image.imageUrl, goPreviousScreen, modifier)
            VerticalSpacer(height = 8.dp)
            ContentBody(
                image.username,
                image.username,
                image.profileImageUrl,
                image.likeCount,
                image.description,
                translationState,
                languageState,
                languageDetectionState,
                descriptionLanguage,
                textToSpeechState,
                getTranslatedDescription,
                setTranslationActive,
                setTranslationPassive,
                changeDropdownVisibility,
                selectLanguage,
                startSpeaking,
                resumeSpeaking,
                pauseSpeaking,
                stopSpeaking,
                modifier
            )
        }
    }
    if (languageDetectionState is ImageDetailLanguageDetectionState.Loading ||
        translationState is ImageDetailTranslationState.Loading
    ) {
        LoadingScreen()
    }
}

@Composable
private fun ImageHeader(imageUrl: String, goPreviousScreen: () -> Boolean, modifier: Modifier) {
    Box {
        AsyncImage(
            modifier = modifier.height(LocalConfiguration.current.screenHeightDp.dp / 2),
            contentScale = ContentScale.Crop,
            model = imageUrl,
            contentDescription = null,
        )
        Column(modifier = modifier.offset(16.dp, 16.dp)) {
            IconButton(
                modifier = modifier
                    .clip(CircleShape)
                    .background(Color.White)
                    .size(32.dp),
                onClick = { goPreviousScreen() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back Icon",
                    tint = Color.Black,
                    modifier = modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ContentBody(
    username: String,
    nameSurname: String,
    userProfileImageUrl: String?,
    likeCount: String,
    imageDescription: String,
    translationState: ImageDetailTranslationState,
    languageState: ImageDetailLanguageState,
    languageDetectionState: ImageDetailLanguageDetectionState,
    descriptionLanguage: String?,
    textToSpeechState: ImageDetailTextToSpeechState,
    getTranslatedDescription: (description: String, sourceLanguage: String?, targetLanguage: String) -> Unit,
    setTranslationActive: (translatedText: String, language: String) -> Unit,
    setTranslationPassive: (translatedText: String, language: String) -> Unit,
    changeDropdownVisibility: (visibility: Boolean) -> Unit,
    selectLanguage: (language: String) -> Unit,
    startSpeaking: (text: String) -> Unit,
    resumeSpeaking: () -> Unit,
    pauseSpeaking: () -> Unit,
    stopSpeaking: () -> Unit,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            UserInfo(
                username,
                nameSurname,
                userProfileImageUrl,
                modifier
            )
            ImageLikeInfo(likeCount)
        }
        ImageDescription(
            imageDescription,
            translationState,
            languageState,
            languageDetectionState,
            descriptionLanguage,
            textToSpeechState,
            getTranslatedDescription,
            setTranslationActive,
            setTranslationPassive,
            changeDropdownVisibility,
            selectLanguage,
            startSpeaking,
            resumeSpeaking,
            pauseSpeaking,
            stopSpeaking,
            modifier
        )
    }
}

@Composable
private fun UserInfo(
    username: String,
    nameSurname: String,
    userProfileImageUrl: String?,
    modifier: Modifier
) {
    Row {
        AsyncImage(
            model = userProfileImageUrl,
            contentDescription = null,
            modifier = modifier
                .size(32.dp)
                .clip(CircleShape)
        )
        HorizontalSpacer(width = 8.dp)
        Column {
            Text(
                text = nameSurname,
                fontSize = 16.sp,
                maxLines = 1,
            )
            Text(
                text = username,
                color = Color.Gray,
                fontSize = 12.sp,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ImageLikeInfo(likeCount: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "Like Icon",
            tint = Color.Red
        )
        Text(
            text = likeCount,
            color = Color.Gray,
            fontSize = 10.sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun ImageDescription(
    imageDescription: String,
    translationState: ImageDetailTranslationState,
    languageState: ImageDetailLanguageState,
    languageDetectionState: ImageDetailLanguageDetectionState,
    descriptionLanguage: String?,
    textToSpeechState: ImageDetailTextToSpeechState,
    getTranslatedDescription: (description: String, sourceLanguage: String?, targetLanguage: String) -> Unit,
    setTranslationActive: (translatedText: String, language: String) -> Unit,
    setTranslationPassive: (translatedText: String, language: String) -> Unit,
    changeDropdownVisibility: (visibility: Boolean) -> Unit,
    selectLanguage: (language: String) -> Unit,
    startSpeaking: (text: String) -> Unit,
    resumeSpeaking: () -> Unit,
    pauseSpeaking: () -> Unit,
    stopSpeaking: () -> Unit,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        DescriptionText(
            modifier,
            description = imageDescription
        )
        if (imageDescription.isNotEmpty()) {
            DescriptionAction(
                imageDescription,
                translationState,
                languageState,
                languageDetectionState,
                descriptionLanguage,
                textToSpeechState,
                getTranslatedDescription,
                setTranslationActive,
                setTranslationPassive,
                changeDropdownVisibility,
                selectLanguage,
                startSpeaking,
                resumeSpeaking,
                pauseSpeaking,
                stopSpeaking,
                modifier
            )
        }
        if (translationState is ImageDetailTranslationState.Active) {
            DescriptionText(
                modifier,
                description = translationState.translatedText
            )
        }
    }
}

@Composable
private fun DescriptionText(modifier: Modifier, description: String) {
    Text(
        modifier = modifier.fillMaxWidth(),
        text = description
    )
}

@Composable
private fun DescriptionAction(
    imageDescription: String,
    translationState: ImageDetailTranslationState,
    languageState: ImageDetailLanguageState,
    languageDetectionState: ImageDetailLanguageDetectionState,
    descriptionLanguage: String?,
    textToSpeechState: ImageDetailTextToSpeechState,
    getTranslatedDescription: (description: String, sourceLanguage: String?, targetLanguage: String) -> Unit,
    setTranslationActive: (translatedText: String, language: String) -> Unit,
    setTranslationPassive: (translatedText: String, language: String) -> Unit,
    changeDropdownVisibility: (visibility: Boolean) -> Unit,
    selectLanguage: (language: String) -> Unit,
    startSpeaking: (text: String) -> Unit,
    resumeSpeaking: () -> Unit,
    pauseSpeaking: () -> Unit,
    stopSpeaking: () -> Unit,
    modifier: Modifier
) {
    val textToSpeechIcon = if (textToSpeechState is ImageDetailTextToSpeechState.START
        || textToSpeechState is ImageDetailTextToSpeechState.RESUME
    )
        Icons.Default.Pause
    else Icons.Default.PlayArrow

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row {
            TextButton(
                onClick = {
                    when (translationState) {
                        is ImageDetailTranslationState.Active -> {
                            setTranslationPassive(
                                translationState.translatedText,
                                translationState.language
                            )
                        }
                        is ImageDetailTranslationState.Passive -> {
                            translationState.translatedText?.let { translatedDescription ->
                                if (translationState.language == languageState.selectedLanguage)
                                    setTranslationActive(
                                        translatedDescription,
                                        languageState.selectedLanguage
                                    )
                                else getTranslatedDescription(
                                    imageDescription,
                                    descriptionLanguage,
                                    languageState.selectedLanguage
                                )
                            } ?: run {
                                getTranslatedDescription(
                                    imageDescription,
                                    descriptionLanguage,
                                    languageState.selectedLanguage
                                )
                            }
                        }
                        is ImageDetailTranslationState.Loading -> Unit
                    }
                },
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = when (translationState) {
                        is ImageDetailTranslationState.Active -> "Just see the original"
                        is ImageDetailTranslationState.Passive,
                        ImageDetailTranslationState.Loading -> "See translation"
                    },
                    fontStyle = FontStyle.Italic,
                    fontSize = 10.sp
                )
            }
            HorizontalSpacer(width = 8.dp)
            TextButton(
                onClick = { changeDropdownVisibility(true) },
            ) {
                Text(
                    text = languageState.selectedLanguage,
                    color = Color.Gray,
                    fontSize = 10.sp
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    tint = Color.Gray,
                    contentDescription = null
                )
                LanguageDropDown(
                    visibility = languageState.languageDropdownVisibility,
                    languages = languageState.languages,
                    selectLanguage = selectLanguage
                ) {
                    changeDropdownVisibility(false)
                }
            }
        }

        if (languageDetectionState is ImageDetailLanguageDetectionState.Detected &&
            descriptionLanguage == "en"
        ) {
            IconButton(onClick = {
                when (textToSpeechState) {
                    is ImageDetailTextToSpeechState.START -> pauseSpeaking()
                    is ImageDetailTextToSpeechState.RESUME -> stopSpeaking()
                    is ImageDetailTextToSpeechState.PAUSE -> resumeSpeaking()
                    is ImageDetailTextToSpeechState.STOP -> startSpeaking(imageDescription)
                }
            }) {
                Icon(
                    imageVector = textToSpeechIcon,
                    contentDescription = "Sound Icon",
                    tint = MaterialTheme.colors.primary,
                    modifier = modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun LanguageDropDown(
    visibility: Boolean,
    languages: List<String>,
    selectLanguage: (language: String) -> Unit,
    collapseDropDown: () -> Unit,
) {
    DropdownMenu(
        expanded = visibility,
        onDismissRequest = { collapseDropDown() }) {
        languages.forEach { languageCode ->
            DropdownMenuItem(onClick = { selectLanguage(languageCode) }) {
                Text(
                    text = languageCode,
                    fontSize = 10.sp
                )
            }
        }
    }
}