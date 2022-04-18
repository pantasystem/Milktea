package net.pantasystem.milktea.api.misskey.notes

import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import java.io.Serializable

@kotlinx.serialization.Serializable
data class PollDTO(
    val choices: List<ChoiceDTO>,
    @kotlinx.serialization.Serializable(with = InstantIso8601Serializer::class) val expiresAt: Instant?,
    val multiple: Boolean
) : Serializable {

    @kotlinx.serialization.Serializable
    data class ChoiceDTO(val text: String, val votes: Int, val isVoted: Boolean) : Serializable
}

