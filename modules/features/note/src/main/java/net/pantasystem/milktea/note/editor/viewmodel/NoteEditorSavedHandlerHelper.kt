package net.pantasystem.milktea.note.editor.viewmodel

import androidx.lifecycle.SavedStateHandle
import kotlinx.datetime.Instant
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.drive.DriveFileRepository
import net.pantasystem.milktea.model.file.AppFile
import net.pantasystem.milktea.model.file.FilePreviewSource
import net.pantasystem.milktea.model.note.Note
import net.pantasystem.milktea.model.note.PollEditingState
import net.pantasystem.milktea.model.note.ReactionAcceptanceType
import net.pantasystem.milktea.model.note.Visibility
import java.util.Date

enum class NoteEditorSavedStateKey() {
    Text, Cw, PickedFiles, Visibility, ChannelId, ReplyId, RenoteId, ScheduleAt, DraftNoteId, HasCW, Poll, IsSensitive, ReactionAcceptance,
}


fun SavedStateHandle.setText(text: String?) {
    this[NoteEditorSavedStateKey.Text.name] = text
}

fun SavedStateHandle.getText(): String? {
    return this[NoteEditorSavedStateKey.Text.name]
}

fun SavedStateHandle.getCw(): String? {
    return this[NoteEditorSavedStateKey.Cw.name]
}

fun SavedStateHandle.setCw(text: String?) {
    this[NoteEditorSavedStateKey.Cw.name] = text
}

fun SavedStateHandle.setFiles(files: List<AppFile>) {
    this[NoteEditorSavedStateKey.PickedFiles.name] = files
}

fun SavedStateHandle.getFiles(): List<AppFile> {
    return this[NoteEditorSavedStateKey.PickedFiles.name] ?: emptyList()
}

fun SavedStateHandle.setChannelId(channelId: Channel.Id?) {
    this[NoteEditorSavedStateKey.ChannelId.name] = channelId
}

fun SavedStateHandle.setSensitive(value: Boolean?) {
    this[NoteEditorSavedStateKey.IsSensitive.name] = value
}

fun SavedStateHandle.getChannelId(): Channel.Id? {
    return this[NoteEditorSavedStateKey.ChannelId.name]
}


fun SavedStateHandle.setReplyId(noteId: Note.Id?) {
    this[NoteEditorSavedStateKey.ReplyId.name] = noteId
}

fun SavedStateHandle.setRenoteId(noteId: Note.Id?) {
    this[NoteEditorSavedStateKey.RenoteId.name] = noteId
}

fun SavedStateHandle.getRenoteId(): Note.Id? {
    return this[NoteEditorSavedStateKey.RenoteId.name]
}

fun SavedStateHandle.getReplyId(): Note.Id? {
    return this[NoteEditorSavedStateKey.ReplyId.name]
}

fun SavedStateHandle.setScheduleAt(date: Date?) {
    this[NoteEditorSavedStateKey.ScheduleAt.name] = date
}

fun SavedStateHandle.getScheduleAt(): Date? {
    return this[NoteEditorSavedStateKey.ScheduleAt.name]
}

fun SavedStateHandle.setHasCw(hasCw: Boolean) {
    this[NoteEditorSavedStateKey.HasCW.name] = hasCw
}

fun SavedStateHandle.getHasCw(): Boolean {
    return this[NoteEditorSavedStateKey.HasCW.name] ?: false
}

fun SavedStateHandle.getPoll(): PollEditingState? {
    return this[NoteEditorSavedStateKey.Poll.name]
}

fun SavedStateHandle.setPoll(value: PollEditingState?) {
    this[NoteEditorSavedStateKey.Poll.name] = value
}

fun SavedStateHandle.setReactionAcceptanceType(value: ReactionAcceptanceType?) {
    this[NoteEditorSavedStateKey.ReactionAcceptance.name] = value?.name
}
fun SavedStateHandle.setVisibility(visibility: Visibility?) {
    this[NoteEditorSavedStateKey.Visibility.name] = visibility
}

fun SavedStateHandle.getVisibility(): Visibility? {
    return this[NoteEditorSavedStateKey.Visibility.name]
}

fun SavedStateHandle.setDraftNoteId(id: Long?) {
    this[NoteEditorSavedStateKey.DraftNoteId.name] = id
}

fun SavedStateHandle.getDraftNoteId(): Long? {
    return this[NoteEditorSavedStateKey.DraftNoteId.name]
}

fun SavedStateHandle.getSensitive(): Boolean {
    return this[NoteEditorSavedStateKey.IsSensitive.name] ?: false
}

fun SavedStateHandle.getReactionAcceptance(): ReactionAcceptanceType? {
    return this.get<String?>(NoteEditorSavedStateKey.ReactionAcceptance.name).let { type ->
        ReactionAcceptanceType.values().find { it.name == type }
    }
}

fun SavedStateHandle.applyBy(note: NoteEditorUiState) {
    setVisibility(note.sendToState.visibility)
    setText(note.formState.text)
    setCw(note.formState.cw)
    setHasCw(note.formState.hasCw)
    setFiles(note.files.map { it.file })
    setReplyId(note.sendToState.replyId)
    setRenoteId(note.sendToState.renoteId)
    setPoll(note.poll)
    setDraftNoteId(note.sendToState.draftNoteId)
    setChannelId(note.sendToState.channelId)
    setScheduleAt(
        note.sendToState.schedulePostAt?.let {
            Date(it.toEpochMilliseconds())
        }
    )
    setSensitive(
        note.formState.isSensitive,
    )
    setReactionAcceptanceType(note.sendToState.reactionAcceptanceType)
}

suspend fun SavedStateHandle.getNoteEditingUiState(account: Account?, visibility: Visibility?, fileRepository: DriveFileRepository): NoteEditorUiState {
    return NoteEditorUiState(
        formState = NoteEditorFormState(
            text = getText(),
            cw = getCw(),
            hasCw = getHasCw(),
            isSensitive = getSensitive(),
        ),
        sendToState = NoteEditorSendToState(
            visibility = visibility ?: getVisibility() ?: Visibility.Public(false),
            channelId = getChannelId(),
            renoteId = getRenoteId(),
            replyId = getReplyId(),
            schedulePostAt = getScheduleAt()?.let {
                Instant.fromEpochMilliseconds(it.time)
            },
            draftNoteId = getDraftNoteId(),
            reactionAcceptanceType = getReactionAcceptance()
        ),
        poll = getPoll(),
        files = getFiles().mapNotNull { appFile ->
            when(appFile) {
                is AppFile.Local -> FilePreviewSource.Local(appFile)
                is AppFile.Remote -> runCancellableCatching {
                    FilePreviewSource.Remote(appFile, fileRepository.find(appFile.id))
                }.getOrNull()
            }
        },
        currentAccount = account,
    )
}