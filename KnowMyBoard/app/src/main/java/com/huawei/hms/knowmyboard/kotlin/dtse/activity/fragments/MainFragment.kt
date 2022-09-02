package com.huawei.hms.knowmyboard.dtse.activity.fragments

import com.huawei.hms.knowmyboard.dtse.activity.viewmodel.LoginViewModel
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import android.content.Intent
import android.provider.MediaStore
import android.widget.TextView
import android.annotation.SuppressLint
import android.app.Dialog
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.huawei.hms.knowmyboard.dtse.R
import com.huawei.hms.knowmyboard.dtse.activity.ml.TextRecognitionActivity
import com.huawei.hms.knowmyboard.dtse.activity.util.Constants
import com.huawei.hms.knowmyboard.dtse.databinding.FragmentMainFragmentBinding
import java.lang.Exception

class MainFragment : Fragment() {
    var binding: FragmentMainFragmentBinding? = null
    var loginViewModel: LoginViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_main_fragment, container, false)
        loginViewModel = ViewModelProvider(requireActivity()).get(
            LoginViewModel::class.java
        )
        binding!!.loginViewModel = loginViewModel
        binding!!.buttonScan.setOnClickListener { dialog() }
        loginViewModel!!.imagePath.observeForever { bitmap ->
            try {
                binding!!.imageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("TAG", "Error : " + e.message)
            }
        }
        loginViewModel!!.textRecongnized.observeForever { res ->
            binding!!.textLanguage.text = "Language : " + getStringResourceByName(res[0])
            binding!!.textDetected.text = "Detected text : " + res[1]
            binding!!.textTranslated.text = "Translated text : " + res[2]
        }
        return binding!!.root
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        requireActivity().startActivityForResult(intent, Constants.OPEN_CAMERA)
    }

    private fun getStringResourceByName(aString: String): String {
        return try {
            val packageName = requireActivity().packageName
            val resId = resources
                .getIdentifier(aString, "string", packageName)
            if (resId == 0) {
                aString
            } else {
                getString(resId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            aString
        }
    }

    private fun scan() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        requireActivity().startActivityForResult(intent, Constants.OPEN_GALLERY)
    }

    fun dialog() {
        val dialog = Dialog(requireActivity(), R.style.AppTheme)
        dialog.setTitle("Choose")
        dialog.setContentView(R.layout.dialog_pop_up)
        val txt_gallry = dialog.findViewById<View>(R.id.textView_gallery) as TextView
        val txt_camera = dialog.findViewById<View>(R.id.textView_camera) as TextView
        txt_gallry.setOnClickListener {
            dialog.dismiss()
            scan()
        }
        txt_camera.setOnClickListener {
            dialog.dismiss()
            openCamera()
        }
        dialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_fragment_menu, menu)
    }

    @SuppressLint("NonConstantResourceId")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_camera -> requireActivity().startActivityForResult(
                Intent(
                    activity, TextRecognitionActivity::class.java
                ), 1234
            )
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        var TAG = "TAG"
    }
}