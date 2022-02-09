package jp.panta.misskeyandroidclient.model.notes.reservation

import jp.panta.misskeyandroidclient.model.notes.draft.DraftNote
import java.util.*

interface NoteReservationPostExecutor {
    fun register(draftNote: DraftNote)
}