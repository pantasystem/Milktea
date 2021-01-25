package jp.panta.misskeyandroidclient.model.notes.timelines

import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.api.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.page.Pageable
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.NoteModel
import jp.panta.misskeyandroidclient.model.notes.NoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.asFlow
import retrofit2.Call
import java.lang.IllegalArgumentException

/**
 * 抽象化は後から考えればいいのでは（名案）
 * Home, Global, Localの場合StreamingAPIに接続するのでそこも考慮する必要がある
 */
class TimelineModel(
    private val misskeyAPI: MisskeyAPI,
    private val noteModel: NoteModel,
    private val pageable: Pageable,
    private val account: Account,
    private val encryption: Encryption,
    private val coroutineScope: CoroutineScope
) {

    @ExperimentalCoroutinesApi
    private val broadcast = BroadcastChannel<List<Note>>(1)
    @FlowPreview
    @ExperimentalCoroutinesApi
    val timelineUpdateEvent = broadcast.asFlow()

    @ExperimentalCoroutinesApi
    private val mIsLoadingEvent = BroadcastChannel<Boolean>(Channel.CONFLATED)
    @FlowPreview
    @ExperimentalCoroutinesApi
    val isLoadingEvent = mIsLoadingEvent.asFlow()



    private var noteIds: List<String> = emptyList()
    private val api: (NoteRequest)-> Call<List<NoteDTO>?> = when(pageable){
        is Pageable.HomeTimeline -> {
            misskeyAPI::homeTimeline
        }
        is Pageable.GlobalTimeline ->{
            misskeyAPI::globalTimeline
        }
        is Pageable.HybridTimeline ->{
            misskeyAPI::hybridTimeline
        }
        is Pageable.LocalTimeline ->{
            misskeyAPI::localTimeline
        }
        else -> throw IllegalArgumentException("対応していないタイムラインです")
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun loadInit() {
        val res = api.invoke(pageable.toParams().toNoteRequest(account.getI(encryption)))
            .execute()
        val body = res.body()
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun loadFuture() {

    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun loadPast() {

    }

    private suspend fun setLoading(loading: Boolean){
        this.mIsLoadingEvent.send(loading)
    }
}