package net.pantasystem.milktea.data.api.misskey.notes

import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import net.pantasystem.milktea.data.model.notes.poll.Poll
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

fun PollDTO?.toPoll(): Poll? {
    return this?.let { dto ->
        Poll(
            multiple = dto.multiple,
            expiresAt = dto.expiresAt,
            choices = choices.mapIndexed { index, value ->
                Poll.Choice(
                    index = index,
                    text = value.text,
                    isVoted = value.isVoted,
                    votes = value.votes
                )
            }
        )
    }
}