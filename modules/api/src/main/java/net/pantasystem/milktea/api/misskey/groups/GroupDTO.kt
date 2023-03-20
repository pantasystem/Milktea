package net.pantasystem.milktea.api.misskey.groups

import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.serialization.SerialName
import java.io.Serializable

@kotlinx.serialization.Serializable
data class GroupDTO(
    @SerialName("id")
    val id: String,

    @SerialName("createdAt")
    @kotlinx.serialization.Serializable(InstantIso8601Serializer::class)
    val createdAt: Instant,

    @SerialName("name")
    val name: String,

    @SerialName("ownerId")
    val ownerId: String,

    @SerialName("userIds")
    val userIds: List<String>
): Serializable


