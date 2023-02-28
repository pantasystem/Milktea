package net.pantasystem.milktea.api.mastodon.list

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class AddAccountsToList(
    @SerialName("account_ids") val accountIds: List<String>
)

typealias RemoveAccountsFromList = AddAccountsToList