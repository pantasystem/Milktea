package jp.panta.misskeyandroidclient.model.notes

import jp.panta.misskeyandroidclient.model.auth.custom.App
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.model.notes.poll.Poll
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionCount
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.model.users.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

data class Note(
    val id: Id,
    val createdAt: Date,
    val text: String?,
    val cw: String?,
    val userId: User.Id,

    val replyId: Id?,

    val renoteId: Id?,

    val viaMobile: Boolean?,
    val visibility: String?,
    val localOnly: Boolean?,

    val visibleUserIds: List<User.Id>?,

    val url: String?,
    val uri: String?,
    val renoteCount: Int,
    val reactionCounts: List<ReactionCount>,
    val emojis: List<Emoji>?,
    val repliesCount: Int,
    val files: List<FileProperty>?,
    val poll: Poll?,
    val myReaction: String?,


    val app: App?,
    var instanceUpdatedAt: Date = Date()
) {

    data class Id(
        val accountId: Long,
        val noteId: String
    )

    fun updated(){
        this.instanceUpdatedAt = Date()
    }
}


class StatefulNote(
    val noteEventStream: Flow<NoteRepository.Event>,
    val userEventStream: Flow<UserRepository.Event>,
    coroutineScope: CoroutineScope,
    val note: Note,
    val user: User,
    val renote: StatefulNote?,


) {
    class Factory(
        val noteId: Note.Id,
        val noteRepository: NoteRepository,
        val userRepository: UserRepository,
        val coroutineScope: CoroutineScope
    ) {
        suspend fun create() {
            val note = noteRepository.get(noteId)
            val user = note?.let{
                userRepository.get(it.userId)
            }
            val reply = note?.replyId?.let{
                noteRepository.get(it)
            }

            val renote = note?.renoteId?.let{
                noteRepository.get(it)
            }
        }
    }
    init {


        coroutineScope.launch(Dispatchers.IO) {

        }
    }
}