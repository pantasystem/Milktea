package net.pantasystem.milktea.model.notes.reaction.history

data class ReactionHistory(
    val reaction: String,
    val instanceDomain: String,
    var id: Long? = null
)