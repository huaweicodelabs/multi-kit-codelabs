/*
 * Copyright 2022. Huawei Technologies Co., Ltd. All rights reserved.
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
     http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.huawei.codelabs.splitbill.ui.main.adapter;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.codelabs.splitbill.R;
import com.huawei.codelabs.splitbill.databinding.FilesListBinding;
import com.huawei.codelabs.splitbill.databinding.FragmentFileDetailsBinding;
import com.huawei.codelabs.splitbill.ui.main.models.Files;

import java.util.List;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.ViewHolder> {

    private final List<Files> filesList;
    FragmentFileDetailsBinding fragmentFileDetailsBinding;

    public FilesAdapter(List<Files> filesList, FragmentFileDetailsBinding fragmentFileDetailsBinding) {
        this.filesList = filesList;
        this.fragmentFileDetailsBinding=fragmentFileDetailsBinding;
    }

    @NonNull
    @Override
    public FilesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FilesAdapter.ViewHolder(FilesListBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FilesAdapter.ViewHolder holder, int position) {
        try {
            Files device = filesList.get(position);
            holder.tvFilesList.setText(device.getFileName());
        } catch (Exception ignored) {

        }
    }

    @Override
    public int getItemCount() {
        return filesList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView tvFilesList;
        public final RelativeLayout fileListDetail;

        public ViewHolder(@NonNull FilesListBinding deviceListBinding) {
            super(deviceListBinding.getRoot());
            tvFilesList = deviceListBinding.tvFileList;
            fileListDetail =deviceListBinding.fileListDetail;
            fileListDetail.setOnClickListener(this);
        }

        @NonNull
        @Override
        public String toString() {
            return super.toString();
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.fileListDetail) {
                Intent pdfOpenintent = new Intent(Intent.ACTION_VIEW);
                pdfOpenintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                pdfOpenintent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                //pdfOpenintent.setDataAndType(  FileProvider.getUriForFile(fragmentFileDetailsBinding.getRoot().getContext(), BuildConfig.APPLICATION_ID + ".provider",filesList.get(getAdapterPosition()).getFilePath()), "application/pdf");

                pdfOpenintent.setDataAndType(Uri.fromFile(filesList.get(getAdapterPosition()).getFilePath()), "application/pdf");
                try {
                    fragmentFileDetailsBinding.getRoot().getContext().startActivity(pdfOpenintent);
                } catch (ActivityNotFoundException ignored) {

                }
            }
        }
    }
}
