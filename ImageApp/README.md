# ImageApp

# Huawei Mobile Services
Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.

## Table of Contents
* [Introduction](#introduction)
* [What you will Create](#what-you-will-create)
* [What You Will Learn](#what-you-will-learn)
* [What You Will Need](#what-you-will-need)
* [Preparations](#preparations)
* [License](#license)

## Introduction :

You can search images with keyboard and image classification in ImageApp. Images from Cloud DB are listed in the search result. You can see information about the image on the ImageDetail page. You can translate and vocalize the picture description into different languages using ML Kit.

## What You Will Create

In this code lab, you will create a demo project and use Cloud DB, ML Kit(Image Classification, Language Detection, Translation, Text to Speech).

*	Querying data in Cloud DB.
*	Detecting the language of text.
*	Convert text information into audio output in real time.
*	Translate text from the source language into the target language through the server on the cloud.
*	Classifies elements in images into intuitive categories.

## What You Will Learn

In this code lab, you will learn how to integrate:
*	ML Kit - Image Classification
*	ML Kit - Language Detection
*	ML Kit - Translation
*	ML Kit - Text to Speech
*	Cloud DB

## What You Will Need

### Hardware Requirements

*	A computer (desktop or laptop) that runs the Windows 10 operating system
*	Huawei phone with HMS Core (APK) 5.0.0.300 or later installed
```
**Note:** Please prepare the preceding hardware environment and relevant devices in advance.
```
### Software Requirements

*	[Android Studio 3.X](https://developer.android.com/studio)
*	JDK 1.8 and later 
*	SDK Platform 23 and later
*	Gradle 4.6 and later


## Preparations
Preparations required to run the application.

### Create a Project and App in AppGalleryConnect
https://developer.huawei.com/consumer/en/codelab/HMSPreparation/index.html#0

### Configuring the Signing Certificate Fingerprint in AppGallery Connect
https://developer.huawei.com/consumer/en/codelab/HMSPreparation/index.html#4

### Enable HUAWEI Service(s) in AGC console
https://developer.huawei.com/consumer/en/codelab/HMSPreparation/index.html#5

Enable the API permission for below kits from Project Settings, Manage APIs and enable the API permission.
*	ML Kit
*	Cloud DB

```
**Note:** Some API’s will be enabled by default. If not enable it manually.
```

### Adding the AppGallery Connect Configuration File of Your App
https://developer.huawei.com/consumer/en/codelab/HMSPreparation/index.html#6

* Sign in to AppGallery Connect, go to `Project settings > General information`. In the App information area, download the agconnect-services.json file.
* Copy the agconnect-services.json file to the app's root directory.

### Create Cloud DB Zone in AGC console
Create Cloud DB Zone named ImageDbZone
https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/agc-clouddb-agcconsole-clouddbzones-0000001127453707

### Create Cloud DB Object Type in AGC console
Create Cloud DB Object named Order as below

| Field           | Type    | Primary Key | Not Null |
|-----------------|---------|-------------|----------|
| id              | String  |     ✓       |    ✓     |
| likes           | Integer |             |          |
| description     | String  |             |          |
| imageUrl        | String  |             |          |
| profileImageUrl | String  |             |          |
| name            | String  |             |          |
| username        | String  |             |          |
| key             | String  |             |          |

https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/agc-clouddb-agcconsole-objecttypes-0000001127675459

### Adding Data to Cloud DB and Export Java Files in AGC console
https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/agc-clouddb-agcconsole-managingdata-0000001080975864

You can add images to the cloud db manually or by importing the images.json file.
You can see the images by following the directory `app > src > main > java > res > raw > images.json.`

After adding data to Cloud DB, export java files and replace with Image.java and ObjectTypeInfoHelper.java in `app > src > main > java > YOUR_PACKAGE_NAME > data > model` directory.

## Integrating HMS SDK
For official Documentation and more services, please refer below documentation from developer website.

*  [ML Kit](https://developer.huawei.com/consumer/en/hms/huawei-mlkit/)

*  [Cloud DB](https://developer.huawei.com/consumer/en/agconnect/cloud-base/)


## License
HMS Guide sample is licensed under the [Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0).