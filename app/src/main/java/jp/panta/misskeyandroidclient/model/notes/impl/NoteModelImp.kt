package jp.panta.misskeyandroidclient.model.notes.impl

import io.reactivex.disposables.Disposable
import jp.panta.misskeyandroidclient.api.notes.DeleteNote
import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.api.notes.NoteRequest
import jp.panta.misskeyandroidclient.api.notes.toNote
import jp.panta.misskeyandroidclient.api.users.toUser
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.UnauthorizedException
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.notes.*
import jp.panta.misskeyandroidclient.model.users.UserRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.net.SocketTimeoutException
import kotlin.jvm.Throws

class NoteModelImp(
    private val noteRepository: NoteRepository,
    private val userRepository: UserRepository,
    private val account: Account,
    private val encryption: Encryption,
    private val misskeyAPI: MisskeyAPI,
    private val noteCapture: NoteCapture,
    private val coroutineScope: CoroutineScope
) : NoteModel{

    private var mNoteCaptureObserverDisposable: Disposable? = null
    private val mutex = Mutex()



    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(IOException::class, UnauthorizedException::class, SocketTimeoutException::class)
    override suspend fun delete(note: Note) {
        misskeyAPI.delete(DeleteNote(i = account.getI(encryption), note.id)).execute()
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(IOException::class, UnauthorizedException::class, SocketTimeoutException::class)
    override suspend fun get(noteId: String): Note? {
        var n = noteRepository.get(noteId)
        if(n == null){
            val res = misskeyAPI.showNote(NoteRequest(i = account.getI(encryption), noteId = noteId)).execute()
            val noteDTO = res.body()
            if(noteDTO != null){
                n = this.add(noteDTO)
            }
        }
        return n
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(IOException::class, UnauthorizedException::class, SocketTimeoutException::class)
    override suspend fun reaction(reaction: String, reactionTo: Note) {
        TODO("Not yet implemented")
    }

    @Throws(IOException::class, UnauthorizedException::class, SocketTimeoutException::class)
    override suspend fun renote(note: Note) {
        TODO("Not yet implemented")
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun unreaction(reaction: String, unreactionTo: Note) {
        TODO("Not yet implemented")
    }

    suspend fun add(note: NoteDTO): Note?{
        if(noteRepository.add(note.toNote())){
            noteCapture.capture(note.id)
        }
        userRepository.add(note.user.toUser())

        if(note.reNote != null){
            this.add(note.reNote)
        }

        if(note.reply != null){
            this.add(note.reply)
        }

        return noteRepository.get(note.id)


    }

    private suspend fun add(note: Note){
        noteRepository.add(note)
    }

    fun dispose(){

    }

    private suspend fun startCapture(){
        noteCapture.observer().collect {
            mutex.withLock {
                val note = noteRepository.get(it.noteId)
                    ?: return@withLock
                when(it.event){
                    is Event.Deleted ->{
                        delete(note)
                    }
                    is Event.NewNote ->{
                        val newNote = it.event.newNote(note, account)
                        add(newNote)
                    }
                }
            }


        }

    }
}