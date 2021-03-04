package jp.panta.misskeyandroidclient.api.notification

import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.api.users.UserDTO
import jp.panta.misskeyandroidclient.serializations.DateSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class NotificationDTO(
    val id: String,
    @kotlinx.serialization.Serializable(with = DateSerializer::class) val createdAt: Date,
    val type: String,
    val userId: String,
    val user: UserDTO,
    val note: NoteDTO? = null,
    val reaction: String? = null
)