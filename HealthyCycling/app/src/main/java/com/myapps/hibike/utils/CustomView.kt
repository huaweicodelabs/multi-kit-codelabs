package com.myapps.hibike.utils

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.myapps.hibike.databinding.CustomViewBinding

class CustomView(context: Context, attrs: AttributeSet): ConstraintLayout(context, attrs) {

    private val binding = CustomViewBinding.inflate(LayoutInflater.from(context), this, true)

    fun initCustomView(img: Int, titleText: String, data: String, dataType: String) = with(binding) {
        image.setImageResource(img)
        title.text = titleText
        tvData.text = data
        tvDataType.text = dataType
    }
}