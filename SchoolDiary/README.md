# School Diary

# Huawei Mobile Services
Copyright (c) Huawei Technologies Co., Ltd. 2012-2022. All rights reserved.

## Table of Contents
* [Introduction](#introduction)
* [What you will Create](#what-you-will-create)
* [What You Will Learn](#what-you-will-learn)
* [Hardware Requirements](#hardware-requirements)
* [Software Requirements](#software-requirements)
* [License](#license)

## Introduction :

School Diary app will give you an insight about the Serverless components from Huawei Ecosystem such as Cloud Storage, Cloud DB. Apart from that you will also get an idea about Auth - Service that can be used to login to the application. The teacher and student mapping process which is an important feature, on the app. This process involves the QR code scanning and the save user profile, mapping details on the database.

## What You Will Create

In this code lab, you will create a School Diary project and use the APIs of HUAWEI Auth - Service, Scan Kit, Cloud DB, Cloud Storage. We are going to create a end to end Task diary application for two type of users who are Teacher and Student. A teacher could be create & assign, vallidate and close the task. A student can view and submit task by upload images of their work.

*  Login with Auth - Service
*  Scan QR and map the student with teacher
*  Teacher user - Create & assign, validate and Close the tasks with Cloud DB operations
*  Student user - Upload the task images in Cloud Storage and update to Cloud DB

## What You Will Learn

In this code lab, you will learn how to:
*  Auth - Service
*  Scan Kit
*  Cloud DB
*  Cloud Storage

## What You Will Need

### Hardware Requirements

*  A computer (desktop or laptop) that runs the Windows 10 operating system
*  Huawei phone with HMS Core (APK) 5.0.0.300 or later installed
```
**Note:** Please prepare the preceding hardware environment and relevant devices in advance.
```
### Software Requirements

*  [Android Studio Arctic Fox | 2020.3.1](https://developer.android.com/studio)
*  JDK 1.8 and later
*  SDK Platform 23 and later
*  Gradle 7.0.2 and later


## Prepare Initial configuration

Use the below link to do initial configuration for the application development
https://developer.huawei.com/consumer/en/codelab/HMSPreparation/index.html#3

## Enable HUAWEI Service(s) in AGC console

Enable the API permission for below kits from Project Settings, Manage APIs and enable the API permission.
*  Auth - Service
*  Scan Kit
*  Cloud DB
*  Cloud Storage

```
**Note:** Some API’s will be enabled by default. If not enable it manually.
```

## Integrating HMS SDK
For official Documentation and more services, please refer below documentation from developer website

*  [Auth - Service](https://developer.huawei.com/consumer/en/agconnect/auth-service/)
*  [Scan Kit](https://developer.huawei.com/consumer/en/hms/huawei-scankit/)
*  [Cloud DB](https://developer.huawei.com/consumer/en/agconnect/cloud-base/)
*  [Cloud Storage](https://developer.huawei.com/consumer/en/agconnect/cloud-storage/)


## License
HMS Guide sample is licensed under the [Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0).