package net.pantasystem.milktea.model.notes.poll

import kotlinx.serialization.Serializable

@Serializable
data class Vote(
    val i: String,
    val choice: Int,
    val noteId: String
)