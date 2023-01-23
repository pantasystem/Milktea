package net.pantasystem.milktea.model.notes.poll

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import java.io.Serializable

@kotlinx.serialization.Serializable
data class Poll(
    val choices: List<Choice>,
    @kotlinx.serialization.Serializable(with = InstantIso8601Serializer::class) val expiresAt: Instant?,
    val multiple: Boolean
) : Serializable {

    @kotlinx.serialization.Serializable
    data class Choice(val index: Int, val text: String, val votes: Int, val isVoted: Boolean) :
        Serializable

    val canVote: Boolean
        get() {
            return (expiresAt == null
                    || expiresAt >= Clock.System.now())
                    && (multiple || !choices.any { it.isVoted })
                    && !choices.all { it.isVoted }
        }

    val totalVoteCount: Int
        get() {
            return choices.sumOf {
                it.votes
            }
        }
}