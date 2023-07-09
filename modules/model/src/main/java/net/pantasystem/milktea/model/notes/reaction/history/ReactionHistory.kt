package net.pantasystem.milktea.model.notes.reaction.history

data class ReactionHistory(
    val reaction: String,
    val instanceDomain: String,
    val accountId: Long?,
    val targetUserId: String?,
    val targetPostId: String?,
    var id: Long? = null
)