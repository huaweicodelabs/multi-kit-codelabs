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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.huawei.hms.audioeditor.common.Constants
import com.huawei.hms.audioeditor.sdk.HuaweiAudioEditor
import com.huawei.hms.audioeditor.sdk.HuaweiAudioEditor.ExportAudioCallback
import com.huawei.hms.audioeditor.sdk.bean.HAEAudioProperty
import com.huawei.separateaudiocodelab.R
import com.huawei.separateaudiocodelab.databinding.FragmentComposeBinding
import com.huawei.separateaudiocodelab.model.Instrument
import kotlinx.coroutines.*
import java.io.File


class ComposeFragment : Fragment() {

    private val args: ComposeFragmentArgs by navArgs()
    private lateinit var binding: FragmentComposeBinding
    private lateinit var exportPath: String
    private lateinit var rvAdapter: ComposeRvAdapter
    private var instruments = arrayOf<Instrument>()
    private var selectedInstruments: MutableList<Instrument> = arrayListOf()
    private var fileName = "my_compose"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding = FragmentComposeBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
        initListeners()
        setArgs()
        initUI()
    }

    private fun initUI() {
        binding.txtComposeName.setText(fileName)
    }

    private fun initListeners() {
        binding.btnCompose.setOnClickListener {
            if(selectedInstruments.isNotEmpty()){

                activity?.runOnUiThread { binding.processCompose.visibility = View.VISIBLE
                    binding.txtSelectInstruments.text = "Composing" }
                CoroutineScope(Dispatchers.IO).launch { composeSounds() }
            }
            else{
                Toast.makeText(context, "Please select sound", Toast.LENGTH_LONG).show()
            }
        }
        binding.btnChangeSoundName.setOnClickListener {
            if (binding.txtComposeName.isEnabled) {
                it.setBackgroundResource(R.drawable.editing)
                binding.txtComposeName.isEnabled = false
            } else {
                it.setBackgroundResource(R.drawable.ic_baseline_check_box_24)
                binding.txtComposeName.isEnabled = true
            }
        }
        binding.btnHome.setOnClickListener {
            findNavController().navigate(R.id.action_composeFragment_to_homeFragment)
        }
    }

    private fun initRecyclerView() {
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)
        val mEditor = HuaweiAudioEditor.create(context)
        mEditor.initEnvironment()
        val onPlayClickListener: (Instrument, Boolean) -> Unit  = { instrument: Instrument, isPlaying: Boolean ->
            val mTimeLine = mEditor.timeLine
            mTimeLine.appendAudioLane().appendAudioAsset(instrument.filePath,mTimeLine.currentTime)
            if(isPlaying){
                mEditor.playTimeLine(mTimeLine.startTime, mTimeLine.endTime)
            }
            else{
                mEditor.pauseTimeLine()
            }
        }
        val onCheckBoxClickListener: (Instrument, Boolean) -> Unit = { instrument:Instrument, isChecked:Boolean ->
            if (isChecked) {
                selectedInstruments.add(instrument)
            } else {
                selectedInstruments.remove(instrument)
            }
        }
        binding.rvInstruments.layoutManager = layoutManager
        rvAdapter = ComposeRvAdapter(instruments.toList(),onCheckBoxClickListener,onPlayClickListener)
        binding.rvInstruments.adapter = rvAdapter


    }

    private fun setArgs(){
        instruments = args.instrumentsToCompose
        rvAdapter.setItems(instruments.toList())
        exportPath = args.exportPath
    }

    private fun composeSounds(){
        fileName = binding.txtComposeName.text.toString()
        val mEditor = HuaweiAudioEditor.create(context)
        mEditor.initEnvironment()
        val mTimeLine = mEditor.timeLine
        for(instrument in selectedInstruments){
            mTimeLine.appendAudioLane().appendAudioAsset(instrument.filePath,mTimeLine.currentTime)
        }
        val path = exportPath+ File.separator+ fileName + ".mp3"
        val exportAudioCallback: ExportAudioCallback = object : ExportAudioCallback {
            override fun onCompileProgress(time: Long, duration: Long) {
                //Runs on progress
            }
            override fun onCompileFinished() {
                activity?.runOnUiThread{
                    binding.processCompose.visibility = View.INVISIBLE
                    binding.txtSelectInstruments.text = getString(R.string.select_instruments)
                    Toast.makeText(context,"DONE",Toast.LENGTH_LONG)
                }
            }
            override fun onCompileFailed(errCode: Int, errorMsg: String) {
                //Runs when process failed
            }
        }
        HuaweiAudioEditor.getInstance().setExportAudioCallback(exportAudioCallback)
        val audioProperty = HAEAudioProperty()
        audioProperty.encodeFormat = Constants.AV_CODEC_ID_MP3
        audioProperty.sampleRate = Constants.SAMPLE_RATE_44100
        audioProperty.channels = 2
        HuaweiAudioEditor.getInstance().exportAudio(audioProperty, path)
    }
}