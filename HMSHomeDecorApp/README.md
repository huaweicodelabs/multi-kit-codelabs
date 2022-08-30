# HMSHomeDecorApp Application

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

HMSHomeDecorApp will give insight about how Huawei's Account Kit and Scene Kit, that are used to login to the app place 3D object virtually in a real world using Scene Kit ARView. The Scene Kit ARView can detect any plane like wall or ceiling or floor, and place a 3D object to decorate it.

## What You Will Create

In this HMSHomeDecorApp codelab, we are goint to create login screen to authenticate the app. After authenticating, it shows the screen which categories with three different section. We can select a particular item in specific section and add to cart or see it on the ARCamera to place a 3D object in it. 

## What You Will Learn

In this code lab, you will learn how to:

*	Integrate HUAWEI Account Kit.
*	Integrate Auth service. 
*	Integrate Scene Kit.



## What You Will Need

### Hardware Requirements

*	A computer (desktop or laptop) that runs the Windows 10 operating system
*	Huawei phone with HMS Core (APK) 5.0.0.300 or later installed and EMUI version 9.1 or later
```
**Note:** Please prepare the preceding hardware environment and relevant devices in advance.
```
### Software Requirements

*	[Android Studio 3.X](https://developer.android.com/studio)
*	JDK 1.8 and later 
*	SDK Platform 23 and later
*	Gradle 5.4.1 and later

## Prepare Initial configuration

Use the below link to do initial configuration for the application development
https://developer.huawei.com/consumer/en/codelab/HMSPreparation/index.html#3

## Enable HUAWEI Service(s) in AGC console

Enable the API permission for below kits from Project Settings, Manage APIs and enable the API permission.

*	Account Kit
*	Auth Service Kit


```
**Note:** Some API’s will be enabled by default. If not enable it manually.
```

## Integrating HMS SDK
For official Documentation and more services, please refer below documentation from developer website

1.	[Account Kit](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/introduction-0000001050048870)

2.	[Auth Service](https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/agc-auth-introduction-0000001053732605)

3.	[Scene Kit](https://developer.huawei.com/consumer/en/doc/development/graphics-Guides/service-introduction-0000001050163355)


## Example Code

LoginAuthenticationActivity : To authenticate the user and login to the app using Huawei ID.

MainActivityWithDrawer : Used to hold the fragments with different categories.

TryProduct : It tries the selected product from the list to place it as a 3D object on the camera screen.

Electronics Fragment : It lists the electronics items and display it to the user to select a specific item.

Furniture Fragment : It lists the furniture items and display it to the user to select a specific item.

GalleryFragment : It shows the particular selected product details.

HomeFragment : It's the base fragment that holds the view pager and tab to hold different category fragments.

FragmentCollectionPagerAdapter : It's the FragmentStatePagerAdapter that sets the fragment for viewpager.

ItemAdapter : It loads the items for list of different categories.

ItemModel : It is the model class for selected item.

CountDrawable : It is used to draw the cart loaded with items icon. 

SharedPreferenceUtilClass : Used to load store application data.

AppConstants : Contains the constants used inside the application.

## References

*	[Account Kit](https://developer.huawei.com/consumer/en/doc/development/HMSCore-Guides/introduction-0000001050048870)
*	[Auth Service](https://developer.huawei.com/consumer/en/doc/development/AppGallery-connect-Guides/agc-auth-introduction-0000001053732605)
*	[Scene Kit](https://developer.huawei.com/consumer/en/doc/development/graphics-Guides/service-introduction-0000001050163355)


## License

HMSHomeDecorApp is licensed under the [Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0).