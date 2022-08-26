/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
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

package com.huawei.hms.urbanhomeservices.java.listener;

import com.huawei.hms.urbanhomeservices.java.clouddb.ServiceType;

/**
 * This interface is used to delete/update ServiceCategory list(ManageServiceActivity)
 * Based on User action , we will update, edit or delete data.
 *
 * @author: Huawei
 * @since : 20-01-2021
 */
public interface ServiceUpdateListener<T> {
    void deleteService(ServiceType listObject);

    void editService(ServiceType listObject);
}
