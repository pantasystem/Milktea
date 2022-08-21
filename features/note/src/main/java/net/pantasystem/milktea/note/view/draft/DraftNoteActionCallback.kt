package net.pantasystem.milktea.note.view.draft

import net.pantasystem.milktea.model.notes.draft.DraftNote

interface DraftNoteActionCallback {

    fun onSelect(draftNote: DraftNote?)

    fun onDelete(draftNote: DraftNote?)

}