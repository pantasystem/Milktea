package net.pantasystem.milktea.api.mastodon.list

import kotlinx.serialization.SerialName

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
}