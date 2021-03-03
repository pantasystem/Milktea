package jp.panta.misskeyandroidclient.gettters

import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.api.notes.toEntities
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.NoteRelation
import jp.panta.misskeyandroidclient.model.notes.NoteRepository
import jp.panta.misskeyandroidclient.model.users.UserRepository

class NoteRelationGetter(
    private val noteRepository: NoteRepository,
    private val userRepository: UserRepository
) {

    suspend fun get(noteId: Note.Id, deep: Boolean = true): NoteRelation? {
        return runCatching {
            noteRepository.get(noteId)
        }.getOrNull()?.let{
            get(it, deep)
        }
    }

    suspend fun get(accountId: Long, noteId: String): NoteRelation? {
        return get(Note.Id(accountId, noteId))
    }

    suspend fun get(account: Account, noteDTO: NoteDTO): NoteRelation {
        val entities = noteDTO.toEntities(account)
        userRepository.addAll(entities.third)
        noteRepository.addAll(entities.second)
        return get(entities.first)
    }

    suspend fun get(note: Note, deep: Boolean = true): NoteRelation {
        val user = userRepository.get(note.userId)
        return NoteRelation(
            note = note,
            user = user,
            renote = if(deep) {
                note.renoteId?.let{
                    get(it, false)
                }
            }else null,
            reply = if(deep) {
                note.replyId?.let{
                    get(it, false)
                }
            }else null

        )
    }
}