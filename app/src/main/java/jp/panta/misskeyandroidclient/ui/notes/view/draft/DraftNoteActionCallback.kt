package jp.panta.misskeyandroidclient.ui.notes.view.draft

import net.pantasystem.milktea.model.notes.draft.DraftNote

interface DraftNoteActionCallback {

    fun onSelect(draftNote: net.pantasystem.milktea.model.notes.draft.DraftNote?)

    fun onDelete(draftNote: net.pantasystem.milktea.model.notes.draft.DraftNote?)

}