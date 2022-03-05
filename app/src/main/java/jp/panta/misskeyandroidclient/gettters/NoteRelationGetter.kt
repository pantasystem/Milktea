package jp.panta.misskeyandroidclient.gettters

import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.model.drive.FilePropertyDataSource
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.NoteRelation
import jp.panta.misskeyandroidclient.model.notes.NoteRepository
import jp.panta.misskeyandroidclient.model.users.UserDataSource

class NoteRelationGetter(
    private val noteRepository: NoteRepository,
    private val userDataSource: UserDataSource,
    private val filePropertyDataSource: FilePropertyDataSource,
    private val logger: Logger
) {

    suspend fun get(noteId: Note.Id, deep: Boolean = true, featuredId: String? = null, promotionId: String? = null): NoteRelation? {
        return runCatching {
            noteRepository.find(noteId)
        }.onFailure {
            logger.error("ノートの取得に失敗しました", e = it)
        }.getOrNull()?.let{
            get(it, deep, featuredId = featuredId, promotionId = promotionId)
        }
    }

    suspend fun get(accountId: Long, noteId: String, featuredId: String? = null, promotionId: String? = null): NoteRelation? {
        return get(Note.Id(accountId, noteId), featuredId = featuredId, promotionId = promotionId)
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
                note.fileIds?.let { filePropertyDataSource.findIn(it) },
                featuredId
            )
        }

        if(promotionId != null) {
            return NoteRelation.Promotion(
                note,
                user,
                renote,
                reply,
                note.fileIds?.let { filePropertyDataSource.findIn(it) },
                promotionId
            )
        }
        return NoteRelation.Normal(
            note = note,
            user = user,
            renote = renote,
            reply = reply,
            note.fileIds?.let { filePropertyDataSource.findIn(it) },
        )
    }
}