package com.huawei.hms.knowmyboard.dtse.activity.app

import com.huawei.hms.knowmyboard.dtse.activity.app.MyApplication
import com.huawei.hms.analytics.HiAnalytics
import com.huawei.hms.maps.MapsInitializer
import android.app.Activity
import android.app.Application
import com.huawei.hms.analytics.HiAnalyticsInstance
import com.huawei.hms.knowmyboard.dtse.activity.util.Constants
import com.huawei.hms.mlsdk.common.MLApplication

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MLApplication.initialize(this)
        //initialize Analytics Kit
        instance = HiAnalytics.getInstance(this)
        MLApplication.getInstance().apiKey =
            Constants.API_KEY
        MapsInitializer.setApiKey(Constants.API_KEY)
    }

    companion object {
        var activity: Activity? = null
        var instance: HiAnalyticsInstance? = null
    }
}