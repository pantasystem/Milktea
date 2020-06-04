package jp.panta.misskeyandroidclient.view.notes.draft

import jp.panta.misskeyandroidclient.model.notes.draft.DraftNote

interface DraftNoteActionCallback {

    fun onSelect(draftNote: DraftNote?)
}