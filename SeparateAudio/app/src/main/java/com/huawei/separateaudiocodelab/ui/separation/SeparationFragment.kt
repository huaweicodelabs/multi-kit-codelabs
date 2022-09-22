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
package com.huawei.separateaudiocodelab.ui.separation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.huawei.hms.audioeditor.sdk.AudioSeparationCallBack
import com.huawei.hms.audioeditor.sdk.HAEAudioSeparationFile
import com.huawei.hms.audioeditor.sdk.bean.SeparationBean
import com.huawei.hms.audioeditor.sdk.materials.network.SeparationCloudCallBack
import com.huawei.separateaudiocodelab.databinding.FragmentSeparationBinding
import com.huawei.separateaudiocodelab.model.Instrument
import com.huawei.separateaudiocodelab.util.FileUtils
import java.io.File
import kotlin.streams.toList


class SeparationFragment : Fragment() {

    private val args: SeparationFragmentArgs by navArgs()
    private val REQUEST_TAKE_GALLERY_AUDIO = 1003
    private val REQUEST_SELECT_OUTPUT_FOLDER = 1002
    private var instruments: MutableList<Instrument> = arrayListOf()
    private var selectedInstruments: MutableList<Instrument> = arrayListOf()
    private lateinit var filePath: String
    private lateinit var folderPath: String
    private lateinit var fileName: String
    private lateinit var rvAdapter: RvAdapter
    private lateinit var haeAudioSeparationFile: HAEAudioSeparationFile
    private lateinit var binding: FragmentSeparationBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        haeAudioSeparationFile = HAEAudioSeparationFile()
        setArgs()
        if(filePath==""){
            val intent = Intent()
            intent.type = "audio/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Select Music"),
                REQUEST_TAKE_GALLERY_AUDIO
            )
        }
        if(folderPath==""){
            val exportPath = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                    .toString() + File.separator + "SeparateAudioCodelab" + File.separator
            )
            if(!exportPath.exists()) exportPath.mkdir()
            folderPath = exportPath.absolutePath
        }
        getInstruments()
        initRecyclerView()
        initListeners()
        initViews()
    }

    private fun setArgs() {
        filePath = args.audioPath
        folderPath = args.folderPath
        fileName = args.fileName
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSeparationBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun initViews() {
        binding.txtOutputFolderPath.text = folderPath
    }

    private fun initListeners() {
        binding.btnSeparate.setOnClickListener {
            if (selectedInstruments.isNotEmpty()) {
                separateAudio()
                binding.btnSeparate.isClickable = false
            } else {
                Toast.makeText(context, "Please select category", Toast.LENGTH_LONG).show()
            }
        }
        binding.txtChangeOutputPath.setOnClickListener {
            val i = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            i.addCategory(Intent.CATEGORY_DEFAULT)
            startActivityForResult(
                Intent.createChooser(i, "Choose directory"),
                REQUEST_SELECT_OUTPUT_FOLDER
            )
        }
        binding.checkboxSelectAll.setOnCheckedChangeListener { _, b ->
            rvAdapter.toggleSelectAll(b)
            selectedInstruments.removeAll(selectedInstruments)
            if(b){
                selectedInstruments.addAll(instruments)
            }
            binding.txtSelectedInstruments.text =
                selectedInstruments.stream().map { it.desc }.toList().joinToString()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TAKE_GALLERY_AUDIO && resultCode == Activity.RESULT_OK) {
            filePath = FileUtils.getRealPath(context!!,data?.data!!).toString()
            fileName = FileUtils.getFileNameWithoutExtension(filePath)
        }
        if (requestCode == REQUEST_SELECT_OUTPUT_FOLDER && resultCode == Activity.RESULT_OK) {
            val docUri: Uri = DocumentsContract.buildDocumentUriUsingTree(
                data?.data,
                DocumentsContract.getTreeDocumentId(data?.data)
            )
            folderPath = context?.let { FileUtils.getRealPath(it, docUri) }.toString()
            binding.txtOutputFolderPath.text = folderPath
        }
    }

    private fun initRecyclerView() {
        val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(context)
        binding.rvInstruments.layoutManager = layoutManager
        rvAdapter = RvAdapter(instruments) { instrument, isChecked ->
            if (isChecked) {
                selectedInstruments.add(instrument)
            } else {
                selectedInstruments.remove(instrument)
            }
            binding.txtSelectedInstruments.text =
                selectedInstruments.stream().map { it.desc }.toList().joinToString()
        }
        binding.rvInstruments.adapter = rvAdapter
    }

    private fun getInstruments() {
        haeAudioSeparationFile.getInstruments(object :
            SeparationCloudCallBack<List<SeparationBean?>?> {
            override fun onFinish(response: List<SeparationBean>) {
                for (separationBean in response) {
                    instruments.add(Instrument(separationBean.instrument, separationBean.desc,""))
                }
                rvAdapter.setItems(instruments)
            }

            override fun onError(errorCode: Int) {
                //Runs when when error occurred
            }
        })
    }

    private fun separateAudio() {
        haeAudioSeparationFile.setInstruments(selectedInstruments.map { it.name })
        binding.layoutSeparationFragment.visibility = View.VISIBLE
        binding.layoutSeparationFragment.bringToFront()
        val instrumentsToCompose = arrayListOf<Instrument>()
        var doneTaskCount = 0
        binding.txtSeparationProcess.text = "${doneTaskCount}/${selectedInstruments.size}"

        haeAudioSeparationFile.startSeparationTasks(
            filePath,
            folderPath,
            fileName,
            object : AudioSeparationCallBack {
                override fun onResult(separationBean: SeparationBean) {
                    activity?.runOnUiThread {
                        binding.txtSeparationProcess.text =
                            "${++doneTaskCount}/${selectedInstruments.size}"
                    }
                    separationBean.apply { instrumentsToCompose.add(Instrument(this.instrument,this.desc,this.outAudioPath)) }
                }

                override fun onFinish(separationBeans: List<SeparationBean>) {
                    activity?.runOnUiThread {
                        binding.layoutSeparationFragment.visibility = View.GONE
                        binding.btnSeparate.isClickable = true
                        val action = SeparationFragmentDirections.actionSeparationFragmentToComposeFragment(instrumentsToCompose.toList().toTypedArray(),folderPath)
                        findNavController().navigate(action)
                    }
                }

                override fun onFail(errorCode: Int) {
                    //Runs when process failed
                }
                override fun onCancel() {
                    //Runs when process canceled
                }
            })

    }
}