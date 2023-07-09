package net.pantasystem.milktea.model.notes.draft

import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.file.AppFile
import net.pantasystem.milktea.model.file.FilePreviewSource
import net.pantasystem.milktea.model.file.from
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteRelation
import net.pantasystem.milktea.model.notes.ReactionAcceptanceType
import net.pantasystem.milktea.model.notes.getName
import net.pantasystem.milktea.model.notes.isLocalOnly
import java.io.Serializable
import java.util.Date


data class DraftNote(
    val accountId: Long,
    val visibility: String = "public",
    var visibleUserIds: List<String>? = null,
    val text: String?,
    val cw: String? = null,
    var draftFiles: List<DraftNoteFile>? = null,
    val viaMobile: Boolean? = null,
    val localOnly: Boolean? = null,
    val noExtractMentions: Boolean? = null,
    val noExtractHashtags: Boolean? = null,
    val noExtractEmojis: Boolean? = null,
    val replyId: String? = null,
    val renoteId: String? = null,
    val draftPoll: DraftPoll? = null,
    val reservationPostingAt: Date? = null,
    val channelId: Channel.Id? = null,
    val isSensitive: Boolean? = null,
    val reactionAcceptanceType: ReactionAcceptanceType? = null,
    var draftNoteId: Long = 0L
) : Serializable {
    val filePreviewSources: List<FilePreviewSource> by lazy {
        draftFiles?.map {
            when (val appFile = AppFile.from(it)) {
                is AppFile.Local -> FilePreviewSource.Local(appFile)
                is AppFile.Remote -> FilePreviewSource.Remote(
                    appFile,
                    (it as DraftNoteFile.Remote).fileProperty
                )
            }
        } ?: emptyList()
    }
    val appFiles: List<AppFile>
        get() = draftFiles?.map { draftNoteFile ->
            AppFile.from(draftNoteFile)
        } ?: emptyList()
}

sealed interface DraftNoteFile {

    data class Remote(val fileProperty: FileProperty) : DraftNoteFile
    data class Local(
        val name: String,
        val filePath: String,
        val isSensitive: Boolean?,
        val type: String,
        val thumbnailUrl: String?,
        val folderId: String?,
        val fileSize: Long?,
        val localFileId: Long,
        val comment: String?
    ) : DraftNoteFile {
        companion object
    }
}

fun DraftNoteFile.Local.Companion.from(appFile: AppFile.Local, id: Long = 0L): DraftNoteFile.Local {
    return DraftNoteFile.Local(
        filePath = appFile.path,
        folderId = appFile.folderId,
        isSensitive = appFile.isSensitive,
        type = appFile.type,
        name = appFile.name,
        thumbnailUrl = appFile.thumbnailUrl,
        fileSize = appFile.fileSize,
        localFileId = id,
        comment = appFile.comment,
    )
}

fun NoteRelation.toDraftNote(): DraftNote {
    return DraftNote(
        accountId = this.note.id.accountId,
        visibility = this.note.visibility.getName(),
        visibleUserIds = this.note.visibleUserIds?.map {
            it.id
        },
        text = (this.note.type as? Note.Type.Mastodon)?.pureText ?: this.note.text,
        cw = this.note.cw,
        draftFiles = this.files?.map {
            DraftNoteFile.Remote(it)
        },
        viaMobile = this.note.viaMobile,
        localOnly = this.note.visibility.isLocalOnly(),
        renoteId = this.note.renoteId.let {
            it?.noteId
        },
        replyId = this.note.replyId.let {
            it?.noteId
        },
        draftPoll = this.note.poll?.toDraftPoll(),
        channelId = this.note.channelId,
        isSensitive = (this.note.type as? Note.Type.Mastodon)?.isSensitive
    )
}