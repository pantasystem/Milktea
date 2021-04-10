package jp.panta.misskeyandroidclient.viewmodel.notes.favorite

import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.page.Pageable
import jp.panta.misskeyandroidclient.model.fevorite.Favorite
import jp.panta.misskeyandroidclient.api.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.NoteCaptureAPIAdapter
import jp.panta.misskeyandroidclient.model.notes.NoteDataSourceAdder
import jp.panta.misskeyandroidclient.util.BodyLessResponse
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.notes.DetermineTextLengthSettingStore
import jp.panta.misskeyandroidclient.viewmodel.notes.NotePagedStore
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import retrofit2.Response

@Suppress("BlockingMethodInNonBlockingContext")
class FavoriteNotePagingStore(
    val account: Account,
    override val pageableTimeline: Pageable.Favorite,
    private val miCore: MiCore,
    private val noteCaptureAPIAdapter: NoteCaptureAPIAdapter,
    private val coroutineScope: CoroutineScope,
    private val coroutineDispatcher: CoroutineDispatcher
) : NotePagedStore{

    val favorites = miCore.getMisskeyAPI(account)::favorites

    //private val connectionInformation = accountRelation.getCurrentConnectionInformation()!!

    private val adder = NoteDataSourceAdder(miCore.getUserDataSource(), miCore.getNoteDataSource())

    private val builder = NoteRequest.Builder(pageableTimeline, account.getI(miCore.getEncryption()))

    override suspend fun loadInit(request: NoteRequest?): Pair<BodyLessResponse, List<PlaneNoteViewData>?> {
        return if(request == null){
            val res =favorites(builder.build(NoteRequest.Conditions())).execute()
            makeResponse(res, false)
        }else{
            makeResponse(favorites(request).execute(), false)
        }
    }

    override suspend fun loadNew(sinceId: String): Pair<BodyLessResponse, List<PlaneNoteViewData>?> {
        val res = favorites(builder.build(NoteRequest.Conditions(sinceId = sinceId))).execute()
        return makeResponse(res, true)
    }

    override suspend fun loadOld(untilId: String): Pair<BodyLessResponse, List<PlaneNoteViewData>?> {
        val res = favorites(builder.build(NoteRequest.Conditions(untilId = untilId))).execute()
        return makeResponse(res, false)
    }

    private suspend fun makeResponse(res: Response<List<Favorite>?>, isReversed: Boolean): Pair<BodyLessResponse, List<PlaneNoteViewData>?>{
        val rawList = if(isReversed) res.body()?.asReversed() else res.body()
        val list = rawList?.map{
            FavoriteNoteViewData(it ,
                adder.addNoteDtoToDataSource(account, it.note).let { note ->
                    miCore.getGetters().noteRelationGetter.get(note)
                },
                account, DetermineTextLengthSettingStore(miCore.getSettingStore()), noteCaptureAPIAdapter, coroutineScope, coroutineDispatcher)
        }
        return Pair(BodyLessResponse(res), list)
    }
}