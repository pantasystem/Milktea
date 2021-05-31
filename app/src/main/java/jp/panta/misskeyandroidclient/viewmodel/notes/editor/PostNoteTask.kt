package jp.panta.misskeyandroidclient.viewmodel.notes.editor

import com.google.android.exoplayer2.upstream.FileDataSource
import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.drive.FilePropertyDataSource
import jp.panta.misskeyandroidclient.model.drive.FileUploader
import jp.panta.misskeyandroidclient.model.file.File
import jp.panta.misskeyandroidclient.model.notes.*
import jp.panta.misskeyandroidclient.api.notes.CreateNote as CreateNoteDTO
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNote
import jp.panta.misskeyandroidclient.model.notes.draft.DraftPoll
import jp.panta.misskeyandroidclient.model.notes.poll.CreatePoll
import kotlinx.coroutines.*
import java.io.Serializable
import java.util.*

class PostNoteTask(
    //connectionInstance: ConnectionInstance,
    //connectionInformation: EncryptedConnectionInformation,
    val encryption: Encryption,
    val createNote: CreateNote,
    val account: Account,
    loggerFactory: Logger.Factory,
    val filePropertyDataSource: FilePropertyDataSource
    //private val fileUploader: FileUploader
): Serializable{



    private val logger = loggerFactory.create("PostNoteTask")
    private var filesIds: List<String>? = null

    /*private val i: String = account.getI(encryption)
    private var visibleUserIds: List<String>? = null
    var visibility: Visibility = Visibility.Public(true)
    var files: List<File>? = null
    var text: String? = null
    var cw: String? = null
    var viaMobile: Boolean? = null
    var noExtractMentions: Boolean? = null
    var noExtractHashtags: Boolean? = null
    var noExtractEmojis: Boolean? = null
    var replyId: String? = null
    var renoteId: String? = null
    var poll: CreatePoll? = null*/


    
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
                        it.remoteFileId?.fileId ?: fileUploader.upload(it, true).also {
                            filePropertyDataSource.add(it.toFileProperty(account))
                        }.id
                    }
                }?.awaitAll()
            }.getOrNull()?.filterNotNull()

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