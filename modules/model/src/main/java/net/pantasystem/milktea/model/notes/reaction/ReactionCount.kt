package net.pantasystem.milktea.model.notes.reaction

import java.io.Serializable

data class ReactionCount(
    val reaction: String,
    val count: Int,
    val me: Boolean,
) : Serializable