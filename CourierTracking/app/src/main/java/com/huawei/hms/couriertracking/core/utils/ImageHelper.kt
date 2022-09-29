package com.huawei.hms.couriertracking.core.utils

import android.widget.ImageView
import com.squareup.picasso.Picasso

interface IImageHelper {
    fun loadUrl(url:String,imageView: ImageView)
}

class ImageHelper : IImageHelper {

    private val mPicassoInstance = Picasso.get()

    override fun loadUrl(url: String, imageView: ImageView) {
        mPicassoInstance.load(url).into(imageView)
    }
}