package net.pantasystem.milktea.model.notes.reservation

import net.pantasystem.milktea.model.notes.draft.DraftNote

interface NoteReservationPostExecutor {
    fun register(draftNote: DraftNote)
}