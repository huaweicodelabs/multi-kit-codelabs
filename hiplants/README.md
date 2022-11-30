# HiPlants

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

HiPlants is an application that will guess what the plants are which we have photographed with the help of Custom trained data using ML Kit. And will bring us the relevant websites about it.

## What You Will Create

In this codelab, you will use the demo project created to experience ML Kit and Search Kit.

*	Creating your custom trained model.
*	Upload your plant photo.
*	Let ML Kit predict which type of plant it is with using custom model and image classification.
*	Finally, let the Search Kit show us the websites related to the type of plant.


## What You Will Learn

In this code lab, you will learn how to integrate:
*	ML Kit
*	Search Kit


## What You Will Need

### Hardware Requirements

* A computer (desktop or laptop) that runs the Windows 10 operating system
* Huawei phone running EMUI 5.0 or later, or a non-Huawei phone running Android 4.4 or later (Some capabilities are available only to Huawei phones.)
* The phone is used for running and debugging the demo.
```
**Note:** Please prepare the preceding hardware environment and relevant devices in advance.
```
### Software Requirements

*	[Android Studio 3.X](https://developer.android.com/studio)
*	JDK 1.8.211 and later 
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
*	Search Kit


```
**Note:** Some API’s will be enabled by default. If not enable it manually.
```

### Adding the AppGallery Connect Configuration File of Your App
https://developer.huawei.com/consumer/en/codelab/HMSPreparation/index.html#6

* Sign in to AppGallery Connect, go to Project settings > General information. In the App information area, download the agconnect-services.json file.
* Copy the agconnect-services.json file to the app's root directory.

### Create Custom Model
Create custom trained model with using HMS Toolkits 
https://developer.huawei.com/consumer/en/doc/development/Tools-Guides/ai-create-0000001055252424#section389320241331

### Create local integration folder for our custom model
Integrate a model locally
https://developer.huawei.com/consumer/en/doc/development/hiai-Guides/integrating-locally-0000001052825781

### Integrate custom model into the project.
You need to inform the inference engine of the location of your model.
https://developer.huawei.com/consumer/en/doc/development/hiai-Guides/model-inference-0000001052064963


### Usage of Image classification service
This service provides the pre-trained model capability and allows users to pre-train image classification models.
https://developer.huawei.com/consumer/en/doc/development/hiai-Guides/image-classification-pre-trained-model-0000001055210299


## Integrating HMS SDK
For official Documentation and more services, please refer below documentation from developer website.

*  [ML Kit](https://developer.huawei.com/consumer/en/hms/huawei-mlkit/)

*  [Search Kit](https://developer.huawei.com/consumer/en/hms/huawei-searchkit/)




## License
HMS Guide sample is licensed under the [Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0).