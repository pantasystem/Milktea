package jp.panta.misskeyandroidclient.viewmodel.notes

import android.util.Log
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.Page
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.core.AccountRelation
import jp.panta.misskeyandroidclient.model.core.EncryptedConnectionInformation
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.v12.MisskeyAPIV12
import jp.panta.misskeyandroidclient.util.BodyLessResponse
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import retrofit2.Call
import retrofit2.Response
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.lang.NullPointerException

class NoteTimelineStore(
    override val account: Account,
    //override val timelineRequestBase: NoteRequest.Setting,
    override val pageableTimeline: Page.Timeline,
    val include: NoteRequest.Include,
    private val miCore: MiCore,
    private val encryption: Encryption
) : NotePagedStore{

    private val requestBuilder = NoteRequest.Builder(pageableTimeline, include)

    private fun getStore(): ((NoteRequest)-> Call<List<Note>?>)? {
        return try{
            when(pageableTimeline){
                is Page.GlobalTimeline -> miCore.getMisskeyAPI(account)::globalTimeline
                is Page.LocalTimeline -> miCore.getMisskeyAPI(account)::localTimeline
                is Page.HybridTimeline -> miCore.getMisskeyAPI(account)::hybridTimeline
                is Page.HomeTimeline -> miCore.getMisskeyAPI(account)::homeTimeline
                is Page.Search -> miCore.getMisskeyAPI(account)::searchNote
                is Page.Favorite -> throw IllegalArgumentException("use FavoriteNotePagingStore.kt")
                is Page.UserTimeline -> miCore.getMisskeyAPI(account)::userNotes
                is Page.UserListTimeline -> miCore.getMisskeyAPI(account)::userListTimeline
                is Page.SearchByTag -> miCore.getMisskeyAPI(account)::searchByTag
                is Page.Featured -> miCore.getMisskeyAPI(account)::featured
                is Page.Mention -> miCore.getMisskeyAPI(account)::mentions
                is Page.Antenna -> {
                    val api = miCore.getMisskeyAPI(account)
                    if(api is MisskeyAPIV12){
                        (api as MisskeyAPIV12)::antennasNotes
                    }else{
                        throw IllegalArgumentException("antennaはV12以上でなければ使用できません")
                    }
                }
                else -> throw IllegalArgumentException("unknown class:${pageableTimeline.javaClass}")
            }

        }catch(e: NullPointerException){
            null
        }

    }

    override fun loadInit(request: NoteRequest?): Pair<BodyLessResponse, List<PlaneNoteViewData>?> {
        val res = if(request == null){
            val i = account.getI(encryption)!!
            val req = requestBuilder.build(i, null)
            getStore()?.invoke(req)?.execute()
        }else{
            getStore()?.invoke(request)?.execute()
        }
        return makeResponse(res?.body(), res)
    }

    override fun loadNew(sinceId: String): Pair<BodyLessResponse, List<PlaneNoteViewData>?> {
        val i = account.getI(encryption)!!

        val req = requestBuilder.build(i, NoteRequest.Conditions(sinceId = sinceId))
        val res = getStore()?.invoke(req)?.execute()
        val reversedList = res?.body()?.asReversed()
        return makeResponse(reversedList, res)
    }

    override fun loadOld(untilId: String): Pair<BodyLessResponse, List<PlaneNoteViewData>?> {
        val i = account.getI(encryption)!!
        val req = requestBuilder.build(i, NoteRequest.Conditions(untilId = untilId))
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
                    PlaneNoteViewData(it, account, DetermineTextLengthSettingStore(miCore.getSettingStore()))
                }else{
                    HasReplyToNoteViewData(it, account, DetermineTextLengthSettingStore(miCore.getSettingStore()))
                }
            }catch(e: Exception){
                Log.d("NoteTimelineStore", "パース中にエラー発生: $it", e)
                null
            }

        }?.filterNotNull())
    }
}