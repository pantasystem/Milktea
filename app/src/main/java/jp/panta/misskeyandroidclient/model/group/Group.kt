package jp.panta.misskeyandroidclient.model.group

data class Group(
    val id: String,
    val createdAt: String,
    val name: String,
    val ownerId: String?,
    val userIds: List<String>?
)