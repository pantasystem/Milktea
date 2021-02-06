package jp.panta.misskeyandroidclient.model.notes.timelines

import jp.panta.misskeyandroidclient.api.notes.NoteDTO
import jp.panta.misskeyandroidclient.api.notes.NoteRequest
import jp.panta.misskeyandroidclient.api.notes.toEntities
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.page.Pageable
import jp.panta.misskeyandroidclient.model.api.MisskeyAPI
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.model.notes.NoteId
import jp.panta.misskeyandroidclient.model.notes.NoteRepository
import jp.panta.misskeyandroidclient.model.users.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.Call
import java.lang.IllegalArgumentException

/**
 * 抽象化は後から考えればいいのでは（名案）
 * Home, Global, Localの場合StreamingAPIに接続するのでそこも考慮する必要がある
 * StreamingAPIとFuture, Pastへの読み込みに対して責務を持つ
 * またNoteのCaptureに対しては責務を負わないこととする。
 * また読み込み状態についてはsuspend funのため呼び出し側から把握できるため読み込みの状態を強く持つ責務は持たないとする。
 */
class TimelineModel(
    private val misskeyAPI: MisskeyAPI,
    private val pageable: Pageable,
    private val account: Account,
    private val encryption: Encryption,
    private val userRepository: UserRepository,
    private val noteRepository: NoteRepository,
    private val coroutineScope: CoroutineScope
) {

    sealed class State {
        object NotesEmpty : State()
        data class Init(val noteIds: List<NoteId>) : State()
        data class LoadFuture(val noteIds: List<NoteId>, val newNoteIds: List<NoteId>) : State()
        data class LoadPast(val noteIds: List<NoteId>, val newNoteIds: List<NoteId>) : State()
        data class Receive(val noteIds: List<NoteId>, val newNoteId: String) : State()
    }

    private class StateWrapper{
        private var mNoteIds: List<NoteId> = emptyList()
        @ExperimentalCoroutinesApi
        private val mTimelineState = BroadcastChannel<State>(Channel.CONFLATED)
        @FlowPreview
        @ExperimentalCoroutinesApi
        val timelineState = mTimelineState.asFlow()

        private var isLoading =  false

    }

    private val stateWrapper = StateWrapper()



    @ExperimentalCoroutinesApi
    private val mErrors = BroadcastChannel<Exception>(1)

    @FlowPreview
    @ExperimentalCoroutinesApi
    val errors = mErrors.asFlow()



    private val lock = Mutex()

    private val builder = NoteRequest.Builder(
        i = account.getI(encryption),
        pageable = pageable
    )


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

    @ExperimentalCoroutinesApi
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun loadInit() {
        /*lock.withLock {
            try{
                val res = api.invoke(pageable.toParams().toNoteRequest(account.getI(encryption)))
                    .execute()
                val body = res.body()
                if(body.isNullOrEmpty()){
                    mTimelineState.send(State.NotesEmpty)
                    return
                }
                val ids = body.map{
                    it.toId()
                }
                this.mNoteIds = ids
                mTimelineState.send(State.Init(noteIds = ids))
            } catch(e: Exception){
                mErrors.send(e)
            } finally {
                isLoading = false
            }


        }*/
        // TODO 実装する
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun loadFuture() {
        lock.withLock {
            val req = pageable.toParams().toNoteRequest(account.getI(encryption))
            //val res = api.invoke(builder.build(NoteRequest.Conditions(sinceId = )))
            // TODO 実装する
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun loadPast() {
        lock.withLock {

        }
    }

    private suspend fun NoteDTO.toId(): NoteId{
        val entities = this.toEntities(account)

        userRepository.addAll(entities.third)
        noteRepository.addAll(entities.second)
        return NoteId(Note.Id(account.accountId, this.id), this.tmpFeaturedId)
    }

}