package com.myapps.hibike.data.model

import com.google.gson.annotations.SerializedName

class NotifMessageBody(
    @field:SerializedName("validate_only") var validateOnly: Boolean,
    @field:SerializedName("message") var message: Message?)
{


    class Builder(
        private var title: String,
        private var body: String,
        private var pushToken: Array<String?>
    ) {

        fun build(): NotifMessageBody {
            val clickAction = ClickAction(3)
            val androidNotification = AndroidNotification(title, body, clickAction)
            val androidConfig = AndroidConfig(androidNotification)
            val notification = Notification(title, body)
            val message = Message(notification, androidConfig, pushToken)
            return NotifMessageBody(false, message)
        }
    }

    class Message(
        @field:SerializedName("notification") var notification: Notification,
        @field:SerializedName("android") var android: AndroidConfig,
        @field:SerializedName("token") var token: Array<String?>
    )

    class Notification(
        @field:SerializedName("title") var title: String,
        @field:SerializedName("body") var body: String
    )

    class AndroidConfig(
        @field:SerializedName("notification") var notification: AndroidNotification
    )

    class AndroidNotification(
        @field:SerializedName("title") var title: String,
        @field:SerializedName("body") var body: String,
        @field:SerializedName("click_action") var clickAction: ClickAction
    )

    class ClickAction {
        @SerializedName("type")
        var type: Int

        @SerializedName("intent")
        var intent: String? = null

        constructor(type: Int) {
            this.type = type
        }

        constructor(type: Int, intent: String?) {
            this.type = type
            this.intent = intent
        }
    }
}