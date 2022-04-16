package net.pantasystem.milktea.data.model.notes.reservation

import net.pantasystem.milktea.data.model.notes.draft.DraftNote
import java.util.*

interface NoteReservationPostExecutor {
    fun register(draftNote: DraftNote)
}