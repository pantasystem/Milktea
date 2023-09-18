package net.pantasystem.milktea.api.mastodon.list

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateListRequest(
    @SerialName("title") val title: String,
    @SerialName("replies_policy") val repliesPolicy: ListDTO.RepliesPolicyType
)