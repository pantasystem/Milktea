package net.pantasystem.milktea.note.draft

import net.pantasystem.milktea.model.notes.draft.DraftNote

interface DraftNoteActionCallback {

    fun onSelect(draftNote: DraftNote?)

    fun onDelete(draftNote: DraftNote?)

}