package jp.panta.misskeyandroidclient.model.notification

import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.users.User
import java.io.Serializable
import java.util.*

data class Notification(
    val id: String,
    val createdAt: Date,
    val type: String,
    val userId: String,
    val user: User,
    val note: Note?,
    val reaction: String?
): Serializable