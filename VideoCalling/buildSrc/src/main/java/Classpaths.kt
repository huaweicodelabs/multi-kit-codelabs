object Classpaths {
    const val buildGradle = "com.android.tools.build:gradle:${Versions.gradlePluginVersion}"
    const val kotlinGradle = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlinVersion}"
    const val hiltGradle = "com.google.dagger:hilt-android-gradle-plugin:${Versions.hiltVersion}"
    const val safeArgsGradle =
        "androidx.navigation:navigation-safe-args-gradle-plugin:${Versions.navigationVersion}"
    const val agconnectGradle = "com.huawei.agconnect:agcp:${Versions.agcVersion}"
}