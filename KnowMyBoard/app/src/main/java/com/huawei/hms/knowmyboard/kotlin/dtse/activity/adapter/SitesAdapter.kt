package com.huawei.hms.knowmyboard.dtse.activity.adapter

import android.content.Context
import com.huawei.hms.site.api.model.Site
import com.huawei.hms.knowmyboard.dtse.activity.intefaces.ItemClickListener
import android.view.ViewGroup
import com.huawei.hms.knowmyboard.dtse.activity.adapter.SitesAdapter.SitesViewHolder
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.huawei.hms.knowmyboard.dtse.R
import java.util.ArrayList

class SitesAdapter(
    sites: ArrayList<Site>,
    mContext: Context?,
    itemClickListener: ItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var sites = ArrayList<Site>()
    private var itemClickListener: ItemClickListener
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SitesViewHolder {
        val rootView =
            LayoutInflater.from(parent.context).inflate(R.layout.site_item, parent, false)
        return SitesViewHolder(rootView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val site = sites[position]
        val viewHolder = holder as SitesViewHolder
        viewHolder.siteName.text = site.name
        viewHolder.siteAddress.text = site.formatAddress
        holder.itemView.setOnClickListener {
            itemClickListener.onItemClicked(
                viewHolder,
                site,
                position
            )
        }
    }

    override fun getItemCount(): Int {
        return sites.size
    }

    inner class SitesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var siteName: TextView = itemView.findViewById(R.id.name)
        var siteAddress: TextView = itemView.findViewById(R.id.address)

    }

    init {
        this.sites = sites
        this.itemClickListener = itemClickListener
    }
}