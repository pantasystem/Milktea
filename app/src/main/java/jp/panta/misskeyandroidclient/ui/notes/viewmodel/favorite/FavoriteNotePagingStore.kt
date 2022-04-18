package jp.panta.misskeyandroidclient.ui.notes.viewmodel.favorite

import net.pantasystem.milktea.api.misskey.favorite.Favorite
import net.pantasystem.milktea.data.infrastructure.notes.NoteCaptureAPIAdapter
import jp.panta.misskeyandroidclient.util.BodyLessResponse
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.NotePagedStore
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.PlaneNoteViewData
import net.pantasystem.milktea.api.misskey.notes.NoteRequest
import net.pantasystem.milktea.data.infrastructure.notes.NoteDataSourceAdder
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.Pageable
import retrofit2.Response

@Suppress("BlockingMethodInNonBlockingContext")
class FavoriteNotePagingStore(
    val account: Account,
    override val pageableTimeline: Pageable.Favorite,
    private val miCore: MiCore,
    private val noteCaptureAPIAdapter: NoteCaptureAPIAdapter,

    ) : NotePagedStore {

    val favorites = miCore.getMisskeyAPIProvider().get(account)::favorites

    //private val connectionInformation = accountRelation.getCurrentConnectionInformation()!!

    private val adder = NoteDataSourceAdder(
        miCore.getUserDataSource(),
        miCore.getNoteDataSource(),
        miCore.getFilePropertyDataSource()
    )

    private val builder = NoteRequest.Builder(pageableTimeline, account.getI(miCore.getEncryption()))

    override suspend fun loadInit(request: NoteRequest?): Pair<BodyLessResponse, List<PlaneNoteViewData>?> {
        return if(request == null){
            val res =favorites(builder.build(NoteRequest.Conditions()))
            makeResponse(res, false)
        }else{
            makeResponse(favorites(request), false)
        }
    }

    override suspend fun loadNew(sinceId: String): Pair<BodyLessResponse, List<PlaneNoteViewData>?> {
        val res = favorites(builder.build(NoteRequest.Conditions(sinceId = sinceId)))
        return makeResponse(res, true)
    }

    override suspend fun loadOld(untilId: String): Pair<BodyLessResponse, List<PlaneNoteViewData>?> {
        val res = favorites(builder.build(NoteRequest.Conditions(untilId = untilId)))
        return makeResponse(res, false)
    }

    private suspend fun makeResponse(res: Response<List<Favorite>?>, isReversed: Boolean): Pair<BodyLessResponse, List<PlaneNoteViewData>?>{
        val rawList = if(isReversed) res.body()?.asReversed() else res.body()
        val list = rawList?.map{
            FavoriteNoteViewData(it ,
                adder.addNoteDtoToDataSource(account, it.note).let { note ->
                    miCore.getGetters().noteRelationGetter.get(note)
                },
                account,  noteCaptureAPIAdapter, miCore.getTranslationStore())
        }
        return Pair(BodyLessResponse(res), list)
    }
}