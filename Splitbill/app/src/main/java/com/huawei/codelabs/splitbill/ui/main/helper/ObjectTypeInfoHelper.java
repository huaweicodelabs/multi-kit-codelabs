/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
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

import android.util.Log;

import com.huawei.agconnect.cloud.database.CloudDBZoneObject;
import com.huawei.agconnect.cloud.database.ObjectTypeInfo;
import com.huawei.codelabs.splitbill.ui.main.models.Bill;
import com.huawei.codelabs.splitbill.ui.main.models.Comments;
import com.huawei.codelabs.splitbill.ui.main.models.Expense;
import com.huawei.codelabs.splitbill.ui.main.models.ExpenseShare;
import com.huawei.codelabs.splitbill.ui.main.models.Friends;
import com.huawei.codelabs.splitbill.ui.main.models.Group;
import com.huawei.codelabs.splitbill.ui.main.models.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Definition of ObjectType Helper.
 *
 * @since 2020-11-27
 */
public final class ObjectTypeInfoHelper {
    private static final int FORMAT_VERSION = 2;
    private static final int OBJECT_TYPE_VERSION = 32;

    public static ObjectTypeInfo getObjectTypeInfo() {
        ObjectTypeInfo objectTypeInfo = new ObjectTypeInfo();
        objectTypeInfo.setFormatVersion(FORMAT_VERSION);
        objectTypeInfo.setObjectTypeVersion(OBJECT_TYPE_VERSION);
        List<Class<? extends CloudDBZoneObject>> objectTypeList = new ArrayList<>();
        Collections.addAll(objectTypeList, User.class);
        Collections.addAll(objectTypeList, Group.class);
        Collections.addAll(objectTypeList, Friends.class);
        Collections.addAll(objectTypeList, Expense.class);
        Collections.addAll(objectTypeList, Comments.class);
        Collections.addAll(objectTypeList, Bill.class);
        Collections.addAll(objectTypeList, ExpenseShare.class);
        Log.e(ObjectTypeInfoHelper.class.getName(), "getObjectTypeInfo: "+ objectTypeList.size());
        objectTypeInfo.setObjectTypes(objectTypeList);
        return objectTypeInfo;
    }
}
