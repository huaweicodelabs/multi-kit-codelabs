package com.myapps.hibike.utils

interface IPushNotifServiceListener<T> {
    fun onSuccess(successResult: T)
    fun onError(throwable: Throwable)
}