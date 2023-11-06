package net.pantasystem.milktea.model.note.repost


import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.Visibility


data class CreateRenote(
    val author: Account,
    val renoteId: Note.Id,
    val channelId: Channel.Id?,
    val visibility: Visibility,
) {
    companion object {
        fun ofTimeline(author: Account, target: Note) : CreateRenote {
            return CreateRenote(
                author = author,
                renoteId = target.id,
                channelId = null,
                visibility = target.visibility,
            )
        }

        fun ofChannel(author: Account, target: Note): CreateRenote {
            return CreateRenote(
                author = author,
                renoteId = target.id,
                channelId = target.channelId,
                visibility = target.visibility,
            )
        }
    }
}