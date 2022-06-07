package jp.panta.misskeyandroidclient.ui.notes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.data.infrastructure.notes.NoteDataSourceAdder
import net.pantasystem.milktea.data.infrastructure.notes.TimelineStoreImpl
import net.pantasystem.milktea.data.streaming.ChannelBody
import net.pantasystem.milktea.data.streaming.channel.ChannelAPI
import net.pantasystem.milktea.data.streaming.channel.connectUserTimeline
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.notes.TimelineStore

@OptIn(ExperimentalCoroutinesApi::class)
class TimelineViewModel @AssistedInject constructor(
    val miCore: MiCore,
    @Assisted val account: Account?,
    @Assisted val accountId: Long? = account?.accountId,
    @Assisted val pageable: Pageable,
) : ViewModel() {

    @AssistedFactory
    interface ViewModelAssistedFactory {
        fun create(
            account: Account?,
            accountId: Long?,
            pageable: Pageable,
        ): TimelineViewModel
    }

    companion object

    val tag = "TimelineViewModel"
    private val mErrorEvent = MutableSharedFlow<Exception>()
    val errorEvent: SharedFlow<Exception> = mErrorEvent

    private val accountRepository = miCore.getAccountRepository()

    var position: Int = 0


    private val noteDataSourceAdder = NoteDataSourceAdder(
        miCore.getUserDataSource(),
        miCore.getNoteDataSource(),
        miCore.getFilePropertyDataSource()
    )


    val timelineStore: TimelineStore = TimelineStoreImpl(
        pageable,
        noteDataSourceAdder,
        miCore.getNoteDataSource(),
        miCore.getGetters(),
        this::getAccount,
        miCore.getEncryption(),
        miCore.getMisskeyAPIProvider(),
        viewModelScope,
    )

    val timelineState = timelineStore.timelineState.map { pageableState ->
        pageableState.suspendConvert { list ->
            val relations = list.mapNotNull {
                miCore.getGetters().noteRelationGetter.get(it)
            }
            cache.getIn(relations)
        }
    }.stateIn(viewModelScope + Dispatchers.IO, SharingStarted.Lazily, PageableState.Loading.Init())

    val isLoading = timelineStore.timelineState.map {
        it is PageableState.Loading
    }.asLiveData()


    private val logger = miCore.loggerFactory.create("TimelineViewModel")
    private val cache = PlaneNoteViewDataCache(
        this::getAccount,
        miCore.getNoteCaptureAdapter(),
        miCore.getTranslationStore(),
        { account -> miCore.getUrlPreviewStore(account) },
        viewModelScope
    )

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
            timelineStore.onReceiveNote(it.id)
        }.flowOn(Dispatchers.IO).catch { e ->
            logger.warning("ストリーミング受信中にエラー発生", e = e)
        }.launchIn(viewModelScope + Dispatchers.IO)

        loadInit()
    }


    fun loadNew() {
        viewModelScope.launch(Dispatchers.IO) {
            timelineStore.loadFuture()
        }
    }

    fun loadOld() {
        viewModelScope.launch(Dispatchers.IO) {
            timelineStore.loadPrevious()
        }
    }

    fun loadInit() {
        viewModelScope.launch(Dispatchers.IO) {
            timelineStore.clear()
            timelineStore.loadPrevious()
        }
    }


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

}

@Suppress("UNCHECKED_CAST")
fun TimelineViewModel.Companion.provideViewModel(
    assistedFactory: TimelineViewModel.ViewModelAssistedFactory,
    account: Account?,
    accountId: Long? = account?.accountId,
    pageable: Pageable,

) = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return assistedFactory.create(account, accountId, pageable) as T
    }
}