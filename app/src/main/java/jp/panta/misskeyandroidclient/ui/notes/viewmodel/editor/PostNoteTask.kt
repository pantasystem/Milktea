package jp.panta.misskeyandroidclient.ui.notes.viewmodel.editor

import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.drive.FilePropertyDataSource
import jp.panta.misskeyandroidclient.model.drive.FileUploader
import jp.panta.misskeyandroidclient.model.file.AppFile
import jp.panta.misskeyandroidclient.model.notes.*
import jp.panta.misskeyandroidclient.api.misskey.notes.CreateNote as CreateNoteDTO
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNote
import jp.panta.misskeyandroidclient.model.notes.draft.DraftPoll
import kotlinx.coroutines.*
import java.io.Serializable

class PostNoteTask(
    val encryption: Encryption,
    val createNote: CreateNote,
    val account: Account,
    loggerFactory: Logger.Factory,
    val filePropertyDataSource: FilePropertyDataSource
): Serializable{



    private val logger = loggerFactory.create("PostNoteTask")
    private var filesIds: List<String>? = null


    
    suspend fun execute(fileUploader: FileUploader): CreateNoteDTO?{
         val ok = if(createNote.files.isNullOrEmpty()){
             true
         }else{
             executeFileUpload(fileUploader)
        }
        return if(ok){
            logger.debug("投稿データを作成しました。")
            CreateNoteDTO(
                i = createNote.author.getI(encryption),
                visibility = createNote.visibility.type(),
                localOnly = (createNote.visibility as? CanLocalOnly)?.isLocalOnly,
                visibleUserIds = createNote.visibleUserIds(),
                text = createNote.text,
                cw = createNote.cw,
                viaMobile = createNote.viaMobile,
                noExtractEmojis = createNote.noExtractEmojis,
                noExtractMentions = createNote.noExtractMentions,
                noExtractHashtags = createNote.noExtractHashtags,
                replyId = createNote.replyId?.noteId,
                renoteId = createNote.renoteId?.noteId,
                poll = createNote.poll,
                fileIds = filesIds
                )
        }else{
            logger.error("投稿データ作成に失敗しました。")
            null
        }

    }

    private suspend fun executeFileUpload(fileUploader: FileUploader): Boolean{
        val tmpFiles = createNote.files
        filesIds = coroutineScope {
            runCatching {
                tmpFiles?.map {
                    async(Dispatchers.IO) {
                        when(it) {
                            is AppFile.Remote -> it.id.fileId
                            is AppFile.Local -> {
                                val result = fileUploader.upload(it, true)
                                filePropertyDataSource.add(result.toFileProperty(account))
                                result.id
                            }
                        }
                    }
                }?.awaitAll()
            }.getOrNull()

        }
        return tmpFiles != null && tmpFiles.size == filesIds?.size
    }

    fun toDraftNote(draftNote: DraftNote? = null): DraftNote{
        logger.debug("下書きノートが作成された")
        val draftPoll = createNote.poll?.let{
            DraftPoll(it.choices, it.multiple, it.expiresAt)
        }

        return DraftNote(
            accountId = account.accountId,
            text = createNote.text,
            cw = createNote.cw,
            visibleUserIds = createNote.visibleUserIds(),
            draftPoll = draftPoll,
            visibility = createNote.visibility.type(),
            localOnly = createNote.visibility.isLocalOnly(),
            renoteId = createNote.renoteId?.noteId,
            replyId = createNote.replyId?.noteId
        ).apply{
            this.draftNoteId = draftNote?.draftNoteId
        }
    }



}