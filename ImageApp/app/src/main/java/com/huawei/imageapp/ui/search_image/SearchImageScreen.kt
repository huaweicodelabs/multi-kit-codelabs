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

import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.huawei.imageapp.core.designsystem.component.*
import com.huawei.imageapp.core.designsystem.theme.BlackGradientColor
import com.huawei.imageapp.domain.model.ExternalImage
import com.huawei.imageapp.domain.model.ImageClassification

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchImageScreen(
    navigateToImageDetail: () -> Unit,
    setImageToCurrentBackStackEntry: (ExternalImage) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchImageViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val imageState by viewModel.imageState.collectAsState()
    val appBarState by viewModel.appBarState.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            viewModel.getClassifiedImageResult(bitmap)
        }
    )

    SearchImageContent(
        imageState = imageState,
        setImageToCurrentBackStackEntry = setImageToCurrentBackStackEntry,
        navigateToImageDetail = navigateToImageDetail,
        appBarState = appBarState,
        dialogState = dialogState,
        setDefaultAppBar = viewModel::setDefaultAppBar,
        setSearchAppBar = viewModel::setSearchAppBar,
        chooseImageFromGallery = { launcher.launch("image/*") },
        dismissDialog = viewModel::hideClassificationDialog,
        searchImage = viewModel::searchImage,
        closeKeyboard = {
            focusManager.clearFocus()
            keyboardController?.hide()
        },
        searchQuery = searchQuery,
        onTextChanged = { text -> viewModel.setSearchQuery(text) },
        modifier = modifier
    )
}

@Composable
fun SearchImageContent(
    imageState: ImageState,
    setImageToCurrentBackStackEntry: (ExternalImage) -> Unit,
    navigateToImageDetail: () -> Unit,
    appBarState: SearchImageAppBarState,
    dialogState: SearchImageDialogState,
    setDefaultAppBar: () -> Unit,
    setSearchAppBar: () -> Unit,
    chooseImageFromGallery: () -> Unit,
    dismissDialog: () -> Unit,
    searchImage: (query: String) -> Unit,
    closeKeyboard: () -> Unit,
    searchQuery: String,
    onTextChanged: (text: String) -> Unit,
    modifier: Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            SearchImageAppBar(
                appBarState,
                setDefaultAppBar,
                setSearchAppBar,
                chooseImageFromGallery,
                searchImage,
                closeKeyboard,
                searchQuery,
                onTextChanged,
                modifier
            )
        }
    ) { paddingValues ->
        VerticalSpacer(height = 8.dp)
        when (imageState) {
            is ImageState.Success -> {
                Column(
                    modifier = modifier
                        .padding(paddingValues)
                ) {
                    ImageList(
                        modifier,
                        imageState.externalImages,
                        setImageToCurrentBackStackEntry,
                        navigateToImageDetail
                    )
                }
            }
            is ImageState.Loading -> LoadingScreen()
            is ImageState.Error -> ErrorMessage(message = imageState.errorMessage)
            is ImageState.None -> InfoMessage(message = "Please search to see image results.")
        }
        when (dialogState) {
            is SearchImageDialogState.Visible -> {
                ClassificationDialog(
                    classifications = dialogState.classifications,
                    onClassificationItemClick = { text ->
                        onTextChanged(text)
                        setSearchAppBar()
                        searchImage(text)
                    },
                    dismissDialog = dismissDialog,
                    modifier = modifier
                )
            }
            is SearchImageDialogState.Loading -> LoadingScreen()
            is SearchImageDialogState.Gone -> Unit
        }
    }
}

@Composable
private fun ClassificationDialog(
    classifications: List<ImageClassification>,
    onClassificationItemClick: (classification: String) -> Unit,
    dismissDialog: () -> Unit,
    modifier: Modifier
) {

    Dialog(onDismissRequest = { dismissDialog() }) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .height(400.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = modifier.padding(8.dp)
            ) {
                Text(
                    text = "Image Classifications",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
                VerticalSpacer(height = 16.dp)
                LazyColumn(modifier = modifier.fillMaxSize()) {
                    items(classifications) { classification ->
                        Column(
                            modifier = modifier
                                .padding(vertical = 10.dp)
                                .clickable {
                                    onClassificationItemClick(classification.name)
                                    dismissDialog()
                                }
                        ) {
                            Row(
                                modifier = modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = classification.name, fontSize = 16.sp)
                                Text(
                                    text = classification.possibility,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            VerticalSpacer(height = 8.dp)
                            Divider(thickness = .5.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchImageAppBar(
    appBarState: SearchImageAppBarState,
    setDefaultAppBar: () -> Unit,
    setSearchAppBar: () -> Unit,
    chooseImageFromGallery: () -> Unit,
    searchImage: (query: String) -> Unit,
    closeKeyboard: () -> Unit,
    searchQuery: String,
    onTextChanged: (text: String) -> Unit,
    modifier: Modifier
) {
    when (appBarState) {
        SearchImageAppBarState.DefaultAppBar -> DefaultAppBar(
            setSearchAppBar,
            chooseImageFromGallery
        )
        SearchImageAppBarState.SearchAppBar -> SearchAppBar(
            modifier,
            setDefaultAppBar,
            searchImage,
            closeKeyboard,
            searchQuery,
            onTextChanged
        )
    }
}

@Composable
private fun DefaultAppBar(
    setSearchAppBar: () -> Unit,
    chooseImageFromGallery: () -> Unit
) {
    TopAppBar(
        title = { Text(text = "HMS ImageApp") },
        actions = {
            IconButton(onClick = { setSearchAppBar() }) {
                Icon(imageVector = Icons.Filled.Search, contentDescription = null)
            }
            IconButton(onClick = { chooseImageFromGallery() }) {
                Icon(imageVector = Icons.Filled.Photo, contentDescription = null)
            }
        }
    )
}

@Composable
private fun SearchAppBar(
    modifier: Modifier,
    setDefaultAppBar: () -> Unit,
    searchImage: (query: String) -> Unit,
    closeKeyboard: () -> Unit,
    searchQuery: String,
    onTextChanged: (text: String) -> Unit
) {
    Surface(
        modifier
            .fillMaxWidth()
            .height(56.dp),
        elevation = AppBarDefaults.TopAppBarElevation,
        color = MaterialTheme.colors.primary
    ) {
        TextField(modifier = Modifier
            .fillMaxWidth(),
            value = searchQuery,
            onValueChange = { text -> onTextChanged(text) },
            placeholder = {
                Text(
                    modifier = Modifier
                        .alpha(ContentAlpha.medium),
                    text = "Search",
                    color = Color.White
                )
            },
            textStyle = TextStyle(
                fontSize = MaterialTheme.typography.subtitle1.fontSize
            ),
            singleLine = true,
            leadingIcon = {
                IconButton(
                    modifier = modifier
                        .alpha(ContentAlpha.medium),
                    onClick = {}
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon",
                        tint = Color.White
                    )
                }
            },
            trailingIcon = {
                IconButton(
                    onClick = { setDefaultAppBar() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Icon",
                        tint = Color.White
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    closeKeyboard()
                    searchImage(searchQuery)
                }
            ),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                cursorColor = Color.White.copy(alpha = ContentAlpha.medium)
            ))
    }
}

@Composable
private fun ImageList(
    modifier: Modifier,
    images: List<ExternalImage>,
    setImageToCurrentBackStackEntry: (ExternalImage) -> Unit,
    navigateToImageDetail: () -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2)
    ){
      items(images){ image ->
          ImageItem(
              modifier,
              image.imageUrl,
              image.profileImageUrl,
              image.name,
          ) {
              setImageToCurrentBackStackEntry(image)
              navigateToImageDetail()
          }
      }
    }
}

@Composable
private fun ImageItem(
    modifier: Modifier,
    imageUrl: String,
    profileImageUrl: String?,
    userName: String,
    onItemClick: () -> Unit
) {
    Box(
        modifier = modifier
            .padding(1.dp)
            .clickable { onItemClick() },
        contentAlignment = Alignment.BottomStart
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = modifier.fillMaxWidth()
                .aspectRatio(1f),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, BlackGradientColor)
                    )
                )
                .padding(top = 8.dp, bottom = 4.dp, start = 4.dp, end = 4.dp)
        ) {
            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = profileImageUrl,
                    contentDescription = null,
                    modifier = modifier
                        .size(16.dp)
                        .clip(CircleShape)
                )
                HorizontalSpacer(width = 4.dp)
                Text(
                    text = userName,
                    color = Color.White,
                    fontSize = 12.sp,
                    maxLines = 1,
                )
            }
        }
    }
}