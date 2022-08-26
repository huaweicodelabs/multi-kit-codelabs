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

package com.huawei.hms.urbanhomeservices.java.clouddb;

import com.huawei.agconnect.cloud.database.CloudDBZoneObject;
import com.huawei.agconnect.cloud.database.annotations.DefaultValue;
import com.huawei.agconnect.cloud.database.annotations.PrimaryKeys;

/**
 * Definition of ObjectType ServiceCategory.
 *
 * @author : Huawei
 * @since : 2021-02-08
 */

@PrimaryKeys({"service_id"})
public class ServiceCategory extends CloudDBZoneObject {
    private Integer service_id;
    private String service_name;
    private String service_category;
    @DefaultValue(booleanValue = true)
    private Boolean shadow_flag;
    private String image_name;

    public ServiceCategory() {
        super(ServiceCategory.class);
        this.shadow_flag = true;
    }

    public void setServiceId(Integer service_id) {
        this.service_id = service_id;
    }

    public Integer getServiceId() {
        return service_id;
    }

    public void setServiceName(String service_name) {
        this.service_name = service_name;
    }

    public String getServiceName() {
        return service_name;
    }

    public void setServiceCategory(String service_category) {
        this.service_category = service_category;
    }

    public String getServiceCategory() {
        return service_category;
    }

    public void setShadowFlag(Boolean shadow_flag) {
        this.shadow_flag = shadow_flag;
    }

    public Boolean getShadowFlag() {
        return shadow_flag;
    }

    public void setImageName(String image_name) {
        this.image_name = image_name;
    }

    public String getImageName() {
        return image_name;
    }

}
