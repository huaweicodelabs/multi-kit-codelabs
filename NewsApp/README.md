## SmartNews-App-sample

## Table of Contents

 * [Introduction](#introduction)
 * [Installation](#installation)
 * [Configuration ](#configuration )
 * [Supported Environments](#supported-environments)
 * [Sample Code](# Sample Code)
 * [License](#license)
 
 
## Introduction
    The sample code is used to implement the function of converting the text to audio output in different languages using ML Kit. 
    The following describes the structure of the sample code:

	activities: UI, which contains the latest News articles.
	adapters  : Adapter to display the list of news from different category.
	models    : Data model classes.
     	view      : To observe the view model class and represent the UI
	viewmodel : Link between view and model
	utils     : A tool class.

## Installation
    To use functions provided by examples, please make sure Huawei Mobile Service 5.0 has been installed on your cellphone. 
    There are two ways to install the sample demo:

    You can compile and build the codes in Android Studio. After building the APK, you can install it on the phone and debug it.
    Generate the APK file from Gradle. Use the ADB tool to install the APK on the phone and debug it adb install 
    {YourPath}\app\release\app-release.apk
    
## Supported Environments
	Android Studio 4.X, JDK 1.8 and later , SDK Platform 21 and later, Gradle 4.6 and later

	
## Configuration 
    Create an app in AppGallery Connect and obtain the project configuration file agconnect-services.json. 
    In Android Studio, switch to the Project view and move the agconnect-services.json file to the root directory of the app.

    Change the value of applicationId in the build.gradle file of the app to the name of the app package applied for in the preceding step.
	
## Sample Code
   
    The SmartNews App provides demonstration for following scenarios:

    1. Login is implemented in Main Activity.	
    2. Implemented Network kit to fetch News from API in NewsActivity class and also integerated with Ads kit.	  
       News API is used in the application to retrieve the live aritcles or latest news.Register and get new Api key to get live news and update the Api key in file.
    3.Search kit is implemented to search for news search.
    4.Integrated web browser to view full News article in NewDetailActivity.
    5. Integrated the ML Kit and initialized in Application Class .Text is translated to different language(user selected language) and converted to speech and ASR is used to give input to search kit .
       Language settings can be changed in settings Activty
    6. Integrated few common kits like Account, Crash Service, Ads kit.  
     
##  License
* Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.

*Software: glide
 Copyright 2014 Google

*Software :Dagger
 Copyright  2012-2019 vogella GmbH
