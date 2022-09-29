# Courier Tracking

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

In CourierTracking, the user first lists products from Cloud DB. Each incoming product contains the location information of the store where the product is located. When the user clicks on any product in the list, the location of the user is taken with the Location Kit and displayed on the Map by creating a route between the user's location and the store location with the Map Kit Direction API feature. Then the courier tracking simulation is started on the Map. When the courier reaches the user, the function in Cloud Function is triggered by HTTP Trigger and a notification is sent to the user. 

## What You Will Create

In this code lab, you will create a demo project and use Cloud Functions, Cloud DB, Map Kit, Location Kit and Push Kit.

*	Querying data in Cloud DB.
*	Getting user location using Location Kit.
*	Creating a route between two locations using the Direction API.
*	Drawing on a Map (Marker,Polyline).
*	Sending notifications with Push Kit via Cloud Functions.

## What You Will Learn

In this code lab, you will learn how to integrate:
*	Cloud Functions
*	Cloud DB
*	Map Kit
*	Location Kit
*	Push Kit

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
*	Cloud Functions
*	Cloud DB
*	Map Kit
*	Location Kit
*	Push Kit

```
**Note:** Some API’s will be enabled by default. If not enable it manually.
```

### Adding the AppGallery Connect Configuration File of Your App
https://developer.huawei.com/consumer/en/codelab/HMSPreparation/index.html#6

* Sign in to AppGallery Connect, go to Project settings > General information. In the App information area, download the agconnect-services.json file.
* Copy the agconnect-services.json file to the app's root directory.

### Create Cloud DB Zone in AGC console
Create Cloud DB Zone named CourierDbZone
https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/agc-clouddb-agcconsole-clouddbzones-0000001127453707

### Create Cloud DB Object Type in AGC console
Create Cloud DB Object named Order as below

Field                   Type        Primary Key     Not Null
id                      String          ✓               ✓
productTitle            String
productDescription      String
productPhotoUrl         String
productPrice            Double
status                  Integer
storeLat                Double
storeLng                Double

https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/agc-clouddb-agcconsole-objecttypes-0000001127675459

### Adding Data to Cloud DB and Export Java Files in AGC console
https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/agc-clouddb-agcconsole-managingdata-0000001080975864

You can add orders to the cloud db manually or by importing the orders.json file.
You can see the orders by following the directory app > main > res > raw > orders.son.

After adding data to Cloud DB, export java files and replace with Order.java and ObjectTypeInfoHelper.java in app > main > java > data > network > model directory.

### Creating Function and HTTP Trigger from Cloud Function in AGC console
https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/agc-cloudfunction-create-0000001058511532#section397094161014

* Create a function in AppGallery Connect
* Enter your Client ID and ClientSecret information into handler.js in the courier_tracking_js file in the project directory. Then zip the courier_tracking_js file and upload it to the function you created.

https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/agc-cloudfunction-httptrigger-0000001301187876
* Create HTTP Trigger named courier-tracking-notification


## Integrating HMS SDK
For official Documentation and more services, please refer below documentation from developer website.

*  [Cloud Function](https://developer.huawei.com/consumer/en/agconnect/cloud-function/)

*  [Cloud DB](https://developer.huawei.com/consumer/en/agconnect/cloud-base/)

*  [Map Kit](https://developer.huawei.com/consumer/en/hms/huawei-MapKit/)

*  [Location Storage](https://developer.huawei.com/consumer/en/hms/huawei-locationkit/)

*  [Push Kit](https://developer.huawei.com/consumer/en/hms/huawei-pushkit/)


## License
HMS Guide sample is licensed under the [Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0).