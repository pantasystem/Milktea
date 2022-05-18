package jp.panta.misskeyandroidclient.ui.notes.viewmodel

import android.util.Log
import androidx.lifecycle.*
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.favorite.FavoriteNotePagingStore
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.api.misskey.MisskeyAPI
import net.pantasystem.milktea.data.infrastructure.notes.*
import net.pantasystem.milktea.data.infrastructure.settings.SettingStore
import net.pantasystem.milktea.data.streaming.ChannelBody
import net.pantasystem.milktea.data.streaming.channel.ChannelAPI
import net.pantasystem.milktea.data.streaming.channel.connectUserTimeline
import jp.panta.misskeyandroidclient.util.BodyLessResponse
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.url.UrlPreviewLoadTask
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.api.misskey.notes.NoteRequest
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.NoteDataSource
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

@ExperimentalCoroutinesApi
class TimelineViewModel(
    val account: Account?,
    val accountId: Long? = account?.accountId,
    val pageable: Pageable,
    val miCore: MiCore,
    val include: NoteRequest.Include
) : ViewModel() {

    data class Request(
        val sinceId: String? = null,
        val untilId: String? = null,
        var substitute: Request? = null
    )


    val tag = "TimelineViewModel"
    private val mErrorEvent = MutableSharedFlow<Exception>()
    val errorEvent: SharedFlow<Exception> = mErrorEvent

    private val accountRepository = miCore.getAccountRepository()

    var position: Int = 0

    private var mNoteIds = HashSet<Note.Id>()

    private val timelineState = MutableStateFlow<TimelineState>(TimelineState.Init(emptyList()))

    private val removeSize = 50

    private val noteDataSourceAdder = NoteDataSourceAdder(
        miCore.getUserDataSource(),
        miCore.getNoteDataSource(),
        miCore.getFilePropertyDataSource()
    )


    val isLoading = MutableLiveData<Boolean>()
    val isInitLoading = MutableLiveData<Boolean>()
    private var mIsLoading: Boolean = false
        set(value) {
            field = value
            isLoading.postValue(value)
        }
    private var mIsInitLoading: Boolean = false
        set(value) {
            field = value
            isInitLoading.postValue(value)
        }


    private val logger = miCore.loggerFactory.create("TimelineViewModel")

    init {
        flow {
            emit(getAccount())
        }.filter {
            pageable is Pageable.GlobalTimeline
                    || pageable is Pageable.HybridTimeline
                    || pageable is Pageable.LocalTimeline
                    || pageable is Pageable.HomeTimeline
                    || pageable is Pageable.UserListTimeline
                    || pageable is Pageable.Antenna
                    || pageable is Pageable.UserTimeline
                    || pageable is Pageable.ChannelTimeline
        }.flatMapLatest { account ->
            when (pageable) {
                is Pageable.GlobalTimeline -> {
                    miCore.getChannelAPI(account).connect(ChannelAPI.Type.Global)
                }
                is Pageable.HybridTimeline -> {
                    miCore.getChannelAPI(account).connect(ChannelAPI.Type.Hybrid)

                }
                is Pageable.LocalTimeline -> {
                    miCore.getChannelAPI(account).connect(ChannelAPI.Type.Local)

                }
                is Pageable.HomeTimeline -> {
                    miCore.getChannelAPI(account).connect(ChannelAPI.Type.Home)
                }
                is Pageable.UserListTimeline -> {
                    miCore.getChannelAPI(account)
                        .connect(ChannelAPI.Type.UserList(userListId = pageable.listId))
                }
                is Pageable.Antenna -> {
                    miCore.getChannelAPI(account)
                        .connect(ChannelAPI.Type.Antenna(antennaId = pageable.antennaId))
                }
                is Pageable.UserTimeline -> {
                    miCore.getChannelAPI(account)
                        .connectUserTimeline(pageable.userId)
                }
                is Pageable.ChannelTimeline -> {
                    miCore.getChannelAPI(account)
                        .connect(ChannelAPI.Type.Channel(channelId = pageable.channelId))
                }
                else -> throw IllegalStateException("Global, Hybrid, Local, Homeは以外のStreamは対応していません。")
            }
        }.map {
            it as? ChannelBody.ReceiveNote
        }.filterNotNull().map {
            noteDataSourceAdder.addNoteDtoToDataSource(getAccount(), it.body)
        }.map {
            miCore.getGetters().noteRelationGetter.get(it)
        }.filter {
            !mNoteIds.contains(it.note.id) && !this.mIsLoading
        }.map {
            if (it.reply == null) {
                PlaneNoteViewData(
                    it,
                    getAccount(),
                    miCore.getNoteCaptureAdapter(),
                    miCore.getTranslationStore()
                )
            } else {
                HasReplyToNoteViewData(
                    it,
                    getAccount(),
                    miCore.getNoteCaptureAdapter(),
                    miCore.getTranslationStore()
                )
            }
        }.onEach { note ->
            this.mNoteIds.add(note.id)
            listOf(note).captureNotes()
            val list = ArrayList<PlaneNoteViewData>(this.timelineState.value.notes).also {
                it.add(0, note)
            }
            if (timelineState.value.notes.size > removeSize && position == 0 && timelineState.subscriptionCount.value > 0) {
                val removed = list.subList(removeSize, list.size - 1)

                mNoteIds.removeAll(removed.map { it.id }.toSet())
                removed.forEach {
                    it.job?.cancel()
                }
                removed.clear()
            }
            this.timelineState.value = TimelineState.ReceivedNew(
                notes = list
            )

        }.catch { e ->
            logger.warning("ストリーミング受信中にエラー発生", e = e)
        }.launchIn(viewModelScope + Dispatchers.IO)

        loadInit()
    }


    fun getTimelineState(): StateFlow<TimelineState> {
        return timelineState
    }

    fun loadNew() {
        synchronized(timelineState) {
            logger.debug("loadNew")
            if (mIsInitLoading || mIsLoading) {
                logger.debug("loadNewキャンセル")
                return
            }
            mIsLoading = true
            val request = timelineState.value.makeSinceIdRequest()
            logger.debug("makeSinceIdRequest完了:$request")
            if (request?.sinceId == null) {
                mIsInitLoading = false
                mIsLoading = false
                logger.debug("初期読み込みへ移行")
                return loadInit()
            }
            logger.debug("判定準備完了 active:${viewModelScope.isActive}")
            viewModelScope.launch(Dispatchers.IO) {
                logger.debug("launch開始")
                runCatching {
                    logger.debug("読み込みを開始")
                    val res = syncLoad(request)
                    val list = res?.second?.filter(::filterDuplicate) ?: emptyList()
                    list.captureNotes()
                    loadUrlPreviews(list)

                    val state = timelineState.value

                    val newList = ArrayList<PlaneNoteViewData>(state.notes).apply {
                        addAll(0, list)
                    }

                    // 先頭で購読されていてノート数が一定数以上なら後端のノートをリスト上から削除する
                    if (position == 0 && newList.size > removeSize && timelineState.subscriptionCount.value > 0) {
                        val removed = newList.subList(removeSize, newList.size - 1)
                        removed.forEach { it.job?.cancel() }
                        mNoteIds.removeAll(removed.map(::mapId).toSet())
                        removed.clear()
                    }
                    mNoteIds.addAll(list.map(::mapId))
                    TimelineState.LoadNew(
                        newList,
                    )
                }.onSuccess {
                    timelineState.value = it
                }.onFailure {
                    handleError(it)
                }
                mIsLoading = false
                mIsInitLoading = false
            }
        }.also {
            logger.debug("job: active=${it.isActive}, completed=${it.isCompleted}, cancelled=${it.isCancelled}")
        }


    }

    fun loadOld() {
        synchronized(timelineState) {
            val request = timelineState.value.makeUntilIdRequest()
            request?.untilId
                ?: return loadInit()
            if (mIsLoading || mIsInitLoading) {
                return
            }
            mIsLoading = true

            viewModelScope.launch(Dispatchers.IO) {
                runCatching {
                    val res = syncLoad(request)
                    val list = res?.second?.filter(::filterDuplicate) ?: emptyList()
                    list.captureNotes()
                    loadUrlPreviews(list)
                    mNoteIds.addAll(list.map(::mapId))
                    val state = timelineState.value
                    val newList = ArrayList<PlaneNoteViewData>(state.notes).apply {
                        addAll(list)
                    }
                    TimelineState.LoadOld(
                        newList,
                    )
                }.onSuccess {
                    timelineState.value = it
                }.onFailure {
                    handleError(it)
                }
                mIsLoading = false
                mIsInitLoading = false

            }
        }

    }

    fun loadInit() {
        synchronized(timelineState) {
            Log.d(
                "TimelineViewModel",
                "初期読み込みを開始します isLoading:${mIsLoading}, isInitLoading:${isInitLoading}"
            )

            if (mIsInitLoading || mIsLoading) {
                return
            }
            mIsLoading = true
            mIsInitLoading = true

            viewModelScope.launch(Dispatchers.IO) {
                runCatching {
                    val account = getAccount()
                    val response = account.getPagedStore().loadInit()
                    val list = response.second ?: emptyList()
                    logger.debug("Networkから受信")
                    val state = TimelineState.Init(
                        list,
                    )

                    list.captureNotes()

                    loadUrlPreviews(list)

                    mNoteIds.clear()
                    mNoteIds.addAll(list.map(::mapId))
                    state
                }.onSuccess { state ->
                    timelineState.value = state

                }.onFailure {
                    timelineState.value = TimelineState.Init(emptyList())
                    handleError(it)
                }
                mIsInitLoading = false
                mIsLoading = false


            }
        }

    }


    private suspend fun syncLoad(
        request: Request?,
        isRetry: Boolean = false
    ): Pair<BodyLessResponse, List<PlaneNoteViewData>?>? {
        if (request == null && isRetry) {
            return null
        }
        val account = getAccount()
        val res = when {
            request?.untilId != null -> {
                account.getPagedStore().loadOld(untilId = request.untilId)
            }
            request?.sinceId != null -> {
                account.getPagedStore().loadNew(sinceId = request.sinceId)
            }
            else -> {
                account.getPagedStore().loadInit()
            }
        }
        val notes = res.second

        if (notes?.isNotEmpty() == true) {
            return res
        }

        val targetNoteId = request?.untilId ?: request?.sinceId ?: return null

        val targetNote = try {
            account.getMisskeyAPI().showNote(
                NoteRequest(
                    i = account.getI(miCore.getEncryption()),
                    noteId = targetNoteId
                )
            )
        } catch (e: Throwable) {
            null
        }

        if (targetNote != null) {
            return res
        }

        return syncLoad(request?.substitute, true)

    }


    private suspend fun loadUrlPreviews(list: List<PlaneNoteViewData>) {
        val account = getAccount()

        list.forEach { note ->
            note.textNode?.getUrls()?.let { urls ->
                UrlPreviewLoadTask(
                    miCore.getUrlPreviewStore(account),
                    urls,
                    viewModelScope
                ).load(note.urlPreviewLoadTaskCallback)
            }

        }
    }


    private fun mapId(planeNoteViewData: PlaneNoteViewData): Note.Id {
        return planeNoteViewData.id
    }

    private fun filterDuplicate(planeNoteViewData: PlaneNoteViewData): Boolean {
        Log.d("TM-VM-Filter", "重複を発見したため排除しました")
        return !mNoteIds.contains(planeNoteViewData.id)
    }


    private fun List<PlaneNoteViewData>.captureNotes() {
        val scope = viewModelScope + Dispatchers.IO
        val notes = this
        viewModelScope.launch(Dispatchers.IO) {
            notes.forEach { note ->
                note.job = note.eventFlow.onEach {
                    if (it is NoteDataSource.Event.Deleted) {
                        timelineState.value =
                            TimelineState.Deleted(
                                timelineState.value.notes.filterNot { pnvd ->
                                    pnvd.id == it.noteId
                                }
                            )

                    }
                }.launchIn(scope)
            }
        }
    }


    private fun TimelineState?.makeUntilIdRequest(substituteSize: Int = 2): Request? {
        val ids = this?.getUntilIds(substituteSize) ?: emptyList()
        return ids.makeUntilIdRequest()
    }

    private fun List<String>.makeUntilIdRequest(index: Int = 0): Request? {
        if (this.size <= index) {
            return null
        }

        val now = Request(
            untilId = this[index]
        )
        now.substitute = this.makeUntilIdRequest(index + 1)
        return now
    }

    private fun TimelineState?.makeSinceIdRequest(substituteSize: Int = 2): Request? {
        logger.debug("makeSinceIdRequest")
        return (this?.getSinceIds(substituteSize) ?: emptyList()).makeSinceIdRequest()
    }

    private fun List<String>.makeSinceIdRequest(index: Int = 0): Request? {
        if (this.size <= index) {
            return null
        }
        val now = Request(
            sinceId = this[index]
        )
        now.substitute = makeUntilIdRequest(index + 1)
        return now
    }

    //
    private var mAccountCache: Account? = account
    private suspend fun getAccount(): Account {
        if (mAccountCache != null) {
            mAccountCache
        }

        if (accountId != null && accountId > 0) {
            val ac = accountRepository.get(accountId)
            mAccountCache = ac
            return mAccountCache ?: throw IllegalStateException("Accountが取得できませんでした。")
        }

        mAccountCache = accountRepository.getCurrentAccount()
        return mAccountCache ?: throw IllegalStateException("Accountが取得できませんでした。")
    }


    private fun Account.getMisskeyAPI(): MisskeyAPI {
        return miCore.getMisskeyAPIProvider().get(this)
    }

    private fun Account.getPagedStore(): NotePagedStore {
        return when (pageable) {
            is Pageable.Favorite -> FavoriteNotePagingStore(
                this,
                pageable,
                miCore,
                miCore.getNoteCaptureAdapter(),
            )
            else -> NoteTimelineStore(
                this,
                pageable,
                include,
                miCore,
                miCore.getNoteCaptureAdapter(),
                miCore.getTranslationStore()
            )
        }
    }

    private fun handleError(t: Throwable) {
        if (t is Exception) {
            mErrorEvent.tryEmit(t)
        }

    }
}