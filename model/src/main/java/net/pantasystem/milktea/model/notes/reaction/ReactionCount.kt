package net.pantasystem.milktea.model.notes.reaction

import java.io.Serializable

data class ReactionCount(
    val reaction: String,
    val count: Int
) : Serializable {

    fun isLocal(): Boolean {
        return !reaction.contains("@") || reaction.replace(":", "").split("@").getOrNull(1) == "."
    }
}