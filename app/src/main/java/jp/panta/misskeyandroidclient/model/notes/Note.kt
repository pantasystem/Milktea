package jp.panta.misskeyandroidclient.model.notes

import jp.panta.misskeyandroidclient.model.auth.custom.App
import jp.panta.misskeyandroidclient.model.drive.FileProperty
import jp.panta.misskeyandroidclient.model.emoji.Emoji
import jp.panta.misskeyandroidclient.model.notes.poll.Poll
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionCount
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.model.users.UserRepository
import jp.panta.misskeyandroidclient.model.users.UserState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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



/**
 * noteに基づいて関連したオブジェクトなどをとってきてくれる
 */
class StatefulNote(
    val note: StateFlow<State>,
    val user: StateFlow<UserState>,
    val reply: StatefulNote?,
    val renote: StatefulNote?,
    var job: Job? = null
){
    sealed class State {
        data class Removed(val id: Note.Id) : State()
        data class Success(val note: Note) : State()
        data class Error(val exception: Exception) : State()
        object None : State()
    }

    class Loader(
        private val noteRepository: NoteRepository,
        private val userRepository: UserRepository,
        private val noteCaptureAPIAdapter: NoteCaptureAPIAdapter,
        private val coroutineScope: CoroutineScope,
        private val dispatcher: CoroutineDispatcher = Dispatchers.IO
    ) {
        suspend fun load(id: Note.Id, recursive: Boolean = true): StatefulNote {
            var note: Note? = null
            val noteState = try{
                noteRepository.get(id).let{
                    note = it
                    State.Success(it)
                }
            }catch (t: Exception){
                State.Error(exception = t)
            }
            val noteStateFlow = MutableStateFlow<State>(noteState)
            val job = captureNote(id, noteStateFlow).launchIn(coroutineScope + dispatcher)

            val userState = try{
                note?.let{
                    userRepository.get(it.userId)?.let{ u->
                        UserState.Success(u)
                    }
                }?: UserState.None

            }catch(e: Exception) {
                UserState.Error(e)
            }
            val userStateFlow = MutableStateFlow<UserState>(userState)

            var reply: StatefulNote? = null
            var renote: StatefulNote? = null
            if(recursive && note != null) {
                note?.renoteId?.let{ renoteId ->
                    renote = load(renoteId, false)
                }
                note?.replyId?.let{ replyId ->
                    reply = load(replyId, false)
                }

            }

            return StatefulNote(noteStateFlow, userStateFlow, reply = reply, renote = renote, job = job)
        }

        private fun captureNote(id: Note.Id, noteStateFlow: MutableStateFlow<State>) = noteCaptureAPIAdapter.capture(id).onEach {
            when(it){
                is NoteRepository.Event.Updated -> {
                    noteStateFlow.value = State.Success(it.note)
                }
                is NoteRepository.Event.Deleted -> {
                    noteStateFlow.value = State.Removed(it.noteId)
                }
                is NoteRepository.Event.Created -> {
                    noteStateFlow.value = State.Success(it.note)
                }
            }
        }
    }

}