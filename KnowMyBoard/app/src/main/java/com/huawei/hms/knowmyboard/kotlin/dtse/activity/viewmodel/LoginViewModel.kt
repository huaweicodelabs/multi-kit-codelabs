package com.huawei.hms.knowmyboard.dtse.activity.viewmodel

import android.app.Application
import com.huawei.hms.support.account.service.AccountAuthService
import android.graphics.Bitmap
import com.huawei.hms.location.LocationResult
import com.huawei.hms.site.api.model.Site
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.huawei.hms.common.ApiException
import com.huawei.hms.knowmyboard.dtse.R
import com.huawei.hms.support.account.request.AccountAuthParams
import com.huawei.hms.support.account.request.AccountAuthParamsHelper
import com.huawei.hms.support.account.AccountAuthManager
import com.huawei.hms.knowmyboard.dtse.activity.app.MyApplication
import java.util.ArrayList

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    var service: AccountAuthService? = null
    val message = MutableLiveData<String>()
    val textRecongnized = MutableLiveData<ArrayList<String>>()
    val imagePath = MutableLiveData<Bitmap>()
    val locationResult = MutableLiveData<LocationResult>()
    val siteSelected = MutableLiveData<Site>()
    fun getSiteSelected(): LiveData<Site> {
        return siteSelected
    }

    fun setSiteSelected(siteSelected: Site) {
        this.siteSelected.value = siteSelected
    }

    fun sendData(msg: String) {
        message.value = msg
    }

    fun getMessage(): LiveData<String> {
        return message
    }

    fun setImage(imagePath: Bitmap) {
        this.imagePath.value = imagePath
    }

    fun setLocationResult(locationResult: LocationResult) {
        this.locationResult.value = locationResult
    }

    fun setTextRecongnized(textRecongnized: ArrayList<String>) {
        this.textRecongnized.value = textRecongnized
    }

    fun logoutHuaweiID() {
        if (service != null) {
            service!!.signOut()
            sendData("KnowMyBoard")
            Toast.makeText(getApplication(), "You are logged out from Huawei ID", Toast.LENGTH_LONG)
                .show()
        }
    }

    fun loginClicked() {
        val authParams =
            AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM).setAuthorizationCode()
                .createParams()
        service = AccountAuthManager.getService(MyApplication.activity, authParams)
        MyApplication.activity!!.startActivityForResult(service!!.signInIntent, 8888)
    }

    fun cancelAuthorization() {
        if (service != null) {
            // service indicates the AccountAuthService instance generated using the getService method during the sign-in authorization.
            service!!.cancelAuthorization().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Processing after a successful authorization cancellation.
                    sendData(getApplication<Application>().resources.getResourceName(R.string.app_name))
                    Toast.makeText(getApplication(), "Cancelled authorization", Toast.LENGTH_LONG)
                        .show()
                } else {
                    // Handle the exception.
                    val exception = task.exception
                    if (exception is ApiException) {
                        val statusCode = exception.statusCode
                        Toast.makeText(
                            getApplication(),
                            "Failed to cancel authorization. status code $statusCode",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        } else {
            Toast.makeText(getApplication(), "Login required", Toast.LENGTH_LONG).show()
        }
    }
}