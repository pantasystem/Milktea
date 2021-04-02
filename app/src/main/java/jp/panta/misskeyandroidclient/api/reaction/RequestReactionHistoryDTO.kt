package jp.panta.misskeyandroidclient.api.reaction

data class RequestReactionHistoryDTO (
    val noteId: String,
    val type: String?,
    val limit: Int = 20,
    val offset: Int? = null,
    val sinceId: String? = null,
    val untilId: String? = null
)