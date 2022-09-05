/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2022. All rights reserved.
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
 */
package com.huawei.schooldairy.ui.activities;

import android.app.ProgressDialog;
import android.util.Log;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.huawei.schooldairy.R;

/**
 * Abstract base activity class with common functions for all activity classes
 * @author: Huawei
 * @since: 25-05-2021
 */
public abstract class AbstractBaseActivity extends AppCompatActivity {

    private ProgressDialog mProgressDialog;

    /**
     * To hide soft keyboard anywhere within the application
     */
    protected void hideKeyboard() {
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (getCurrentFocus() != null)
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            Log.e("MultiBackStack", "Failed to add fragment to back stack", e);
        }
    }

    /**
     * To make back arrow visible on Action Button
     */
    protected void showBackArrow() {
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowHomeEnabled(true);
        }
    }

    /**
     * To make back arrow clickable
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * To show progress dialog without title
     * @param message - Display message
     */
    public void showProgressDialog(@NonNull String message) {
        showProgressDialog(null, message);
    }

    /**
     * To show progress dialog with title
     * @param title - Progress title
     * @param message - Display message
     */
    public void showProgressDialog(String title, @NonNull String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            if (title != null)
                mProgressDialog.setTitle(title);
            //mProgressDialog.setIcon(R.mipmap.ic_launcher);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setIndeterminate(true);
        }
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.setMessage(message);
            mProgressDialog.show();
        }
    }

    /**
     * To hide progress dialog
     */
    public void hideDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    /**
     * Click listener for AlertDialog's okay Button
     */
    public interface OnOkayListener {
        void onClickOkay();
    }

    /**
     * To show alert dialog
     * @param msg - display message
     * @param listener - okay button listener
     */
    protected void showAlertDialog(String msg, OnOkayListener listener) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(null);
        dialogBuilder.setIcon(R.mipmap.ic_launcher);
        dialogBuilder.setMessage(msg);
        dialogBuilder.setPositiveButton(getString(R.string.dialog_ok_btn), (dialog, which) -> {
            if (listener != null)
                listener.onClickOkay();
            dialog.cancel();
        });
        dialogBuilder.setCancelable(listener != null);
        dialogBuilder.show();
    }

    /**
     * To show alert dialog
     * @param msg - display message
     */
    protected void showAlertDialog(String msg) {
        showAlertDialog(msg, null);
    }

    /**
     * To show toast
     * @param mToastMsg - display message
     */
    protected void showToast(String mToastMsg) {
        Toast.makeText(this, mToastMsg, Toast.LENGTH_LONG).show();
    }
}