package net.pantasystem.milktea.data.infrastructure.notes

import net.pantasystem.milktea.api.misskey.notes.NoteRequest
import net.pantasystem.milktea.model.account.page.PageParams


fun PageParams.toNoteRequest(i: String?) : NoteRequest {

    return NoteRequest(
        i = i,
        withFiles = withFiles,
        excludeNsfw = excludeNsfw,
        includeMyRenotes = includeMyRenotes,
        includeLocalRenotes = includeLocalRenotes,
        includeRenotedMyNotes = includeRenotedMyRenotes,
        listId = listId,
        following = following,
        visibility = visibility,
        noteId = noteId,
        tag = tag,
        reply = reply,
        renote = renote,
        poll = poll,
        offset = offset,
        markAsRead = markAsRead,
        channelId = channelId,
        userId = userId
    )
}