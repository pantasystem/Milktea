package jp.panta.misskeyandroidclient.model.drive

data class RequestFile (
    val i: String,
    val limit: Int? = null,
    val sinceId: String? = null,
    val untilId: String? = null,
    val folderId: String? = null,
    val type: String? = null
)