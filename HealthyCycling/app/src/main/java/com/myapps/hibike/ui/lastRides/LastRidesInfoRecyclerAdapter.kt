package com.myapps.hibike.ui.lastRides

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.myapps.hibike.R
import com.myapps.hibike.data.model.RideModel
import com.myapps.hibike.databinding.ItemLastRideBinding
import com.myapps.hibike.utils.extension.show
import java.text.DecimalFormat
import javax.inject.Inject

class LastRidesInfoRecyclerAdapter @Inject constructor()
    : ListAdapter<RideModel, LastRidesInfoRecyclerAdapter.ViewHolder>(DiffCallback()) {

    private var unpaidRideId: String? = null

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val binding = ItemLastRideBinding.bind(itemView)

        @SuppressLint("SetTextI18n")
        fun bind(rideInfo: RideModel) {
            with(binding) {
                if (unpaidRideId == rideInfo.docId) {
                    ivAttention.show()
                    tvAttention.show()
                }
                tvDate.text = rideInfo.date
                tvAmount.text = rideInfo.amount
                if(rideInfo.minute == 0L && rideInfo.second != 0L){
                    tvDuration.text = "${rideInfo.hour} h 1 min"
                }
                else tvDuration.text = "${rideInfo.hour} h ${rideInfo.minute} min"
                tvDistance.text = "${DecimalFormat("##.###").format(rideInfo.distance)}  km"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_last_ride, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setUnpaidRideId(id: String){
        unpaidRideId = id
    }

}

class DiffCallback : DiffUtil.ItemCallback<RideModel>() {
    override fun areItemsTheSame(oldItem: RideModel, newItem: RideModel): Boolean {
        return oldItem.bikeId == newItem.bikeId
    }

    override fun areContentsTheSame(oldItem: RideModel, newItem: RideModel): Boolean {
        return oldItem == newItem
    }
}