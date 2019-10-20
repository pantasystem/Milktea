package jp.panta.misskeyandroidclient.model.auth

import jp.panta.misskeyandroidclient.SecretConstant

data class ConnectionInstance(
    val instanceBaseUrl: String,
    val userId: String,
    val userToken: String
){
    fun getI(): String{
        return SecretConstant.i()
    }
}