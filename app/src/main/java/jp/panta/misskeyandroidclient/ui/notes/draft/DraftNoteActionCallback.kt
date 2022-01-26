package jp.panta.misskeyandroidclient.ui.notes.draft

import jp.panta.misskeyandroidclient.model.notes.draft.DraftNote

interface DraftNoteActionCallback {

    fun onSelect(draftNote: DraftNote?)

    fun onDelete(draftNote: DraftNote?)

}