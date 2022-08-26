# UrbanHomeServices Application

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

UrbanHomeServices App will give insight about how Huawei's AGC Cloud DB service can be used by Service Providers to add services like Plumbing, Appliance Repair, Painting, Carpentry and House Keeping. So that, Consumer can query and avail those services based on their current location.
This app will also give insight about how HMS Identity kit will be used for user's address management like adding, editing and deleting addresses.

## What You Will Create

In this UrbanHomeServices Demo codelab, we are going to create a end to end prototype for adding, editing and deleting services from Cloud DB and query for those service based on user's address updated using HMS Identity kit.
Also, search for nearby stores using the HMS Site kit and show the route on the map using HMS Location and Map kit.

This application will be used by 2 kinds of Users.
*	Service Provider
*	Consumer

Service Provider:

*	Login using Huawei Id or Facebook Id.
*	Add, Update and Delete service details in the Cloud DB.

Consumer :
 
*	Login using Huawei Id or Facebook Id.
*	Querying for Service Providers like Plumbers, Electricians, Painters, Carpenters and House keeping.
*	Add, Update and Delete address.
*	Search for nearby stores and view the route to the store location on the map.

## What You Will Learn

In this code lab, you will learn how to:

*	Integrate HUAWEI Account Kit.
*	Integrate Auth service. 
*	Integrate Map and Location Kits.
*   Integrate Cloud DB services.
*	Integrate Identity Kit.
*	Integrate Site Kit

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

## Prepare Initial configuration

Use the below link to do initial configuration for the application development
https://developer.huawei.com/consumer/en/codelab/HMSPreparation/index.html#3

## Enable HUAWEI Service(s) in AGC console

Enable the API permission for below kits from Project Settings, Manage APIs and enable the API permission.

*	Map Kit
*	Site Kit
*	Location Kit
*	Account Kit
*	Auth Service
*	Cloud DB
*	Identity Kit

```
**Note:** Some API’s will be enabled by default. If not enable it manually.
```

## Integrating HMS SDK
For official Documentation and more services, please refer below documentation from developer website

1.	[Account Kit](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/introduction-0000001050048870)

2.	[Auth Service](https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/agc-auth-introduction-0000001053732605)

3.	[Map Kit](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides-V5/android-sdk-brief-introduction-0000001061991343-V5)

4.	[Site Kit](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/android-sdk-introduction-0000001050158571)

5.	[Location Kit)](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/introduction-0000001050706106)

6.	[Cloud DB](https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/agc-clouddb-introduction)

7.	[Identity Kit](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/introduction-0000001050040471)


## Example Code

AddServiceActivity: To add services into Cloud DB by Service provider

LoginActivity: Used to login into the application by Consumer and Service provider using HUAWEI ID and Facebook Id

MainActivity: Displays services added by Service provider like Plumber, Electricians, Painters, Carpenters ans so on.

ManageServiceActivity: To Edit or Delete services from Cloud DB as per User.

NearByStoresLocationActivity: Used to draw the route between the Consumer ans the Service provider

ServiceDetailsActivity: Consumer can check service details to dial or email to service provider

ServiceDetailsCloudDBActivity: Consumer can find nearby search services list like Plumber, Electricians, Painters, Carpenters ans so on.

SiteKitResultActivity: Consumer can find nearby services from HMS Site Kit list like Plumber, Electricians, Painters, Carpenters ans so on.

SplashActivity: Used to select User type to login.

AppConstants: Contains the constants used inside the application

## References

*	[Account Kit](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/introduction-0000001050048870)
*	[Auth Service](https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/agc-auth-introduction-0000001053732605)
*	[Map Kit](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides-V5/android-sdk-brief-introduction-0000001061991343-V5)
*	[Site Kit](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/android-sdk-introduction-0000001050158571)
*	[Location Kit](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/introduction-0000001050706106)
*	[Cloud DB](https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/agc-clouddb-introduction)
*	[Identity Kit](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/introduction-0000001050040471)

## License

HMS UrbanHomeServices is licensed under the [Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0).