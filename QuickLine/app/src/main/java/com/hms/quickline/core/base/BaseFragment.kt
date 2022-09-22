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

package com.hms.quickline.core.base

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment

open class BaseFragment : Fragment {
    lateinit var mFragmentNavigation: FragmentNavigation
    var baseContext: Context? = null

    constructor() : super()
    constructor(contentLayoutId: Int) : super(contentLayoutId)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        baseContext = context
        mFragmentNavigation = context as FragmentNavigation
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.v("LifeCycle", "${name()} onViewCreated")
    }

    private fun name(): String {
        return this.javaClass.simpleName
    }

    interface FragmentNavigation {
        fun setBottomBarVisibility(isVisible: Boolean = true)
        fun giveAction(action: Int)
        fun navigateUP()
        fun navigateTop()
    }
}