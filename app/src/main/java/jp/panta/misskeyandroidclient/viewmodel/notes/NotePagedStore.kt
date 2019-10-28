package jp.panta.misskeyandroidclient.viewmodel.notes

import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.util.BodyLessResponse

interface NotePagedStore {
    val timelineRequestBase: NoteRequest.Setting
    val connectionInstance: ConnectionInstance

    fun loadOld(untilId: String): Pair<BodyLessResponse, List<PlaneNoteViewData>?>
    fun loadNew(sinceId: String): Pair<BodyLessResponse, List<PlaneNoteViewData>?>
    fun loadInit(request: NoteRequest? = null): Pair<BodyLessResponse, List<PlaneNoteViewData>?>
}