# Image BG Cleaner

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

In ImageBGCleaner, the user can remove image bacground after image is picked and After image background is removed, the user can upload image to Cloud Storage,List all image on Cloud and delete any file. In addition, users can save the image only to the phone memory if they wish.

## What You Will Create

In this code lab, you will create a demo project and use ML Kit, Auth Service and Cloud Storage.

*	Remove Backgorund using Image Segmentation feature of ML Kit.
*	Uploading a File to Cloud Storage
*	Listing File on Cloud Storage
*	Delete a File on Cloud Storage
*	Downloading a File on Cloud Storage

## What You Will Learn

In this code lab, you will learn how to integrate:
*	Ml Kit
*	Cloud Storage
*	Auth Service

## What You Will Need

### Hardware Requirements

*	A computer (desktop or laptop) that runs the Windows 10 operating system
*	Huawei phone with HMS Core (APK) 5.0.0.300 or later installed
```
**Note:** Please prepare the preceding hardware environment and relevant devices in advance.
```
### Software Requirements

*	[Android Studio 3.X](https://developer.android.com/studio)
*	JDK 1.8.211 or later 
*	minSdkVersion 19 or later (mandatory)
*	targetSdkVersion 31 (recommended)
*	compileSdkVersion 31 (recommended)
*	Gradle 4.6 and later (recommended)


## Preparations
Preparations required to run the application.

### Create a Project and App in AppGalleryConnect
https://developer.huawei.com/consumer/en/codelab/HMSPreparation/index.html#0

### Configuring the Signing Certificate Fingerprint in AppGallery Connect
https://developer.huawei.com/consumer/en/codelab/HMSPreparation/index.html#4

### Enable HUAWEI Service(s) in AGC console
https://developer.huawei.com/consumer/en/codelab/HMSPreparation/index.html#5

Enable the following Services in AppGallery Connect Console
*	ML Kit
*	Cloud Storage
*	Auth Service

### Adding the AppGallery Connect Configuration File of Your App
https://developer.huawei.com/consumer/en/codelab/HMSPreparation/index.html#6

* Sign in to AppGallery Connect, go to Project settings > General information. In the App information area, download the agconnect-services.json file.
* Copy the agconnect-services.json file to the app's root directory.

### Enable ML Kit
https://developer.huawei.com/consumer/en/doc/development/hiai-Guides/enable-service-0000001050038078

### Using ML Kit Segmentation
https://developer.huawei.com/consumer/en/doc/development/hiai-Guides/image-segmentation-0000001050040109

### Enable Cloud Storage
https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/agc-cloudstorage-enable-service-0000001275330014
* Set Storage instance and Default data processing location.

The storage instance name:

* Can contain only lowercase letters, digits, and hyphens (-).
* Must start with a digit or letter.
* Can contain 3 to 57 characters.
* Cannot be an IP address.
* Cannot end with a hyphen (-).
* Must be globally unique and cannot be changed after being created.


### Uploading a File to Cloud Storage
https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/agc-cloudstorage-upload-android-0000001055326211

### Listing Files
https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/agc-cloudstorage-list-android-0000001054966219
### Downloading a File
https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/agc-cloudstorage-download-android-0000001054766193
### Deleting a File
https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/agc-cloudstorage-delete-android-0000001055726156
### Enable Auth Service
https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/agc-auth-enable-service-0000001274125746

### Use Anonymous Account
https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/agc-auth-android-anonymous-0000001053532658

## Integrating HMS SDK
For official Documentation and more services, please refer below documentation from developer website.

*  [Cloud Storage](https://developer.huawei.com/consumer/en/agconnect/cloud-storage/)

*  [Auth Service](https://developer.huawei.com/consumer/en/agconnect/auth-service/)

*  [ML Kit](https://developer.huawei.com/consumer/en/hms/huawei-mlkit/)

## License
HMS Guide sample is licensed under the [Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
