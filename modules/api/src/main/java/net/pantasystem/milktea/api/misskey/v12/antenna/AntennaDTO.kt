package net.pantasystem.milktea.api.misskey.v12.antenna


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.antenna.Antenna
import net.pantasystem.milktea.model.antenna.AntennaSource
import net.pantasystem.milktea.model.antenna.from
import net.pantasystem.milktea.model.group.Group
import net.pantasystem.milktea.model.list.UserList
import java.io.Serializable as JSerializable

/**
 * @param users @userName, @userName@hostの形式のデータが入る
 */
@Serializable
data class AntennaDTO(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String,

    @SerialName("src")
    val src: String,

    @SerialName("userListId")
    val userListId: String? = null,

    @SerialName("userGroupId")
    val userGroupId: String? = null,

    @SerialName("keywords")
    val keywords: List<List<String>>,

    @SerialName("excludeKeywords")
    val excludeKeywords: List<List<String>>,

    @SerialName("users")
    val users: List<String>,

    @SerialName("caseSensitive")
    val caseSensitive: Boolean,

    @SerialName("withFile")
    val withFile: Boolean,

    @SerialName("withReplies")
    val withReplies: Boolean,

    @SerialName("notify")
    val notify: Boolean,

    @SerialName("hasUnreadNote")
    val hasUnreadNote: Boolean? = null
) : JSerializable {

    fun toEntity(account: Account): Antenna {
        return Antenna(
            Antenna.Id(account.accountId, id),
            name,
            AntennaSource.from(src),
            userListId?.let { userListId ->
                UserList.Id(account.accountId, userListId)
            },
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