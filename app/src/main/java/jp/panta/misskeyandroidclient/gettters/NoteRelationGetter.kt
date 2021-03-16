package jp.panta.misskeyandroidclient.gettters

import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.api.notes.toEntities
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.NoteRelation
import jp.panta.misskeyandroidclient.model.notes.NoteRepository
import jp.panta.misskeyandroidclient.model.users.UserDataSource

class NoteRelationGetter(
    private val noteRepository: NoteRepository,
    private val userDataSource: UserDataSource
) {

    suspend fun get(noteId: Note.Id, deep: Boolean = true, featuredId: String? = null, promotionId: String? = null): NoteRelation? {
        return runCatching {
            noteRepository.get(noteId)
        }.getOrNull()?.let{
            get(it, deep, featuredId = featuredId, promotionId = promotionId)
        }
    }

    suspend fun get(accountId: Long, noteId: String, featuredId: String? = null, promotionId: String? = null): NoteRelation? {
        return get(Note.Id(accountId, noteId), featuredId = featuredId, promotionId = promotionId)
    }

    suspend fun get(account: Account, noteDTO: NoteDTO): NoteRelation {
        val entities = noteDTO.toEntities(account)
        userDataSource.addAll(entities.third)
        noteRepository.addAll(entities.second)
        return get(entities.first, featuredId = noteDTO.tmpFeaturedId, promotionId = noteDTO.promotionId)
    }

    suspend fun get(note: Note, deep: Boolean = true, featuredId: String? = null, promotionId: String? = null): NoteRelation {
        val user = userDataSource.get(note.userId)

        val renote = if(deep) {
            note.renoteId?.let{
                get(it, false)
            }
        } else null
        val reply = if(deep) {
            note.replyId?.let{
                get(it, false)
            }
        }else null

        if(featuredId != null) {
            return NoteRelation.Featured(
                note,
                user,
                renote,
                reply,
                featuredId
            )
        }

        if(promotionId != null) {
            return NoteRelation.Promotion(
                note,
                user,
                renote,
                reply,
                promotionId
            )
        }
        return NoteRelation.Normal(
            note = note,
            user = user,
            renote = renote,
            reply = reply
        )
    }
}