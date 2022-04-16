package jp.panta.misskeyandroidclient.ui.notes.viewmodel

import net.pantasystem.milktea.data.api.misskey.notes.NoteRequest
import jp.panta.misskeyandroidclient.util.BodyLessResponse
import net.pantasystem.milktea.data.model.account.page.Pageable

interface NotePagedStore {
    //val timelineRequestBase: NoteRequest.Setting
    val pageableTimeline: Pageable


    suspend fun loadOld(untilId: String): Pair<BodyLessResponse, List<PlaneNoteViewData>?>
    suspend fun loadNew(sinceId: String): Pair<BodyLessResponse, List<PlaneNoteViewData>?>
    suspend fun loadInit(request: NoteRequest? = null): Pair<BodyLessResponse, List<PlaneNoteViewData>?>
}