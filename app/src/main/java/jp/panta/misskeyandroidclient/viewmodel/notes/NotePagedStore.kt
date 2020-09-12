package jp.panta.misskeyandroidclient.viewmodel.notes

import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.util.BodyLessResponse
import jp.panta.misskeyandroidclient.model.account.page.Page

interface NotePagedStore {
    //val timelineRequestBase: NoteRequest.Setting
    val pageableTimeline: Page
    val account: Account?

    fun loadOld(untilId: String): Pair<BodyLessResponse, List<PlaneNoteViewData>?>
    fun loadNew(sinceId: String): Pair<BodyLessResponse, List<PlaneNoteViewData>?>
    fun loadInit(request: NoteRequest? = null): Pair<BodyLessResponse, List<PlaneNoteViewData>?>
}