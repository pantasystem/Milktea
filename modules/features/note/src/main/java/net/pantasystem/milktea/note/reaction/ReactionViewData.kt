package net.pantasystem.milktea.note.reaction

import net.pantasystem.milktea.model.emoji.Emoji
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.reaction.LegacyReaction
import net.pantasystem.milktea.model.notes.reaction.Reaction
import net.pantasystem.milktea.model.notes.reaction.ReactionCount

data class ReactionViewData(
    val noteId: Note.Id,
    val reaction: String,
    val reactionCount: ReactionCount,
    val isMyReaction: Boolean,
    val emoji: Emoji?
) {
    companion object {
        fun from(
            reactions: List<ReactionCount>,
            note: Note,
            instanceEmojis: Map<String, Emoji>?,
        ): List<ReactionViewData> {
            val noteEmojis = note.emojis?.associateBy {
                it.name
            }
            return reactions.map { reactionCount ->

                val textReaction = LegacyReaction.reactionMap[reactionCount.reaction] ?: reactionCount.reaction
                val r = Reaction(textReaction)
                val emoji = noteEmojis?.get(textReaction.replace(":", ""))
                    ?: instanceEmojis?.get(r.getName())
                ReactionViewData(
                    noteId = note.id,
                    reaction = textReaction,
                    reactionCount = reactionCount,
                    isMyReaction = note.myReaction == reactionCount.reaction,
                    emoji = emoji
                )
            }
        }
    }
}