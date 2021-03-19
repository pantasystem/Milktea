package jp.panta.misskeyandroidclient.api.list

import jp.panta.misskeyandroidclient.serializations.DateSerializer
import java.io.Serializable
import java.util.Date

@kotlinx.serialization.Serializable
data class UserList(
    val id: String,
    @kotlinx.serialization.Serializable(with = DateSerializer::class) val createdAt: Date,
    val name: String,
    val userIds: List<String>
) : Serializable