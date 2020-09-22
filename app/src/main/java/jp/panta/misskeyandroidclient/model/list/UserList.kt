package jp.panta.misskeyandroidclient.model.list

import java.io.Serializable
import java.util.*

data class UserList(
    val id: String,
    val createdAt: Date,
    val name: String,
    val userIds: List<String>
) : Serializable