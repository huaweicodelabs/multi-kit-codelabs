package com.myapps.hibike.ui.home.paymentInfoDialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.myapps.hibike.databinding.DialogPaymentInfoBinding
import com.myapps.hibike.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PaymentInfoDialog: DialogFragment() {

    private lateinit var binding: DialogPaymentInfoBinding

    @Inject
    lateinit var paymentInfoRecyclerAdapter: PaymentInfoRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DialogPaymentInfoBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding){
            ivExit.setOnClickListener {
                dialog?.cancel()
            }
            rvPaymentInfo.adapter = paymentInfoRecyclerAdapter
            paymentInfoRecyclerAdapter.submitList(Constants.paymentInfoList)
        }
    }
}