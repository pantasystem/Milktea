package net.pantasystem.milktea.user.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.*
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.pantasystem.milktea.api.misskey.v12_75_0.MisskeyAPIV1275
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.notes.NoteTranslationStore
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common_android.eventbus.EventBus
import net.pantasystem.milktea.common_android.resource.StringSource
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.gettters.NoteRelationGetter
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.CurrentAccountWatcher
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.account.page.PageableTemplate
import net.pantasystem.milktea.model.notes.NoteCaptureAPIAdapter
import net.pantasystem.milktea.model.notes.NoteDataSource
import net.pantasystem.milktea.model.user.Acct
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserRepository
import net.pantasystem.milktea.model.user.mute.CreateMute
import net.pantasystem.milktea.model.user.nickname.DeleteNicknameUseCase
import net.pantasystem.milktea.model.user.nickname.UpdateNicknameUseCase
import net.pantasystem.milktea.note.viewmodel.PlaneNoteViewData
import net.pantasystem.milktea.user.R

class UserDetailViewModel @AssistedInject constructor(
    private val translationStore: NoteTranslationStore,
    private val deleteNicknameUseCase: DeleteNicknameUseCase,
    private val updateNicknameUseCase: UpdateNicknameUseCase,
    private val accountStore: AccountStore,
    private val accountRepository: AccountRepository,
    private val noteDataSource: NoteDataSource,
    private val settingStore: SettingStore,
    userDataSource: UserDataSource,
    loggerFactory: Logger.Factory,
    private val noteRelationGetter: NoteRelationGetter,
    private val userRepository: UserRepository,
    private val noteCaptureAPIAdapter: NoteCaptureAPIAdapter,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    @Assisted val userId: User.Id?,
    @Assisted private val fqdnUserName: String?,
) : ViewModel() {

    @AssistedFactory
    interface ViewModelAssistedFactory {
        fun create(userId: User.Id?, fqdnUserName: String?): UserDetailViewModel
    }

    companion object;

    private val logger = loggerFactory.create("UserDetailViewModel")
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
    private val accountWatcher = CurrentAccountWatcher(userId?.accountId, accountRepository)


    @OptIn(ExperimentalCoroutinesApi::class)
    val userState = when {
        userId != null -> {
            userDataSource.observe(userId)
        }
        fqdnUserName != null -> {
            accountStore.observeCurrentAccount.filterNotNull().flatMapLatest {
                userDataSource.observe(it.accountId, fqdnUserName)
            }
        }
        else -> {
            throw IllegalArgumentException()
        }
    }.mapNotNull {
        logger.debug("flow user:$it")
        it as? User.Detail
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val user = userState.asLiveData()

    private val pinNotesState = userState.filterNotNull().map {
        it.pinnedNoteIds?.map { id ->
            noteDataSource.get(id)
        } ?: emptyList()
    }


    @OptIn(ExperimentalCoroutinesApi::class)
    val pinNotes = MediatorLiveData<List<PlaneNoteViewData>>().apply {
        pinNotesState.map { notes ->
            notes.mapNotNull {
                noteRelationGetter.get(it.getOrThrow()).getOrNull()
            }.map { note ->
                PlaneNoteViewData(
                    note,
                    accountWatcher.getAccount(),
                    noteCaptureAPIAdapter,
                    translationStore
                )
            }
        }.onEach {
            this.postValue(it)
        }.flatMapLatest {
            it.map { n ->
                n.eventFlow
            }.merge()
        }.catch { e ->
            logger.warning("", e = e)
        }.launchIn(viewModelScope + dispatcher)
    }

    val isMine = combine(userState, accountStore.state) { userState, accountState ->
        userState?.id?.id == accountState.currentAccount?.remoteId
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    val birthday = userState.map {
        it?.birthday
    }.filterNotNull().map {
        StringSource(R.string.birthday, "${it.year}/${it.monthNumber}/${it.dayOfMonth}")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val registrationDate = userState.map {
        it?.createdAt?.toLocalDateTime(TimeZone.currentSystemDefault())?.date
    }.filterNotNull().map {
        StringSource(R.string.registration_date, "${it.year}/${it.monthNumber}/${it.dayOfMonth}")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val tabTypes = combine(accountStore.observeCurrentAccount.filterNotNull(), userState.filterNotNull()) { account, user ->
        val isEnableGallery = misskeyAPIProvider.get(account.instanceDomain) is MisskeyAPIV1275
        val isPublicReaction = user.isPublicReactions
        listOfNotNull(
            UserDetailTabType.UserTimeline(user.id),
            UserDetailTabType.PinNote(user.id),
            UserDetailTabType.Media(user.id),
            if (isEnableGallery) UserDetailTabType.Gallery(user.id, accountId = account.accountId) else null,
            if (isPublicReaction) UserDetailTabType.Reactions(user.id) else null,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val showFollowers = EventBus<User?>()
    val showFollows = EventBus<User?>()

    init {
        require(userId != null || fqdnUserName != null) {
            "userIdかfqdnUserNameのいずれかが指定されている必要があります。"
        }
        sync()
    }


    fun sync() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                getUserId()
            }.mapCatching { userId ->
                userRepository.sync(userId).getOrThrow()
            }.onFailure {
                logger.error("user sync error", it)
            }
        }
    }


    fun changeFollow() {
        viewModelScope.launch(Dispatchers.IO) {
            userState.value?.let {
                runCatching {
                    val user = userRepository.find(it.id) as User.Detail
                    if (user.isFollowing || user.hasPendingFollowRequestFromYou) {
                        userRepository.unfollow(user.id)
                    } else {
                        userRepository.follow(user.id)
                    }
                    userRepository.sync(user.id).getOrThrow()
                }.onFailure {
                    logger.error("unmute failed", e = it)
                }
            }

        }
    }

    fun showFollows() {
        showFollows.event = user.value
    }

    fun showFollowers() {
        showFollowers.event = user.value
    }

    fun mute(expiredAt: Instant?) {
        viewModelScope.launch(Dispatchers.IO) {
            userState.value?.let {
                runCatching {
                    userRepository.mute(CreateMute(it.id, expiredAt))
                    userRepository.sync(it.id).getOrThrow()
                }.onFailure {
                    logger.error("unmute", e = it)
                }
            }
        }
    }

    fun unmute() {
        viewModelScope.launch(Dispatchers.IO) {
            userState.value?.let {
                runCatching {
                    userRepository.unmute(it.id)
                    userRepository.sync(it.id).getOrThrow()
                }.onFailure {
                    logger.error("unmute", e = it)
                }
            }
        }
    }

    fun block() {
        viewModelScope.launch(Dispatchers.IO) {
            userState.value?.let {
                runCatching {
                    userRepository.block(it.id)
                    userRepository.sync(it.id)
                }.onFailure {
                    logger.error("block failed", it)
                }
            }
        }
    }

    fun unblock() {
        viewModelScope.launch(Dispatchers.IO) {
            userState.value?.let {
                runCatching {
                    userRepository.unblock(it.id)
                    userRepository.sync(it.id).getOrThrow()
                }.onFailure {
                    logger.info("unblock failed", e = it)
                }
            }
        }
    }

    fun changeNickname(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val user = findUser()
                updateNicknameUseCase(user, name)
            }.onSuccess {
                logger.debug("ニックネーム更新処理成功")
            }.onFailure {
                logger.error("ニックネーム更新処理失敗", e = it)
            }
        }
    }

    fun deleteNickname() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val user = findUser()
                deleteNicknameUseCase(user)
            }.onSuccess {
                logger.debug("ニックネーム削除処理成功")
            }.onFailure {
                logger.error("ニックネーム削除失敗", e = it)
            }
        }
    }

    fun toggleUserTimelineTab() {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val userId = getUserId()
                val account = accountRepository.get(userId.accountId).getOrThrow()
                val page = account.pages.firstOrNull {
                    val pageable = it.pageable()
                    pageable is Pageable.UserTimeline && pageable.userId == userId.id
                }
                if (page == null) {
                    accountStore.addPage(
                        PageableTemplate(account).user(findUser(), settingStore.isUserNameDefault)
                    )
                } else {
                    accountStore.removePage(page)
                }
            }.onFailure {
                logger.error("toggle user timeline tab failed", it)
            }
        }
    }

    private suspend fun findUser(): User {
        return userRepository.find(getUserId())
    }



    private suspend fun getUserId(): User.Id {
        if (userId != null) {
            return userId
        }

        val account = accountWatcher.getAccount()
        if (fqdnUserName != null) {
            val (userName, host) = Acct(fqdnUserName).let {
                it.userName to it.host
            }
            return userRepository.findByUserName(account.accountId, userName, host).id
        }
        throw IllegalStateException()
    }
}

@Suppress("UNCHECKED_CAST")
fun UserDetailViewModel.Companion.provideFactory(
    assistedFactory: UserDetailViewModel.ViewModelAssistedFactory,
    userId: User.Id
): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return assistedFactory.create(userId, null) as T
    }
}

@Suppress("UNCHECKED_CAST")
fun UserDetailViewModel.Companion.provideFactory(
    assistedFactory: UserDetailViewModel.ViewModelAssistedFactory,
    fqdnUserName: String,
): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return assistedFactory.create(null, fqdnUserName) as T
    }
}

sealed class UserDetailTabType(
    @StringRes val title: Int
) {

    data class UserTimeline(val userId: User.Id) : UserDetailTabType(R.string.post)
    data class PinNote(val userId: User.Id) : UserDetailTabType(R.string.pin)
    data  class Gallery(val userId: User.Id, val accountId: Long) : UserDetailTabType(R.string.gallery)
    data class Reactions(val userId: User.Id) : UserDetailTabType(R.string.reaction)
    data class Media(val userId: User.Id) : UserDetailTabType(R.string.media)
}