package net.pantasystem.milktea.api.mastodon.list

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.list.UserList

@kotlinx.serialization.Serializable
data class ListDTO(
    val id: String,
    val title: String,
    @SerialName("replies_policy") val repliesPolicy: RepliesPolicyType
)  {
    @kotlinx.serialization.Serializable
    enum class RepliesPolicyType {
        @SerialName("followed") Followed,
        @SerialName("list") List,
        @SerialName("none") None,
    }

    fun toModel(account: Account): UserList {
        return UserList(
            id = UserList.Id(accountId = account.accountId, id),
            createdAt = Instant.fromEpochMilliseconds(Long.MAX_VALUE),
            name = title,
            userIds = emptyList()
        )
    }
}