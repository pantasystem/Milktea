package jp.panta.misskeyandroidclient.model.messaging

data class RequestMessageHistory(
    val i: String,
    val limit: Int? = null,
    val group: Boolean? = null
)