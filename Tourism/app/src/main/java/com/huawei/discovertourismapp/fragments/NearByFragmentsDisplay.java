package com.huawei.discovertourismapp.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.huawei.discovertourismapp.utils.Util;
import com.huawei.discovertourismapp.viewmodel.PageViewModel;

public class NearByFragmentsDisplay extends Fragment {
    public static NearByFragmentsDisplay newInstance() {
        return new NearByFragmentsDisplay();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //pageViewModel = ViewModelProviders.of((MainActivity)getActivity()).get(NearByFragment.class);
      //  util=new Util();
      //  initObserver();

        //pageViewModel = new ViewModelProvider(requireActivity()).get(PageViewModel.class);
    }
}
