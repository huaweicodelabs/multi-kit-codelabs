# Tourism

# Huawei Mobile Services
Copyright (c) Huawei Technologies Co., Ltd. 2012-2021. All rights reserved.

## Table of Contents
* [Introduction](#introduction)
* [What you will Create](#what-you-will-create)
* [What You Will Learn](#what-you-will-learn)
* [Hardware Requirements](#hardware-requirements)
* [Software Requirements](#software-requirements)
* [Example Code](#example-code)
* [License](#license)

## Introduction :

Travelers can search for areas of interest, things to do, or notable locations in Huawei Maps. Find places like nearby Atms, Restaurants, and Hospitals.

## What You Will Create

Tourism Application that helps to find all the necessary information about tourist visits with the help of ML Kit's landmark recognition API, you can recognize well-known landmarks in an image. When you pass an image to this API, you will get the recognized landmark, along with few related images using search kit.  These related images can quickly display interactive viewing of 360-degree spherical or cylindrical panoramic images using Panorama kit. Users in an all-encompassing experience along with that shows popular places, city attractions or nearby places to visit that happen in an area using Site kit.

*	Authentication
*	Search Nearby Place
*       Image search load related images of that place
*       Fetch a location and display in the map
*       360° image showcases and interactions in a 3D space

## What You Will Learn

In this code lab, you will learn how to:

*	Integrate Auth Service.
*	Integrate Location Kit.
*	Integrate Map.
*	Integrate Site. 
*	Integrate ML Kit. 
*	Integrate Search. 
*	Integrate Panorama Kit. 

## What You Will Need

### Hardware Requirements

*	A computer (desktop or laptop) that runs the Windows 10 operating system
*	Huawei phone with HMS Core (APK) 5.0.0.300 or later installed
```
**Note:** Please prepare the preceding hardware environment and relevant devices in advance.
```
### Software Requirements

*	[Android Studio 4.X](https://developer.android.com/studio)
*	JDK 1.8 and later 
*	SDK Platform 24 and later
*	Gradle 6.0 and later

```
**Note:** Please prepare the preceding software environment in advance.
> Only EMUI 5 (API Level 26) and later versions support Location, Map & Site Kit. 
```

## Prepare Initial configuration

Use the below link to do initial configuration for the application development
https://developer.huawei.com/consumer/en/codelab/HMSPreparation/index.html#3

## Enable HUAWEI Service(s) in AGC console

Enable the API permission for below kits from Project Settings, Manage APIs and enable the API permission.

*	Auth Service
*	LocationKit
*	Site Kit
*	Search Kit
*	Map Kit
*	ML Kit
*	Panorama Kit

```
**Note:** Some API’s will be enabled by default. If not enable it manually.
```

## Integrating HMS SDK
For official Documentation and more services, please refer below documentation from developer website

1.	[Auth Service(Mobile Number, Huawei Id)](https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/agc-auth-android-getstarted-0000001053053922)

2.	[Map Kit](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides-V5/android-sdk-use-0000001062402024-V5)

3.	[Site Kit](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides-V5/android-sdk-nearby-place--search-0000001050158585-V5)

4.	[Location Kit](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides-V5/location-develop-steps-0000001050746143-V5)

5.	[Search Kit](https://developer.huawei.com/consumer/en/hms/huawei-searchkit/)

6.	[ML Kit](https://developer.huawei.com/consumer/en/hms/huawei-mlkit/)

7.	[Panorama Kit](https://developer.huawei.com/consumer/en/hms/huawei-panoramakit/)


## References

*	[Auth Service(Mobile Number, Huawei Id)](https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/agc-auth-android-getstarted-0000001053053922)

*	[Map Kit](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides-V5/android-sdk-use-0000001062402024-V5)

*	[Site Kit](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides-V5/android-sdk-nearby-place--search-0000001050158585-V5)

*	[Location Kit](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides-V5/location-develop-steps-0000001050746143-V5)

*	[Search Kit](https://developer.huawei.com/consumer/en/hms/huawei-searchkit/)

*	[ML Kit](https://developer.huawei.com/consumer/en/hms/huawei-mlkit/)

*	[Panorama Kit](https://developer.huawei.com/consumer/en/hms/huawei-panoramakit/)


## License
Tourism App is licensed under the [Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0).