package com.myapps.hibike.utils

import android.text.TextUtils
import android.util.Base64
import android.util.Log
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.X509EncodedKeySpec

object SecurityUtil {

    private const val TAG = "HMS_LOG_SecurityUtil"
    private const val SIGN_ALGORITHMS = "SHA256WithRSA"
    private const val RSA = "RSA"
    private const val UTF_8 = "utf-8"

    fun doCheck(content: String, sign: String?, publicKey: String?): Boolean {
        if (TextUtils.isEmpty(publicKey)) {
            return false
        }
        try {
            val keyFactory = KeyFactory.getInstance(RSA)
            val encodedKey = Base64.decode(publicKey, Base64.DEFAULT)
            val pubKey = keyFactory.generatePublic(X509EncodedKeySpec(encodedKey))
            val signature = Signature.getInstance(SIGN_ALGORITHMS)
            signature.initVerify(pubKey)
            signature.update(content.toByteArray(charset(UTF_8)))
            return signature.verify(Base64.decode(sign, Base64.DEFAULT))
        } catch (e: Exception) {
            Log.e(TAG, e.message.toString())
        }
        return false
    }
}