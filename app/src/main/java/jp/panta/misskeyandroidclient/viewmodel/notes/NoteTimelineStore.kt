package jp.panta.misskeyandroidclient.viewmodel.notes

import android.util.Log
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.core.EncryptedConnectionInformation
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.NoteType
import jp.panta.misskeyandroidclient.util.BodyLessResponse
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import retrofit2.Call
import retrofit2.Response
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.lang.NullPointerException

class NoteTimelineStore(
    override val accountRelation: AccountRelation,
    override val timelineRequestBase: NoteRequest.Setting,
    private val miCore: MiCore,
    private val encryption: Encryption
) : NotePagedStore{


    private fun getStore(): ((NoteRequest)-> Call<List<Note>?>)? {
        return try{
            when(timelineRequestBase.type){
                NoteType.HOME -> miCore.getMisskeyAPI(accountRelation)!!::homeTimeline
                NoteType.LOCAL -> miCore.getMisskeyAPI(accountRelation)!!::localTimeline
                NoteType.SOCIAL -> miCore.getMisskeyAPI(accountRelation)!!::hybridTimeline
                NoteType.GLOBAL -> miCore.getMisskeyAPI(accountRelation)!!::globalTimeline
                NoteType.SEARCH -> miCore.getMisskeyAPI(accountRelation)!!::searchNote
                NoteType.SEARCH_HASH -> miCore.getMisskeyAPI(accountRelation)!!::searchByTag
                NoteType.USER -> miCore.getMisskeyAPI(accountRelation)!!::userNotes
                NoteType.FEATURED -> miCore.getMisskeyAPI(accountRelation)!!::featured
                NoteType.FAVORITE -> throw IllegalArgumentException("use FavoriteNotePagingStore.kt")
                NoteType.DETAIL -> throw IllegalArgumentException("use NoteDetailFragment")
                NoteType.USER_LIST -> miCore.getMisskeyAPI(accountRelation)!!::userListTimeline
            }
        }catch(e: NullPointerException){
            null
        }

    }
    override fun loadInit(request: NoteRequest?): Pair<BodyLessResponse, List<PlaneNoteViewData>?> {
        val res = if(request == null){
            val req = timelineRequestBase.buildRequest(accountRelation.getCurrentConnectionInformation()!!, NoteRequest.Conditions(), encryption)
            getStore()?.invoke(req)?.execute()
        }else{
            getStore()?.invoke(request)?.execute()
        }
        return makeResponse(res?.body(), res)
    }

    override fun loadNew(sinceId: String): Pair<BodyLessResponse, List<PlaneNoteViewData>?> {
        val req = timelineRequestBase.buildRequest(accountRelation.getCurrentConnectionInformation()!!, NoteRequest.Conditions(sinceId = sinceId), encryption)
        val res = getStore()?.invoke(req)?.execute()
        val reversedList = res?.body()?.asReversed()
        return makeResponse(reversedList, res)
    }

    override fun loadOld(untilId: String): Pair<BodyLessResponse, List<PlaneNoteViewData>?> {
        val req = timelineRequestBase.buildRequest(accountRelation.getCurrentConnectionInformation()!!, NoteRequest.Conditions(untilId = untilId), encryption)
        val res = getStore()?.invoke(req)?.execute()
        return makeResponse(res?.body(), res)
    }

    private fun makeResponse(list: List<Note>?, response: Response<List<Note>?>?): Pair<BodyLessResponse, List<PlaneNoteViewData>?>{
        if(response?.code() != 200){
            Log.e("NoteTimelineStore", "異常ステータス受信:${response?.code()}, :${response?.errorBody()?.string()}")
        }
        return Pair<BodyLessResponse, List<PlaneNoteViewData>?>(BodyLessResponse(response), list?.map{
            try{
                if(it.reply == null){
                    PlaneNoteViewData(it, accountRelation.account)
                }else{
                    HasReplyToNoteViewData(it, accountRelation.account)
                }
            }catch(e: Exception){
                Log.d("NoteTimelineStore", "パース中にエラー発生: $it", e)
                null
            }

        }?.filterNotNull())
    }
}