package com.huawei.hms.knowmyboard.dtse.activity.intefaces

import androidx.recyclerview.widget.RecyclerView
import com.huawei.hms.site.api.model.Site

interface ItemClickListener {
    fun onItemClicked(vh: RecyclerView.ViewHolder?, item: Site?, pos: Int)
}