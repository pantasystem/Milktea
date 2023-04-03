package net.pantasystem.milktea.api.misskey.list


import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.serialization.SerialName
import java.io.Serializable

@kotlinx.serialization.Serializable
data class UserListDTO(
    @SerialName("id")
    val id: String,

    @kotlinx.serialization.Serializable(with = InstantIso8601Serializer::class)
    @SerialName("createdAt")
    val createdAt: Instant,

    @SerialName("name")
    val name: String,

    @SerialName("userIds")
    val userIds: List<String>
) : Serializable