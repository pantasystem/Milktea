package jp.panta.misskeyandroidclient.model.notes.poll

import jp.panta.misskeyandroidclient.serializations.DateSerializer
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.datetime.toLocalDateTime
import java.io.Serializable
import java.util.*

@kotlinx.serialization.Serializable
data class Poll(
    val choices: List<Choice>,
    @kotlinx.serialization.Serializable(with = InstantIso8601Serializer::class) val expiresAt: Instant?,
    val multiple: Boolean
): Serializable{

    @kotlinx.serialization.Serializable
    data class Choice(val text: String, val votes: Int, val isVoted: Boolean): Serializable
}