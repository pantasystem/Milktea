package jp.panta.misskeyandroidclient.model.notification

import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.api.users.UserDTO
import jp.panta.misskeyandroidclient.serializations.DateSerializer
import kotlinx.serialization.Serializer
import java.io.Serializable
import java.util.*


@kotlinx.serialization.Serializable
data class Notification(
    val id: String,

    @kotlinx.serialization.Serializable(with = DateSerializer::class) val createdAt: Date,
    val type: String,
    val userId: String,
    val user: UserDTO,
    val note: NoteDTO?,
    val reaction: String?
): Serializable