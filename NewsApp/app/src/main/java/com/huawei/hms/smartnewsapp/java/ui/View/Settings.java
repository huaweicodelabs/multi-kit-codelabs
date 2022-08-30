package com.huawei.hms.smartnewsapp.java.ui.View;

/*
 *
 *  * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hms.smartnewsapp.R;
import com.huawei.hms.smartnewsapp.java.util.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to display the user details and settings
 */
public class Settings extends Activity implements AdapterView.OnItemSelectedListener {
    SharedPreferences.Editor editor;
    TextView nameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.language_settings);
        setName();
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        List<String> language = new ArrayList<String>();
        language.add(this.getResources().getString(R.string.english));
        language.add(this.getResources().getString(R.string.chinese));
        language.add(this.getResources().getString(R.string.italian));
        language.add(this.getResources().getString(R.string.german));
        language.add(this.getResources().getString(R.string.spanish));
        language.add(this.getResources().getString(R.string.french));
        ArrayAdapter<String> dataAdapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, language);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    /**
     * to display the user name in about screen
     */
    private void setName() {
        nameText = findViewById(R.id.name);
        SharedPreferences prefs = getSharedPreferences(Constants.MY_PREFS_NAME, MODE_PRIVATE);
        nameText.setText(prefs.getString("name", "User"));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(), "Selected: " + item, Toast.LENGTH_LONG).show();
        editor = getSharedPreferences(Constants.MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("language", item);
        editor.apply();
        editor.commit();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
}
