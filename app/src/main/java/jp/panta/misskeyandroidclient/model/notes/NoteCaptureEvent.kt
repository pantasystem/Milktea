package jp.panta.misskeyandroidclient.model.notes

import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionCount
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap


class NoteCaptureEvent(
    val noteId: Note.Id,
    val event: Event,
    val eventAt: Date = Date(),
    val authorId: String = ""
)

@Deprecated("古く扱いにくいため非推奨")
sealed class Event{

    abstract fun adaptationAndCreateNoteDTO(note: NoteDTO, account: Account) : NoteDTO?


    object Deleted : Event(){
        override fun adaptationAndCreateNoteDTO(note: NoteDTO, account: Account): NoteDTO {
            return note
        }
    }

    sealed class NewNote() : Event(){
        abstract fun newNote(note: Note, account: Account): Note


        data class Voted(
            val choice: Int,
            val userId: String?
        ) : NewNote(){
            override fun adaptationAndCreateNoteDTO(note: NoteDTO, account: Account): NoteDTO {
                val poll = note.poll
                    ?: return note
                val choices = ArrayList(poll.choices)
                val choice = choices[this.choice]
                val votedCount = choice.votes + 1
                val votedChoice = choice.copy( votes = votedCount, isVoted = account.remoteId == this.userId)
                choices[this.choice] = votedChoice
                return note.copy(poll = poll.copy(choices = choices))
            }

            override fun newNote(note: Note, account: Account): Note{
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

        data class Reacted(
            val userId: String?,
            val reaction: String,
            val emoji: Emoji?
        ) : NewNote(){
            override fun adaptationAndCreateNoteDTO(note: NoteDTO, account: Account): NoteDTO {
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

            override fun newNote(note: Note, account: Account): Note{
                val emojis = note.emojis?.toHashSet()
                emoji?.let{
                    emojis?.add(it)
                }
                val reactions: List<ReactionCount> = note.reactionCounts
                val map = LinkedHashMap(reactions.map{
                    it.reaction to it.count
                }.toMap())
                map[this.reaction] = (map[this.reaction]?: 0) + 1
                return note.copy(
                    emojis = emojis?.toList(),
                    reactionCounts = map.map{ ReactionCount(it.key, it.value)}.filter{ it.count > 0 },
                    myReaction = if(account.remoteId == this.userId) this.reaction else note.myReaction
                )
            }
        }

        data class UnReacted(
            val userId: String?,
            val reaction: String
        ) : NewNote(){

            override fun adaptationAndCreateNoteDTO(note: NoteDTO, account: Account): NoteDTO {
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

            override fun newNote(note: Note, account: Account): Note{
                val reactionsMap = LinkedHashMap<String, Int>(
                    note.reactionCounts.map {
                        it.reaction to it.count
                    }.toMap()
                )

                reactionsMap[this.reaction] = (reactionsMap[this.reaction]?: 0) - 1
                if(reactionsMap[this.reaction]?: 0 <= 0){
                    reactionsMap.remove(this.reaction)
                }
                return note.copy(
                    reactionCounts = reactionsMap.map{ ReactionCount(it.key, it.value)}.filter{ it.count > 0 },
                    myReaction = if(account.remoteId == this.userId) null else note.myReaction
                )
            }
        }
    }




}
