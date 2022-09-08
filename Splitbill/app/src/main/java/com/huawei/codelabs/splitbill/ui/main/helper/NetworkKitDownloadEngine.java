/* Copyright 2022. Huawei Technologies Co., Ltd. All rights reserved.
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
package com.huawei.codelabs.splitbill.ui.main.helper;
import android.content.Context;
import androidx.lifecycle.MutableLiveData;
import com.huawei.hms.network.file.api.Result;

public abstract class NetworkKitDownloadEngine {
    public Context context;
    MutableLiveData<Result> downloadResult;

    public NetworkKitDownloadEngine(Context context) {
        this.context = context;
        initManager();
    }

    public NetworkKitDownloadEngine() {

    }

    public void checkResult(Result result) {
        downloadResult = new MutableLiveData<>();
        downloadResult.setValue(result);
    }
    protected abstract void initManager();
    public abstract void download(String Url, String name);
}
