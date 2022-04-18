package net.pantasystem.milktea.api.misskey.groups

import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import java.io.Serializable

@kotlinx.serialization.Serializable
data class GroupDTO(
    val id: String,
    @kotlinx.serialization.Serializable(InstantIso8601Serializer::class) val createdAt: Instant,
    val name: String,
    val ownerId: String,
    val userIds: List<String>
): Serializable


