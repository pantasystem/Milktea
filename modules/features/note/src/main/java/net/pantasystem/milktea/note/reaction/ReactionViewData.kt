package net.pantasystem.milktea.note.reaction

import net.pantasystem.milktea.model.emoji.CustomEmoji
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.reaction.LegacyReaction
import net.pantasystem.milktea.model.note.reaction.Reaction
import net.pantasystem.milktea.model.note.reaction.ReactionCount

data class ReactionViewData(
    val noteId: Note.Id,
    val reaction: String,
    val reactionCount: ReactionCount,
    val isMyReaction: Boolean,
    val emoji: CustomEmoji?
) {
    companion object {
        fun from(
            reactions: List<ReactionCount>,
            note: Note,
        ): List<ReactionViewData> {
            val noteEmojis = note.emojiNameMap
            return reactions.map { reactionCount ->

                val textReaction = LegacyReaction.reactionMap[reactionCount.reaction] ?: reactionCount.reaction
                val r = Reaction(textReaction)
                val emoji = noteEmojis?.get(textReaction.replace(":", ""))
                    ?: noteEmojis?.get(r.getName())
                ReactionViewData(
                    noteId = note.id,
                    reaction = textReaction,
                    reactionCount = reactionCount,
                    isMyReaction = reactionCount.me,
                    emoji = emoji
                )
            }
        }
    }
}