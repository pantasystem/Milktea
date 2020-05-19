package jp.panta.misskeyandroidclient.model.v10

data class RequestFollowFollower(
    val i: String?,
    val userId: String?,
    val cursor: String?,
    val username: String? = null,
    val host: String? = null,
    val limit: Int = 20,
    val iknow: Boolean? = null,
    val diff: Boolean? = null

)