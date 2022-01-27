package jp.panta.misskeyandroidclient.ui.notes.viewmodel

import android.util.Log
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.api.notes.NoteRequest
import jp.panta.misskeyandroidclient.api.throwIfHasError
import jp.panta.misskeyandroidclient.api.v12.MisskeyAPIV12
import jp.panta.misskeyandroidclient.util.BodyLessResponse
import jp.panta.misskeyandroidclient.viewmodel.MiCore

import retrofit2.Response
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.lang.NullPointerException
import jp.panta.misskeyandroidclient.model.account.page.Pageable
import jp.panta.misskeyandroidclient.model.notes.NoteCaptureAPIAdapter
import jp.panta.misskeyandroidclient.model.notes.NoteDataSourceAdder
import jp.panta.misskeyandroidclient.model.notes.NoteTranslationStore


@Suppress("BlockingMethodInNonBlockingContext")
class NoteTimelineStore(
    val account: Account,
    //override val timelineRequestBase: NoteRequest.Setting,
    override val pageableTimeline: Pageable,
    val include: NoteRequest.Include,
    private val miCore: MiCore,
    private val noteCaptureAPIAdapter: NoteCaptureAPIAdapter,
    private val noteTranslationStore: NoteTranslationStore
) : NotePagedStore {

    private val requestBuilder = NoteRequest.Builder(pageableTimeline, account.getI(miCore.getEncryption()), include)
    private val adder = NoteDataSourceAdder(miCore.getUserDataSource(), miCore.getNoteDataSource(), miCore.getFilePropertyDataSource())


    private fun getStore(): (suspend (NoteRequest)-> Response<List<NoteDTO>?>)? {
        return try{
            when(pageableTimeline){
                is Pageable.GlobalTimeline -> miCore.getMisskeyAPIProvider().get(account)::globalTimeline
                is Pageable.LocalTimeline -> miCore.getMisskeyAPIProvider().get(account)::localTimeline
                is Pageable.HybridTimeline -> miCore.getMisskeyAPIProvider().get(account)::hybridTimeline
                is Pageable.HomeTimeline -> miCore.getMisskeyAPIProvider().get(account)::homeTimeline
                is Pageable.Search -> miCore.getMisskeyAPIProvider().get(account)::searchNote
                is Pageable.Favorite -> throw IllegalArgumentException("use FavoriteNotePagingStore.kt")
                is Pageable.UserTimeline -> miCore.getMisskeyAPIProvider().get(account)::userNotes
                is Pageable.UserListTimeline -> miCore.getMisskeyAPIProvider().get(account)::userListTimeline
                is Pageable.SearchByTag -> miCore.getMisskeyAPIProvider().get(account)::searchByTag
                is Pageable.Featured -> miCore.getMisskeyAPIProvider().get(account)::featured
                is Pageable.Mention -> miCore.getMisskeyAPIProvider().get(account)::mentions
                is Pageable.Antenna -> {
                    val api = miCore.getMisskeyAPIProvider().get(account)
                    if(api is MisskeyAPIV12){
                        (api)::antennasNotes
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

    override suspend fun loadInit(request: NoteRequest?): Pair<BodyLessResponse, List<PlaneNoteViewData>?> {
        val res = if(request == null){
            val req = requestBuilder.build( null)
            getStore()?.invoke(req)
        }else{
            getStore()?.invoke(request)
        }
        res?.throwIfHasError()
        return makeResponse(res?.body(), res)
    }

    override suspend fun loadNew(sinceId: String): Pair<BodyLessResponse, List<PlaneNoteViewData>?> {

        val req = requestBuilder.build(NoteRequest.Conditions(sinceId = sinceId))
        val res = getStore()?.invoke(req)
        val reversedList = res?.body()?.asReversed()
        res?.throwIfHasError()
        return makeResponse(reversedList, res)
    }

    override suspend fun loadOld(untilId: String): Pair<BodyLessResponse, List<PlaneNoteViewData>?> {
        val req = requestBuilder.build(NoteRequest.Conditions(untilId = untilId))
        val res = getStore()?.invoke(req)
        res?.throwIfHasError()

        return makeResponse(res?.body(), res)
    }


    private suspend fun makeResponse(list: List<NoteDTO>?, response: Response<List<NoteDTO>?>?): Pair<BodyLessResponse, List<PlaneNoteViewData>?>{
        if(response?.code() != 200){
            Log.e("NoteTimelineStore", "異常ステータス受信:${response?.code()}, :${response?.errorBody()?.string()}")
            Log.e("NoteTMStore", "pageable:$pageableTimeline, params: ${pageableTimeline.toParams()}")
        }
        return Pair(BodyLessResponse(response),
            list?.mapNotNull {
                try {
                    val related = adder.addNoteDtoToDataSource(account, it).let { note ->
                        miCore.getGetters().noteRelationGetter.get(note)
                    }
                    if (it.reply == null) {
                        PlaneNoteViewData(related, account, noteCaptureAPIAdapter, noteTranslationStore)
                    } else {
                        HasReplyToNoteViewData(related, account, noteCaptureAPIAdapter, noteTranslationStore)
                    }
                } catch (e: Exception) {
                    Log.d("NoteTimelineStore", "パース中にエラー発生: $it", e)
                    null
                }

            })
    }
}