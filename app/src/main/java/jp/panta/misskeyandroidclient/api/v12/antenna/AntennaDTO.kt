package jp.panta.misskeyandroidclient.api.v12.antenna

import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.antenna.Antenna
import jp.panta.misskeyandroidclient.model.group.Group
import kotlinx.serialization.Serializable
import java.io.Serializable as JSerializable

/**
 * @param users @userName, @userName@hostの形式のデータが入る
 */
@Serializable
data class AntennaDTO(
    val id: String,
    val name: String,
    val src: String,
    val userListId: String?,
    val userGroupId: String?,
    val keywords: List<List<String>>,
    val excludeKeywords: List<List<String>>,
    val users: List<String>,
    val caseSensitive: Boolean,
    val withFile: Boolean,
    val withReplies: Boolean,
    val notify: Boolean,
    val hasUnreadNote: Boolean
) : JSerializable {

    fun toEntity(account: Account): Antenna {
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
            hasUnreadNote
        )

    }
}