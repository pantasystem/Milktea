package jp.panta.misskeyandroidclient.api.reaction

import jp.panta.misskeyandroidclient.api.users.UserDTO
import java.util.*

data class ReactionHistoryDTO (
    val id: String,
    val createdAt: Date,
    val user: UserDTO,
    val type: String,
)