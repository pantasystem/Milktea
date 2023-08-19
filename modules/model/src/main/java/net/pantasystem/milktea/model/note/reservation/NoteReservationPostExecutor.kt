package net.pantasystem.milktea.model.note.reservation

import net.pantasystem.milktea.model.note.draft.DraftNote

interface NoteReservationPostExecutor {
    fun register(draftNote: DraftNote)
}