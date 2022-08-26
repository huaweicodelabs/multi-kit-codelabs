/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2019-2019. All rights reserved.
 * Generated by the CloudDB ObjectType compiler.  DO NOT EDIT!
 */

package com.huawei.hms.urbanhomeservices.kotlin.clouddb;

import com.huawei.agconnect.cloud.database.CloudDBZoneObject;
import com.huawei.agconnect.cloud.database.annotations.DefaultValue;
import com.huawei.agconnect.cloud.database.annotations.PrimaryKeys;

/**
 * Definition of ObjectType ServiceType.
 *
 * @author: Huawei
 * @since 20-01-21
 */

@PrimaryKeys({"id"})
public class ServiceType extends CloudDBZoneObject {
    private String cat_name;
    private Integer id;
    @DefaultValue(booleanValue = true)
    private Boolean shadow_flag;
    private Long phone_number;
    private String email_id;
    private String service_provider_name;
    private String country;
    private String state;
    private String city;
    private String user_name;

    public ServiceType() {
        super(ServiceType.class);
        this.shadow_flag = true;
    }

    public void setCatName(String cat_name) {
        this.cat_name = cat_name;
    }

    public String getCatName() {
        return cat_name;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setShadowFlag(Boolean shadow_flag) {
        this.shadow_flag = shadow_flag;
    }

    public Boolean getShadowFlag() {
        return shadow_flag;
    }

    public void setPhoneNumber(Long phone_number) {
        this.phone_number = phone_number;
    }

    public Long getPhoneNumber() {
        return phone_number;
    }

    public void setEmailId(String email_id) {
        this.email_id = email_id;
    }

    public String getEmailId() {
        return email_id;
    }

    public void setServiceProviderName(String service_provider_name) {
        this.service_provider_name = service_provider_name;
    }

    public String getServiceProviderName() {
        return service_provider_name;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCountry() {
        return country;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCity() {
        return city;
    }

    public void setUserName(String user_name) {
        this.user_name = user_name;
    }

    public String getUserName() {
        return user_name;
    }
}
