package com.huawei.hms.couriertracking.core.service

import com.huawei.hms.push.HmsMessageService
import com.huawei.hms.push.RemoteMessage

class PushService : HmsMessageService() {
    override fun onNewToken(token: String?) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
    }
}