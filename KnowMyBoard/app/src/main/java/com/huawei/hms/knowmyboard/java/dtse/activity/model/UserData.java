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

package com.huawei.hms.knowmyboard.dtse.activity.model;

import com.huawei.hms.support.api.entity.auth.Scope;

import java.util.HashSet;
import java.util.Set;

public class UserData {
    private String uid;
    private String openId;
    private String displayName;
    private String photoUriString;
    private String accessToken;
    private int status;
    private int gender;
    private String serviceCountryCode;
    private String countryCode;
    private Set<Scope> grantedScopes;
    private String serverAuthCode;
    private String unionId;
    private String email;
    private Set<Scope> extensionScopes = new HashSet();
    private String idToken;
    private long expirationTimeSecs;
    private String givenName;
    private String familyName;
    private String ageRange;
    private int homeZone;
    private int carrierId;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhotoUriString() {
        return photoUriString;
    }

    public void setPhotoUriString(String photoUriString) {
        this.photoUriString = photoUriString;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getServiceCountryCode() {
        return serviceCountryCode;
    }

    public void setServiceCountryCode(String serviceCountryCode) {
        this.serviceCountryCode = serviceCountryCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public Set<Scope> getGrantedScopes() {
        return grantedScopes;
    }

    public void setGrantedScopes(Set<Scope> grantedScopes) {
        this.grantedScopes = grantedScopes;
    }

    public String getServerAuthCode() {
        return serverAuthCode;
    }

    public void setServerAuthCode(String serverAuthCode) {
        this.serverAuthCode = serverAuthCode;
    }

    public String getUnionId() {
        return unionId;
    }

    public void setUnionId(String unionId) {
        this.unionId = unionId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<Scope> getExtensionScopes() {
        return extensionScopes;
    }

    public void setExtensionScopes(Set<Scope> extensionScopes) {
        this.extensionScopes = extensionScopes;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public long getExpirationTimeSecs() {
        return expirationTimeSecs;
    }

    public void setExpirationTimeSecs(long expirationTimeSecs) {
        this.expirationTimeSecs = expirationTimeSecs;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getAgeRange() {
        return ageRange;
    }

    public void setAgeRange(String ageRange) {
        this.ageRange = ageRange;
    }

    public int getHomeZone() {
        return homeZone;
    }

    public void setHomeZone(int homeZone) {
        this.homeZone = homeZone;
    }

    public int getCarrierId() {
        return carrierId;
    }

    public void setCarrierId(int carrierId) {
        this.carrierId = carrierId;
    }
}
