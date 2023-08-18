package net.pantasystem.milktea.data.infrastructure.note.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.data.infrastructure.drive.FileUploader
import net.pantasystem.milktea.data.infrastructure.drive.UploadSource
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.file.AppFile
import net.pantasystem.milktea.model.note.*
import java.io.Serializable
import net.pantasystem.milktea.api.misskey.notes.CreateNote as CreateNoteDTO
import net.pantasystem.milktea.api.misskey.notes.ReactionAcceptanceType as RAT


class PostNoteTask(
    val createNote: CreateNote,
    val account: Account,
    loggerFactory: Logger.Factory,
    val filePropertyDataSource: FilePropertyDataSource,
) : Serializable {


    private val logger = loggerFactory.create("PostNoteTask")
    private var filesIds: List<FileProperty.Id>? = null


    suspend fun execute(fileUploader: FileUploader): CreateNoteDTO? {
        val ok = if (createNote.files.isNullOrEmpty()) {
            true
        } else {
            executeFileUpload(fileUploader)
        }
        return if (ok) {
            logger.debug("投稿データを作成しました。")
            CreateNoteDTO(
                i = createNote.author.token,
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
                fileIds = filesIds?.map { it.fileId },
                channelId = createNote.channelId?.channelId,
                reactionAcceptance = when (createNote.reactionAcceptance) {
                    ReactionAcceptanceType.LikeOnly -> RAT.LikeOnly
                    ReactionAcceptanceType.LikeOnly4Remote -> RAT.LikeOnly4Remote
                    ReactionAcceptanceType.NonSensitiveOnly -> RAT.NonSensitiveOnly
                    ReactionAcceptanceType.NonSensitiveOnly4LocalOnly4Remote -> RAT.NonSensitiveOnly4LocalOnly4Remote
                    null -> null
                },
            )
        } else {
            logger.error("投稿データ作成に失敗しました。")
            null
        }

    }

    private suspend fun executeFileUpload(fileUploader: FileUploader): Boolean {
        val tmpFiles = createNote.files
        filesIds = coroutineScope {
            runCancellableCatching {
                tmpFiles?.map {
                    async(Dispatchers.IO) {
                        when (it) {
                            is AppFile.Remote -> {
                                if (account.accountId == it.id.accountId) {
                                    it.id
                                } else {
                                    val result = fileUploader.upload(
                                        UploadSource.OtherAccountFile(
                                            filePropertyDataSource.find(it.id).getOrThrow()
                                        ), true
                                    )
                                    result.id
                                }
                            }

                            is AppFile.Local -> {
                                val result = fileUploader.upload(UploadSource.LocalFile(it), true)
                                result.id
                            }
                        }
                    }
                }?.awaitAll()
            }.getOrNull()

        }
        return tmpFiles != null && tmpFiles.size == filesIds?.size
    }

//    fun toDraftNote(draftNote: DraftNote? = null): DraftNote {
//        logger.debug("下書きノートが作成された")
//        val draftPoll = createNote.poll?.let{
//            DraftPoll(it.choices, it.multiple, it.expiresAt)
//        }
//
//        return DraftNote(
//            accountId = account.accountId,
//            text = createNote.text,
//            cw = createNote.cw,
//            visibleUserIds = createNote.visibleUserIds(),
//            draftPoll = draftPoll,
//            visibility = createNote.visibility.type(),
//            localOnly = createNote.visibility.isLocalOnly(),
//            renoteId = createNote.renoteId?.noteId,
//            replyId = createNote.replyId?.noteId,
//            channelId = createNote.channelId,
//            draftNoteId = draftNote?.draftNoteId ?: 0L,
//            draftFiles =
//        )
//    }
//


}