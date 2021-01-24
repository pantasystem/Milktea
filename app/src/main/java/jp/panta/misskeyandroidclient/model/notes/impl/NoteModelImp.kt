package jp.panta.misskeyandroidclient.model.notes.impl

import android.util.Log
import jp.panta.misskeyandroidclient.api.notes.*
import jp.panta.misskeyandroidclient.api.users.toUser
import jp.panta.misskeyandroidclient.model.AddResult
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.UnauthorizedException
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.notes.*
import jp.panta.misskeyandroidclient.model.users.UserRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import java.io.IOException
import java.lang.Exception
import java.net.SocketTimeoutException
import kotlin.jvm.Throws

class NoteModelImp(
    private val noteRepository: NoteRepository,
    private val userRepository: UserRepository,
    private val account: Account,
    private val encryption: Encryption,
    private val misskeyAPI: MisskeyAPI,
    private val noteCapture: NoteCapture,
) : NoteModel{




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
        misskeyAPI.createReaction(CreateReaction(
            i = account.getI(encryption),
            reaction = reaction,
            noteId = reactionTo.id
        )).execute()

    }



    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun unreaction(reaction: String, unreactionTo: Note) {
        misskeyAPI.deleteReaction(
            DeleteNote(
                i = account.getI(encryption),
                noteId = unreactionTo.id
            )
        ).execute()
    }

    suspend fun add(note: NoteDTO): Note?{
        if(noteRepository.add(note.toNote()) == AddResult.CREATED){
            noteCapture.capture(note.id)
        }
        userRepository.add(note.user.toUser())

        if(note.reNote != null) {
            this.add(note.reNote)
        }
        if(note.reply != null){
            this.add(note.reply)
        }

        return noteRepository.get(note.id)


    }



    fun startListen(coroutineScope: CoroutineScope){
        coroutineScope.launch(Dispatchers.IO) {
            try{
                startCapture()
            } catch (e: Exception){
                Log.e("NoteModelImpl", "capture中に致命的なエラー発生", e)
            }

        }

        coroutineScope.launch(Dispatchers.IO) {
            try{
                listenUserRepository()
            } catch (e: Exception){
                Log.e("NoteModelImpl", "listen user repository中に致命的なエラー発生", e)
            }
        }
    }


    private suspend fun startCapture(){
        noteCapture.observer().collect {
            val note = noteRepository.get(it.noteId)
            if(note != null) {
                when (it.event) {
                    is Event.Deleted -> {
                        noteRepository.remove(it.noteId)
                    }
                    is Event.NewNote -> {
                        val newNote = it.event.newNote(note, account)
                        noteRepository.add(newNote)
                    }
                }
            }


        }

    }

    private suspend fun listenUserRepository(){
        userRepository.observable().collect {
            if(it is UserRepository.Event.Removed){
                noteRepository.removeByUserId(it.userId)
            }
        }
    }
}