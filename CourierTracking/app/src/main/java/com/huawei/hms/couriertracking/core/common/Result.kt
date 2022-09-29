package com.huawei.hms.couriertracking.core.common

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val error: String? = null) : Result<Nothing>()
}