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
package com.huawei.hms.smartnewsapp.kotlin.ui.View

import android.app.Activity
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import com.huawei.hms.smartnewsapp.R
import com.huawei.hms.smartnewsapp.kotlin.util.Constants
import java.util.*

/**
 * Activity to display the user details and settings
 */
class Settings : Activity(), OnItemSelectedListener {
    lateinit var editor: SharedPreferences.Editor
    lateinit var nameText: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.language_settings)
        setName()
        val spinner = findViewById<View>(R.id.spinner) as Spinner
        spinner.onItemSelectedListener = this
        val language: MutableList<String> = ArrayList()
        language.add(this.resources.getString(R.string.english))
        language.add(this.resources.getString(R.string.chinese))
        language.add(this.resources.getString(R.string.italian))
        language.add(this.resources.getString(R.string.german))
        language.add(this.resources.getString(R.string.spanish))
        language.add(this.resources.getString(R.string.french))
        val dataAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, language)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = dataAdapter
    }

    /**
     * to display the user name in about screen
     */
    private fun setName() {
        nameText = findViewById(R.id.name)
        val prefs = getSharedPreferences(Constants.MY_PREFS_NAME, MODE_PRIVATE)
        nameText.setText(prefs.getString("name", "User"))
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val item = parent.getItemAtPosition(position).toString()
        Toast.makeText(parent.context, "Selected: $item", Toast.LENGTH_LONG).show()
        editor = getSharedPreferences(Constants.MY_PREFS_NAME, MODE_PRIVATE).edit()
        editor.putString("language", item)
        editor.apply()
        editor.commit()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}
}