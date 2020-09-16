package jp.panta.misskeyandroidclient.model.notes

import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap


class NoteEvent(
    val noteId: String,
    val event: Event,
    val eventAt: Date = Date(),
    val authorId: String = ""
)


sealed class Event{

    abstract fun adaptationAndCreate(note: Note, account: Account) : Note?

    object Deleted : Event(){
        override fun adaptationAndCreate(note: Note, account: Account): Note {
            return note
        }
    }
    class Voted(
        val choice: Int,
        val userId: String?
    ) : Event(){
        override fun adaptationAndCreate(note: Note, account: Account): Note {
            val poll = note.poll
                ?: return note
            val choices = ArrayList(poll.choices)
            val choice = choices[this.choice]
            val votedCount = choice.votes + 1
            val votedChoice = choice.copy( votes = votedCount, isVoted = account.remoteId == this.userId)
            choices[this.choice] = votedChoice
            return note.copy(poll = poll.copy(choices = choices))
        }
    }

    class Reacted(
        val userId: String?,
        val reaction: String,
        val emoji: Emoji?
    ) : Event(){
        override fun adaptationAndCreate(note: Note, account: Account): Note {
            val emojis = note.emojis?.toHashSet()
            emoji?.let{
                emojis?.add(it)
            }
            val reactions = LinkedHashMap(note.reactionCounts?: emptyMap())
            reactions[this.reaction] = (reactions[this.reaction]?: 0) + 1
            return note.copy(
                emojis = emojis?.toList(),
                reactionCounts = reactions,
                myReaction = if(account.remoteId == this.userId) this.reaction else note.myReaction
            )
        }
    }

    class UnReacted(
        val userId: String?,
        val reaction: String
    ) : Event(){

        override fun adaptationAndCreate(note: Note, account: Account): Note {
            val reactions = LinkedHashMap(note.reactionCounts?: emptyMap())
            reactions[this.reaction] = (reactions[this.reaction]?: 0) - 1
            if(reactions[this.reaction]?: 0 <= 0){
                reactions.remove(this.reaction)
            }
            return note.copy(
                reactionCounts = reactions,
                myReaction = if(account.remoteId == this.userId) null else note.myReaction
            )
        }
    }

    class Added(
        val note: Note
    ) : Event(){

        override fun adaptationAndCreate(note: Note, account: Account): Note? {
            return note
        }
    }
}
