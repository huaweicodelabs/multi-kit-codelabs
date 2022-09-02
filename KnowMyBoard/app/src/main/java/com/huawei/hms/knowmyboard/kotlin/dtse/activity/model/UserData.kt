package com.huawei.hms.knowmyboard.dtse.activity.model

import com.huawei.hms.support.api.entity.auth.Scope
import java.util.HashSet

class UserData {
    var uid: String? = null
    var openId: String? = null
    var displayName: String? = null
    var photoUriString: String? = null
    var accessToken: String? = null
    var status = 0
    var gender = 0
    var serviceCountryCode: String? = null
    var countryCode: String? = null
    var grantedScopes: Set<Scope>? = null
    var serverAuthCode: String? = null
    var unionId: String? = null
    var email: String? = null
    var extensionScopes: Any? = HashSet<Any?>()
    var idToken: String? = null
    var expirationTimeSecs: Long = 0
    var givenName: String? = null
    var familyName: String? = null
    var ageRange: String? = null
    var homeZone = 0
    var carrierId = 0
}