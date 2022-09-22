/*
 * Copyright 2022. Explore in HMS. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.huawei.separateaudiocodelab.ui.compose

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.huawei.separateaudiocodelab.R
import com.huawei.separateaudiocodelab.databinding.ItemPlayableInstrumentBinding
import com.huawei.separateaudiocodelab.model.Instrument

class ComposeRvAdapter(
    private var instrumentList: List<Instrument>,
    private val onCheckedChangedListener: (Instrument, Boolean) -> Unit,
    private val onPlayClickListener: (Instrument, Boolean) -> Unit
) : RecyclerView.Adapter<ComposeRvAdapter.ViewHolder>() {

    private var isPlaying = false

    inner class ViewHolder(private val binding: ItemPlayableInstrumentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(instrument: Instrument) {
            binding.txtInstrumentName.text = instrument.name
            binding.checkboxInstrument.setOnClickListener {
                onCheckedChangedListener(
                    instrument,
                    binding.checkboxInstrument.isChecked
                )
            }
            binding.animPlay.setAnimation(R.raw.anim)
            binding.animPlay.setOnClickListener {
                it as LottieAnimationView
                if(!isPlaying){
                    it.playAnimation()
                }
                else{
                    it.cancelAnimation()
                }
                toggleAnimation(!isPlaying)
                onPlayClickListener(instrument,isPlaying)
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPlayableInstrumentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(instrumentList[position]) {
                holder.bind(this)
            }
        }
    }

    override fun getItemCount(): Int {
        return instrumentList.size
    }

    fun setItems(newList: List<Instrument>) {
        instrumentList = newList
        notifyDataSetChanged()
    }

    fun toggleAnimation(isPlaying:Boolean){
        this.isPlaying = isPlaying
        notifyDataSetChanged()
    }
}
