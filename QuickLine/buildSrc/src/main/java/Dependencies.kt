object Dependencies {
    const val appCompat = "androidx.appcompat:appcompat:${Versions.appCompatVersion}"
    const val kotlin = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlinVersion}"
    const val materialDesign = "com.google.android.material:material:${Versions.materialVersion}"
    const val constraintLayout =
        "androidx.constraintlayout:constraintlayout:${Versions.constraintLayoutVersion}"
    const val coreKtx = "androidx.core:core-ktx:${Versions.coreKtxVersion}"

    const val jUnit = "junit:junit:${Versions.jUnitVersion}"
    const val testJUnit = "androidx.test.ext:junit:${Versions.testJunitVersion}"
    const val espressoCore = "androidx.test.espresso:espresso-core:${Versions.espressoCoreVersion}"

    const val navigationFragment =
        "androidx.navigation:navigation-fragment-ktx:${Versions.navigationVersion}"
    const val navigationUi = "androidx.navigation:navigation-ui-ktx:${Versions.navigationVersion}"

    const val glide = "com.github.bumptech.glide:glide:${Versions.glideVersion}"
    const val glideKapt = "com.github.bumptech.glide:compiler:${Versions.glideVersion}"

    const val hiltAndroid = "com.google.dagger:hilt-android:${Versions.hiltVersion}"
    const val hiltKapt = "com.google.dagger:hilt-android-compiler:${Versions.hiltVersion}"

    const val lifecycleCommon =
        "androidx.lifecycle:lifecycle-common-java8:${Versions.lifecycleVersion}"

    const val sdp = "com.intuit.sdp:sdp-android:${Versions.sdpVersion}"
    const val ssp = "com.intuit.ssp:ssp-android:${Versions.sspVersion}"

    //AGConnect
    const val agconnect = "com.huawei.agconnect:agconnect-core:${Versions.agcVersion}"

    //CloudDB
    const val cloudDB = "com.huawei.agconnect:agconnect-cloud-database:${Versions.cloudDBVersion}"

    //Auth
    const val auth = "com.huawei.agconnect:agconnect-auth:${Versions.authVersion}"
    const val huaweiAuth =
        "com.huawei.agconnect:agconnect-auth-huawei:${Versions.huaweiAuthVersion}"

    //WebRTC
    const val webRTC = "org.webrtc:google-webrtc:${Versions.webRtcVersion}"

    //Wireless
    const val wirelessKit = "com.huawei.hms:wireless:${Versions.wirelessKitVersion}"

    //Lottie
    const val lottie = "com.airbnb.android:lottie:${Versions.lottieVersion}"

    //CrashService
    const val crashService = "com.huawei.agconnect:agconnect-crash:${Versions.crashServiceVersion}"
    const val apmAgc = "com.huawei.agconnect:agconnect-apms:${Versions.apmVersion}"
    //Face Detection
    const val livenessDetection = "com.huawei.hms:ml-computer-vision-livenessdetection:${Versions.livenessDetectionVersion}"

    //SafetyDetect
    const val safetyDetect = "com.huawei.hms:safetydetect:${Versions.safetyDetectVersion}"

    //Analytics Kit
    const val analyticsKit = "com.huawei.hms:hianalytics:${Versions.analyticsVersion}"

    //Push Kit
    const val pushKit = "com.huawei.hms:push:${Versions.pushKitVersion}"

}