package com.huawei.discovertourismapp.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.discovertourismapp.R;
import com.huawei.discovertourismapp.model.NearByData;
import com.huawei.discovertourismapp.viewholder.NearByLocationViewHolder;
import com.huawei.hms.site.api.model.Site;

import java.util.ArrayList;
import java.util.List;

public class NearByAdapter extends RecyclerView.Adapter<NearByLocationViewHolder> {

    public Context context;
    public List<Site> list;

    public NearByAdapter(Context context, List<Site> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public NearByLocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemList = layoutInflater.inflate(R.layout.nearby_location_item, parent, false);
        return new NearByLocationViewHolder(itemList);
    }

    @Override
    public void onBindViewHolder(@NonNull NearByLocationViewHolder holder, int position) {

        holder.mName.setText(""+list.get(position).getName());
        holder.address.setText(""+list.get(position).getFormatAddress());
    }


    @Override
    public int getItemCount() {
        if (list != null) {
            return list.size();
        } else {
            return 0;
        }
    }

    public interface ContactAdapterListener {
        void setContactData(NearByData user);
    }


}
