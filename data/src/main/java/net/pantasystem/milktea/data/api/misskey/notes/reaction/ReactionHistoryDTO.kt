package net.pantasystem.milktea.data.api.misskey.notes.reaction

import jp.panta.misskeyandroidclient.api.misskey.users.UserDTO
import java.util.*

data class ReactionHistoryDTO (
    val id: String,
    val createdAt: Date,
    val user: UserDTO,
    val type: String,
)