package jp.panta.misskeyandroidclient.viewmodel.notes

import android.util.Log
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.api.notes.NoteRequest
import jp.panta.misskeyandroidclient.api.throwIfHasError
import jp.panta.misskeyandroidclient.api.v12.MisskeyAPIV12
import jp.panta.misskeyandroidclient.util.BodyLessResponse
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import retrofit2.Call
import retrofit2.Response
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.lang.NullPointerException
import jp.panta.misskeyandroidclient.model.account.page.Pageable
import jp.panta.misskeyandroidclient.model.notes.NoteCaptureAPIAdapter
import jp.panta.misskeyandroidclient.model.notes.NoteDataSourceAdder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

@Suppress("BlockingMethodInNonBlockingContext")
class NoteTimelineStore(
    val account: Account,
    //override val timelineRequestBase: NoteRequest.Setting,
    override val pageableTimeline: Pageable,
    val include: NoteRequest.Include,
    private val miCore: MiCore,
    private val noteCaptureAPIAdapter: NoteCaptureAPIAdapter,
    private val coroutineScope: CoroutineScope,
    private val coroutineDispatcher: CoroutineDispatcher
) : NotePagedStore{

    private val requestBuilder = NoteRequest.Builder(pageableTimeline, account.getI(miCore.getEncryption()), include)
    private val adder = NoteDataSourceAdder(miCore.getUserDataSource(), miCore.getNoteDataSource())


    private fun getStore(): ((NoteRequest)-> Call<List<NoteDTO>?>)? {
        return try{
            when(pageableTimeline){
                is Pageable.GlobalTimeline -> miCore.getMisskeyAPI(account)::globalTimeline
                is Pageable.LocalTimeline -> miCore.getMisskeyAPI(account)::localTimeline
                is Pageable.HybridTimeline -> miCore.getMisskeyAPI(account)::hybridTimeline
                is Pageable.HomeTimeline -> miCore.getMisskeyAPI(account)::homeTimeline
                is Pageable.Search -> miCore.getMisskeyAPI(account)::searchNote
                is Pageable.Favorite -> throw IllegalArgumentException("use FavoriteNotePagingStore.kt")
                is Pageable.UserTimeline -> miCore.getMisskeyAPI(account)::userNotes
                is Pageable.UserListTimeline -> miCore.getMisskeyAPI(account)::userListTimeline
                is Pageable.SearchByTag -> miCore.getMisskeyAPI(account)::searchByTag
                is Pageable.Featured -> miCore.getMisskeyAPI(account)::featured
                is Pageable.Mention -> miCore.getMisskeyAPI(account)::mentions
                is Pageable.Antenna -> {
                    val api = miCore.getMisskeyAPI(account)
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

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun loadInit(request: NoteRequest?): Pair<BodyLessResponse, List<PlaneNoteViewData>?> {
        val res = if(request == null){
            val req = requestBuilder.build( null)
            getStore()?.invoke(req)?.execute()
        }else{
            getStore()?.invoke(request)?.execute()
        }
        res?.throwIfHasError()
        return makeResponse(res?.body(), res)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun loadNew(sinceId: String): Pair<BodyLessResponse, List<PlaneNoteViewData>?> {

        val req = requestBuilder.build(NoteRequest.Conditions(sinceId = sinceId))
        val res = getStore()?.invoke(req)?.execute()
        val reversedList = res?.body()?.asReversed()
        res?.throwIfHasError()
        return makeResponse(reversedList, res)
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun loadOld(untilId: String): Pair<BodyLessResponse, List<PlaneNoteViewData>?> {
        val req = requestBuilder.build(NoteRequest.Conditions(untilId = untilId))
        val res = getStore()?.invoke(req)?.execute()
        res?.throwIfHasError()

        return makeResponse(res?.body(), res)
    }


    private suspend fun makeResponse(list: List<NoteDTO>?, response: Response<List<NoteDTO>?>?): Pair<BodyLessResponse, List<PlaneNoteViewData>?>{
        if(response?.code() != 200){
            Log.e("NoteTimelineStore", "異常ステータス受信:${response?.code()}, :${response?.errorBody()?.string()}")
            Log.e("NoteTMStore", "pageable:$pageableTimeline, params: ${pageableTimeline.toParams()}")
        }
        return Pair<BodyLessResponse, List<PlaneNoteViewData>?>(BodyLessResponse(response),
            list?.mapNotNull {
                try {
                    val related = adder.addNoteDtoToDataSource(account, it).let { note ->
                        miCore.getGetters().noteRelationGetter.get(note)
                    }
                    val store = DetermineTextLengthSettingStore(miCore.getSettingStore())
                    if (it.reply == null) {
                        PlaneNoteViewData(related, account, store, noteCaptureAPIAdapter)
                    } else {
                        HasReplyToNoteViewData(related, account, store, noteCaptureAPIAdapter)
                    }
                } catch (e: Exception) {
                    Log.d("NoteTimelineStore", "パース中にエラー発生: $it", e)
                    null
                }

            })
    }
}