package jp.panta.misskeyandroidclient.model.message

data class RequestMessageHistory(
    val i: String,
    val limit: Int? = null,
    val group: Boolean? = null
)