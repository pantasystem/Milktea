package net.pantasystem.milktea.api.misskey.notes

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import java.io.Serializable

@kotlinx.serialization.Serializable
data class PollDTO(
    @SerialName("choices")
    val choices: List<ChoiceDTO>,

    @SerialName("expiresAt")
    val expiresAt: Instant? = null,

    @SerialName("multiple")
    val multiple: Boolean,
) : Serializable {

    @kotlinx.serialization.Serializable
    data class ChoiceDTO(
        @SerialName("text")
        val text: String,

        @SerialName("votes")
        val votes: Int,

        @SerialName("isVoted")
        val isVoted: Boolean = false,
    ) : Serializable
}

