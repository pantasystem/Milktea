package net.pantasystem.milktea.model.note.reaction

import java.io.Serializable

data class ReactionCount(
    val reaction: String,
    val count: Int,
    val me: Boolean,
) : Serializable