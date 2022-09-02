package com.huawei.hms.knowmyboard.dtse.activity.util

import android.content.Context
import android.content.SharedPreferences
import com.huawei.hms.knowmyboard.dtse.activity.util.MySharedPreferences

class MySharedPreferences(context: Context) {
    private val myPreferences: SharedPreferences
    private val myEditor: SharedPreferences.Editor
    fun putStringValue(key: String?, value: String?) {
        myEditor.putString(key, value)
        myEditor.commit()
    }

    fun getStringValue(key: String?): String? {
        return myPreferences.getString(key, Constants.USER_NAME)
    }

    companion object {
        const val TAG = "MySharedPreferences"
        private var mySharedPreferences: MySharedPreferences? = null
        @JvmStatic
        fun getInstance(context: Context): MySharedPreferences? {
            if (mySharedPreferences == null) {
                synchronized(MySharedPreferences::class.java) {
                    if (mySharedPreferences == null) {
                        mySharedPreferences = MySharedPreferences(context)
                    }
                }
            }
            return mySharedPreferences
        }
    }

    init {
        myPreferences = context.getSharedPreferences(TAG, Context.MODE_PRIVATE)
        myEditor = myPreferences.edit()
    }
}