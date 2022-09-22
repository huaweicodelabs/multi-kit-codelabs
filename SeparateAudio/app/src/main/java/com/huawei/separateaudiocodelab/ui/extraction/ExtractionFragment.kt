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
package com.huawei.separateaudiocodelab.ui.extraction

import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.huawei.hms.audioeditor.sdk.AudioExtractCallBack
import com.huawei.hms.audioeditor.sdk.HAEAudioExpansion
import com.huawei.separateaudiocodelab.R
import com.huawei.separateaudiocodelab.databinding.DialogExtractionCompleteBinding
import com.huawei.separateaudiocodelab.databinding.FragmentExtractionBinding
import com.huawei.separateaudiocodelab.util.FileUtils
import java.io.File


class ExtractionFragment : Fragment() {

    private lateinit var binding: FragmentExtractionBinding
    private val REQUEST_TAKE_GALLERY_VIDEO = 1001
    private var videoPath = ""
    private lateinit var exportPath: File

    private fun initListener() {
        binding.imgVideoEdit.setOnClickListener {
            val intent = Intent()
            intent.type = "video/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Select Video"),
                REQUEST_TAKE_GALLERY_VIDEO
            )
        }

        binding.btnExtraction.setOnClickListener {
            exportPath = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                    .toString() + File.separator + "SeparateAudioCodelab" + File.separator
            )
            if(!exportPath.exists()) exportPath.mkdir()

            if(videoPath!=""){
                HAEAudioExpansion.getInstance().extractAudio(
                    context,
                    videoPath,
                    exportPath.toString(),
                    binding.txtVideoName.text.toString(),
                    object : AudioExtractCallBack {
                        override fun onSuccess(audioPath: String) {
                            showDialog()
                        }

                        override fun onProgress(progress: Int) {
                            //Runs on progress
                        }

                        override fun onFail(errCode: Int) {
                            if(errCode==1006){
                                Toast.makeText(context,"File already exist",Toast.LENGTH_LONG).show()
                            }
                        }

                        override fun onCancel() {
                            //Runs on when process canceled
                        }
                    }) }
            }

        binding.btnChangeVideoName.setOnClickListener {
            if(binding.txtVideoName.isEnabled){
                it.setBackgroundResource(R.drawable.editing)
                binding.txtVideoName.isEnabled = false
            }
            else{
                it.setBackgroundResource(R.drawable.ic_baseline_check_box_24)
                binding.txtVideoName.isEnabled = true
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding = FragmentExtractionBinding.inflate(inflater,container,false)
        initListener()
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == REQUEST_TAKE_GALLERY_VIDEO && resultCode == RESULT_OK){
            videoPath = FileUtils.getRealPath(context!!,data?.data!!).toString()
            binding.txtVideoName.setText(FileUtils.getFileNameWithoutExtension(videoPath))
            binding.imgVideoEdit.setBackgroundResource(R.drawable.video_folder)
        }
    }

    private fun showDialog() {
        activity?.runOnUiThread {
            val dialogBuilder = Dialog(context!!,R.style.DialogStyle)
            val dialogBinding = DialogExtractionCompleteBinding.inflate(layoutInflater)
            dialogBuilder.setContentView(dialogBinding.root)
            dialogBinding.btnExtractionSelect.setOnClickListener {
                dialogBuilder.dismiss()
                val action = ExtractionFragmentDirections.actionExtractionFragmentToSeparationFragment(
                    exportPath.toString()+File.separator+binding.txtVideoName.text.toString()+".wav",
                    exportPath.toString(),
                    binding.txtVideoName.text.toString()
                )
                findNavController().navigate(action)

            }
            dialogBinding.btnExtractionDone.setOnClickListener {
                dialogBuilder.dismiss()
                findNavController().navigate(R.id.action_extractionFragment_to_homeFragment)
            }
            dialogBuilder.create()
            dialogBuilder.show()
        }
    }
}