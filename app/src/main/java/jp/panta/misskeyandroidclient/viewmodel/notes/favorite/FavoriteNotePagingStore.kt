package jp.panta.misskeyandroidclient.viewmodel.notes.favorite

import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.page.Page
import jp.panta.misskeyandroidclient.model.fevorite.Favorite
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.util.BodyLessResponse
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.notes.DetermineTextLengthSettingStore
import jp.panta.misskeyandroidclient.viewmodel.notes.NotePagedStore
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData
import retrofit2.Response

@Suppress("BlockingMethodInNonBlockingContext")
class FavoriteNotePagingStore(
    val account: Account,
    override val pageableTimeline: Page,
    private val miCore: MiCore,
    private val encryption: Encryption
) : NotePagedStore{

    val favorites = miCore.getMisskeyAPI(account)::favorites

    //private val connectionInformation = accountRelation.getCurrentConnectionInformation()!!

    private val builder = NoteRequest.Builder(pageableTimeline, account.getI(encryption))

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

    private fun makeResponse(res: Response<List<Favorite>?>, isReversed: Boolean): Pair<BodyLessResponse, List<PlaneNoteViewData>?>{
        val rawList = if(isReversed) res.body()?.asReversed() else res.body()
        val list = rawList?.map{
            FavoriteNoteViewData(it, account, DetermineTextLengthSettingStore(miCore.getSettingStore()))
        }
        return Pair(BodyLessResponse(res), list)
    }
}