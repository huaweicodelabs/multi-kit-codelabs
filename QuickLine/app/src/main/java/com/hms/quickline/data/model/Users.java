package com.hms.quickline.data.model;

import com.huawei.agconnect.cloud.database.CloudDBZoneObject;
import com.huawei.agconnect.cloud.database.annotations.DefaultValue;
import com.huawei.agconnect.cloud.database.annotations.PrimaryKeys;

import java.util.Date;

/**
 * Definition of ObjectType Users.
 *
 * @since 2022-07-25
 */
@PrimaryKeys({"uid"})
public final class Users extends CloudDBZoneObject {
    private String uid;

    private String name;

    @DefaultValue(booleanValue = false)
    private Boolean isCalling;

    @DefaultValue(booleanValue = true)
    private Boolean isAvailable;

    private String email;

    private String photo;

    private String phone;

    private Date lastSeen;

    @DefaultValue(booleanValue = false)
    private Boolean isVerified;

    private String callerName;

    public Users() {
        super(Users.class);
        this.isCalling = false;
        this.isAvailable = true;
        this.isVerified = false;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setIsCalling(Boolean isCalling) {
        this.isCalling = isCalling;
    }

    public Boolean getIsCalling() {
        return isCalling;
    }

    public void setIsAvailable(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public Boolean getIsAvailable() {
        return isAvailable;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhone() {
        return phone;
    }

    public void setLastSeen(Date lastSeen) {
        this.lastSeen = lastSeen;
    }

    public Date getLastSeen() {
        return lastSeen;
    }

    public void setIsVerified(Boolean isVerified) {
        this.isVerified = isVerified;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }

    public void setCallerName(String callerName) {
        this.callerName = callerName;
    }

    public String getCallerName() {
        return callerName;
    }

}
