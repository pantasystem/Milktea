package jp.panta.misskeyandroidclient.model.notes.timelines

import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.NoteType
import jp.panta.misskeyandroidclient.util.BodyLessResponse
import jp.panta.misskeyandroidclient.viewmodel.notes.NotePagedStore
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData
import retrofit2.Response

class NoteTimelineStore(
    override val connectionInstance: ConnectionInstance,
    override val timelineRequestBase: NoteRequest.Setting,
    misskeyAPI: MisskeyAPI
) : NotePagedStore{

    private val timelineStore = when(timelineRequestBase.type){
        NoteType.HOME -> misskeyAPI::homeTimeline
        NoteType.LOCAL -> misskeyAPI::localTimeline
        NoteType.SOCIAL -> misskeyAPI::hybridTimeline
        NoteType.GLOBAL -> misskeyAPI::globalTimeline
        NoteType.SEARCH -> misskeyAPI::searchNote
        NoteType.SEARCH_HASH -> misskeyAPI::searchByTag
        NoteType.USER -> misskeyAPI::userNotes

    }
    override fun loadInit(request: NoteRequest?): Pair<BodyLessResponse, List<PlaneNoteViewData>?> {
        val res = if(request == null){
            val req = timelineRequestBase.buildRequest(connectionInstance, NoteRequest.Conditions())
            timelineStore(req).execute()
        }else{
            timelineStore(request).execute()
        }
        return makeResponse(res.body(), res)
    }

    override fun loadNew(sinceId: String): Pair<BodyLessResponse, List<PlaneNoteViewData>?> {
        val req = timelineRequestBase.buildRequest(connectionInstance, NoteRequest.Conditions(sinceId = sinceId))
        val res = timelineStore(req).execute()
        val reversedList = res.body()?.asReversed()
        return makeResponse(reversedList, res)
    }

    override fun loadOld(untilId: String): Pair<BodyLessResponse, List<PlaneNoteViewData>?> {
        val req = timelineRequestBase.buildRequest(connectionInstance, NoteRequest.Conditions(untilId = untilId))
        val res = timelineStore(req).execute()
        return makeResponse(res.body(), res)
    }

    private fun makeResponse(list: List<Note>?, response: Response<List<Note>?>): Pair<BodyLessResponse, List<PlaneNoteViewData>?>{
        return Pair<BodyLessResponse, List<PlaneNoteViewData>?>(BodyLessResponse(response), list?.map{ PlaneNoteViewData(it) })
    }
}