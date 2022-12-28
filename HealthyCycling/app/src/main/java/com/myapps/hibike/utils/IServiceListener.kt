package com.myapps.hibike.utils

interface IServiceListener<T> {
    fun onSuccess(successResult: T)
    fun onError(exception: Exception)
}