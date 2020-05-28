package jp.panta.misskeyandroidclient.model.hashtag

class RequestHashTagList (
    val i: String?,
    val sort: String,
    val limit: Int = 30,
    val attachedToUserOnly: Boolean? = null,
    val attachedToLocalUserOnly: Boolean? = null,
    val attachedToRemoteUserOnly: Boolean? = null
)