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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.lang.Exception
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

sealed class NoteState {
    data class Removed(val id: Note.Id) : NoteState()
    data class Success(val note: Note) : NoteState()
    data class Error(val id: Note.Id, val exception: Exception) : NoteState()
    object None : NoteState()
    object Loading : NoteState()
}

class StatefulNote(
    val noteId: Note.Id,
    coroutineScope: CoroutineScope,
    nState: MutableStateFlow<NoteState>,
    u: User,
    val renote: StatefulNote?,
    val reply: StatefulNote?,

    noteCaptureAPIAdapter: NoteCaptureAPIAdapter

) {
    class Factory(

        val noteRepository: NoteRepository,
        val userRepository: UserRepository,
        val noteCaptureAPIAdapter: NoteCaptureAPIAdapter,
        val coroutineScope: CoroutineScope
    ) {
        suspend fun create(noteId: Note.Id, isRecursive: Boolean = true): StatefulNote? {
            val note = noteRepository.get(noteId)
            val user = note.let{
                userRepository.get(it.userId)
            }?: return null
            val nState = note?.let {
                NoteState.Success(it)
            }?: NoteState.None


            return StatefulNote(
                noteId,
                coroutineScope,
                ,
                user,
                noteCaptureAPIAdapter = noteCaptureAPIAdapter,
                reply = note.replyId?.let{
                    if(isRecursive){
                        create(it, false)
                    }else{
                        null
                    }
                },
                renote = note.renoteId?.let{
                    if(isRecursive){
                        create(it, false)
                    }else{
                        null
                    }
                }
            )
        }
    }

    private val _noteState = nState
    val note: StateFlow<NoteState> = _noteState


    init {


        coroutineScope.launch(Dispatchers.IO) {
            noteCaptureAPIAdapter.capture(n.id).onEach {
                if(it is NoteRepository.Event.Updated) {
                    _noteState.value = NoteState.Success(it.note)
                }
            }.launchIn(this)
        }
    }
}