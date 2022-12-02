package net.pantasystem.milktea.note.editor.viewmodel

import androidx.lifecycle.SavedStateHandle
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.file.AppFile
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.PollEditingState
import net.pantasystem.milktea.model.notes.Visibility
import java.util.*

enum class NoteEditorSavedStateKey() {
    Text, Cw, PickedFiles, Visibility, ChannelId, ReplyId, RenoteId, ScheduleAt, DraftNoteId, HasCW, Poll
}


fun SavedStateHandle.setText(text: String?) {
    this[NoteEditorSavedStateKey.Text.name] = text
}

fun SavedStateHandle.getText(): String? {
    return this[NoteEditorSavedStateKey.Text.name]
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


fun SavedStateHandle.setReplyId(noteId: Note.Id?) {
    this[NoteEditorSavedStateKey.ReplyId.name] = noteId
}

fun SavedStateHandle.setRenoteId(noteId: Note.Id?) {
    this[NoteEditorSavedStateKey.RenoteId.name] = noteId
}


fun SavedStateHandle.setScheduleAt(date: Date?) {
    this[NoteEditorSavedStateKey.ScheduleAt.name] = date
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

fun SavedStateHandle.setVisibility(visibility: Visibility) {
    this[NoteEditorSavedStateKey.Visibility.name] = visibility
}

fun SavedStateHandle.getVisibility(): Visibility {
    return this[NoteEditorSavedStateKey.Visibility.name] ?: Visibility.Public(false)
}

fun SavedStateHandle.setDraftNoteId(id: Long?) {
    this[NoteEditorSavedStateKey.DraftNoteId.name] = id
}