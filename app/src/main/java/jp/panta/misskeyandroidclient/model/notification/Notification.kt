package jp.panta.misskeyandroidclient.model.notification

import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.api.users.UserDTO
import java.io.Serializable
import java.util.*

data class Notification(
    val id: String,
    val createdAt: Date,
    val type: String,
    val userId: String,
    val user: UserDTO,
    val note: NoteDTO?,
    val reaction: String?
): Serializable