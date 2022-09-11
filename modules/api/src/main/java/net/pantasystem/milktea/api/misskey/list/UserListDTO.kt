package net.pantasystem.milktea.api.misskey.list


import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import java.io.Serializable

@kotlinx.serialization.Serializable
data class UserListDTO(
    val id: String,
    @kotlinx.serialization.Serializable(with = InstantIso8601Serializer::class) val createdAt: Instant,
    val name: String,
    val userIds: List<String>
) : Serializable