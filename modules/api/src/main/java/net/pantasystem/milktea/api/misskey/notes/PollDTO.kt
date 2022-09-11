package net.pantasystem.milktea.api.misskey.notes

import kotlinx.datetime.Instant
import java.io.Serializable

@kotlinx.serialization.Serializable
data class PollDTO(
    val choices: List<ChoiceDTO>,
    val expiresAt: Instant? = null,
    val multiple: Boolean
) : Serializable {

    @kotlinx.serialization.Serializable
    data class ChoiceDTO(val text: String, val votes: Int, val isVoted: Boolean = false) : Serializable
}

