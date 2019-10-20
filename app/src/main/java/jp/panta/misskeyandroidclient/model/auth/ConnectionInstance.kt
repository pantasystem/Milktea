package jp.panta.misskeyandroidclient.model.auth

data class ConnectionInstance(
    val instanceBaseUrl: String,
    val userId: String,
    val userToken: String
)