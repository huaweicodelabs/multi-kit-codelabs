package com.myapps.hibike.ui.home.paymentInfoDialog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.myapps.hibike.R
import com.myapps.hibike.databinding.ItemDialogBinding
import javax.inject.Inject

class PaymentInfoRecyclerAdapter @Inject constructor()
    : ListAdapter<String, PaymentInfoRecyclerAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemDialogBinding.bind(itemView)
        fun bind(paymentInfo: String) {
            with(binding) {
                val info = paymentInfo.split(",")
                tvMinute.text = info[0]
                tvAmount.text = info[1]
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dialog, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}

class DiffCallback : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }
}