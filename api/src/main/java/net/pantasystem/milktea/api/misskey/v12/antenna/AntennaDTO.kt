package net.pantasystem.milktea.api.misskey.v12.antenna


import kotlinx.serialization.Serializable
import net.pantasystem.milktea.model.antenna.Antenna
import net.pantasystem.milktea.model.group.Group
import java.io.Serializable as JSerializable

/**
 * @param users @userName, @userName@hostの形式のデータが入る
 */
@Serializable
data class AntennaDTO(
    val id: String,
    val name: String,
    val src: String,
    val userListId: String? = null,
    val userGroupId: String? = null,
    val keywords: List<List<String>>,
    val excludeKeywords: List<List<String>>,
    val users: List<String>,
    val caseSensitive: Boolean,
    val withFile: Boolean,
    val withReplies: Boolean,
    val notify: Boolean,
    val hasUnreadNote: Boolean? = null
) : JSerializable {

    fun toEntity(account: net.pantasystem.milktea.model.account.Account): Antenna {
        return Antenna(
            Antenna.Id(account.accountId, id),
            name,
            src,
            userListId,
            userGroupId?.let{
                Group.Id(account.accountId, it)
            },
            keywords,
            excludeKeywords,
            users,
            caseSensitive,
            withFile,
            withReplies,
            notify,
            hasUnreadNote?: false
        )

    }
}