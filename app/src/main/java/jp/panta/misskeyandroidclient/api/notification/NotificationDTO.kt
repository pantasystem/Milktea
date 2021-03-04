package jp.panta.misskeyandroidclient.api.notification

import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.api.users.UserDTO
import java.util.*

data class NotificationDTO(
    val id: String,
    val createdAt: Date,
    val type: String,
    val userId: String,
    val user: UserDTO,
    val note: NoteDTO?,
    val reaction: String?
)