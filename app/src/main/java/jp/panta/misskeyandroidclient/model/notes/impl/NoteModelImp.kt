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
import jp.panta.misskeyandroidclient.model.notes.CreateNote
import jp.panta.misskeyandroidclient.model.users.UserRepository
import jp.panta.misskeyandroidclient.viewmodel.notes.editor.PostNoteTask
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import java.io.IOException
import java.lang.Exception
import java.lang.IllegalStateException
import java.net.SocketTimeoutException
import kotlin.jvm.Throws
import jp.panta.misskeyandroidclient.api.notes.CreateNote as CreateNoteDTO

class NoteModelImp(
    private val noteRepository: NoteRepository,
    private val userRepository: UserRepository,
    private val account: Account,
    private val encryption: Encryption,
    private val misskeyAPI: MisskeyAPI,
) : NoteModel{




    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(IOException::class, UnauthorizedException::class, SocketTimeoutException::class)
    override suspend fun delete(note: Note) {
        misskeyAPI.delete(DeleteNote(i = account.getI(encryption), note.id.noteId)).execute()
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    @Throws(IOException::class, UnauthorizedException::class, SocketTimeoutException::class)
    override suspend fun get(noteId: Note.Id): Note? {
        var n = noteRepository.get(noteId)
        if(n == null){
            val res = misskeyAPI.showNote(NoteRequest(i = account.getI(encryption), noteId = noteId.noteId)).execute()
            val noteDTO = res.body()
            if(noteDTO != null){
                //n = this.add(noteDTO)
                val entities = noteDTO.toEntities(account)
                val notesAddedCount = entities.second.count {
                    noteRepository.add(it) != AddResult.CANCEL || userRepository.get(it.userId) != null
                }
                val usersAddedCount = entities.third.count {
                    userRepository.add(it) != AddResult.CANCEL || userRepository.get(it.id) != null
                }

                // repositoryに正常に加えられなかった場合異常
                if( !(notesAddedCount == entities.second.size && usersAddedCount == entities.third.size)){
                    throw IllegalStateException("Repositoryに正常に加えられませんでした")
                }
                n = entities.first
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
            noteId = reactionTo.id.noteId
        )).execute()

    }



    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun unreaction(reaction: String, unreactionTo: Note) {
        misskeyAPI.deleteReaction(
            DeleteNote(
                i = account.getI(encryption),
                noteId = unreactionTo.id.noteId
            )
        ).execute()
    }

    override suspend fun create(createNote: CreateNote) {
        
    }



}