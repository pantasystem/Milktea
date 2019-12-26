package jp.panta.misskeyandroidclient.viewmodel.notes

import android.util.Log
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.auth.ConnectionInstance
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.NoteType
import jp.panta.misskeyandroidclient.util.BodyLessResponse
import retrofit2.Response
import java.lang.Exception
import java.lang.IllegalArgumentException

class NoteTimelineStore(
    override val connectionInstance: ConnectionInstance,
    override val timelineRequestBase: NoteRequest.Setting,
    misskeyAPI: MisskeyAPI,
    private val encryption: Encryption
) : NotePagedStore{

    private val timelineStore = when(timelineRequestBase.type){
        NoteType.HOME -> misskeyAPI::homeTimeline
        NoteType.LOCAL -> misskeyAPI::localTimeline
        NoteType.SOCIAL -> misskeyAPI::hybridTimeline
        NoteType.GLOBAL -> misskeyAPI::globalTimeline
        NoteType.SEARCH -> misskeyAPI::searchNote
        NoteType.SEARCH_HASH -> misskeyAPI::searchByTag
        NoteType.USER -> misskeyAPI::userNotes
        NoteType.FEATURED -> misskeyAPI::featured
        NoteType.FAVORITE -> throw IllegalArgumentException("use FavoriteNotePagingStore.kt")
        NoteType.DETAIL -> throw IllegalArgumentException("use NoteDetailFragment")
    }
    override fun loadInit(request: NoteRequest?): Pair<BodyLessResponse, List<PlaneNoteViewData>?> {
        val res = if(request == null){
            val req = timelineRequestBase.buildRequest(connectionInstance, NoteRequest.Conditions(), encryption)
            timelineStore(req).execute()
        }else{
            timelineStore(request).execute()
        }
        return makeResponse(res.body(), res)
    }

    override fun loadNew(sinceId: String): Pair<BodyLessResponse, List<PlaneNoteViewData>?> {
        val req = timelineRequestBase.buildRequest(connectionInstance, NoteRequest.Conditions(sinceId = sinceId), encryption)
        val res = timelineStore(req).execute()
        val reversedList = res.body()?.asReversed()
        return makeResponse(reversedList, res)
    }

    override fun loadOld(untilId: String): Pair<BodyLessResponse, List<PlaneNoteViewData>?> {
        val req = timelineRequestBase.buildRequest(connectionInstance, NoteRequest.Conditions(untilId = untilId), encryption)
        val res = timelineStore(req).execute()
        return makeResponse(res.body(), res)
    }

    private fun makeResponse(list: List<Note>?, response: Response<List<Note>?>): Pair<BodyLessResponse, List<PlaneNoteViewData>?>{
        if(response.code() != 200){
            Log.e("NoteTimelineStore", "異常ステータス受信:${response.code()}, :${response.errorBody()?.string()}")
        }
        return Pair<BodyLessResponse, List<PlaneNoteViewData>?>(BodyLessResponse(response), list?.map{
            try{
                if(it.reply == null){
                    PlaneNoteViewData(it, connectionInstance)
                }else{
                    HasReplyToNoteViewData(it, connectionInstance)
                }
            }catch(e: Exception){
                Log.d("NoteTimelineStore", "パース中にエラー発生: $it", e)
                null
            }

        }?.filterNotNull())
    }
}