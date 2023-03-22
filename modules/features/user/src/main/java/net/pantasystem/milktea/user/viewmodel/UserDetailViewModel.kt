package net.pantasystem.milktea.user.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.mapCancellableCatching
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.eventbus.EventBus
import net.pantasystem.milktea.common_android.resource.StringSource
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.CurrentAccountWatcher
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.account.page.PageableTemplate
import net.pantasystem.milktea.model.instance.FeatureEnables
import net.pantasystem.milktea.model.instance.FeatureType
import net.pantasystem.milktea.model.user.*
import net.pantasystem.milktea.model.user.block.BlockRepository
import net.pantasystem.milktea.model.user.mute.CreateMute
import net.pantasystem.milktea.model.user.mute.MuteRepository
import net.pantasystem.milktea.model.user.nickname.DeleteNicknameUseCase
import net.pantasystem.milktea.model.user.nickname.UpdateNicknameUseCase
import net.pantasystem.milktea.model.user.renote.mute.RenoteMuteRepository
import net.pantasystem.milktea.user.R

class UserDetailViewModel @AssistedInject constructor(
    private val deleteNicknameUseCase: DeleteNicknameUseCase,
    private val updateNicknameUseCase: UpdateNicknameUseCase,
    private val accountStore: AccountStore,
    private val accountRepository: AccountRepository,
    private val settingStore: SettingStore,
    private val renoteMuteRepository: RenoteMuteRepository,
    private val blockRepository: BlockRepository,
    private val muteRepository: MuteRepository,
    userDataSource: UserDataSource,
    loggerFactory: Logger.Factory,
    private val userRepository: UserRepository,
    private val featureEnables: FeatureEnables,
    private val toggleFollowUseCase: ToggleFollowUseCase,
    @Assisted val userId: User.Id?,
    @Assisted private val fqdnUserName: String?,
) : ViewModel() {

    @AssistedFactory
    interface ViewModelAssistedFactory {
        fun create(userId: User.Id?, fqdnUserName: String?): UserDetailViewModel
    }

    companion object;

    private val logger = loggerFactory.create("UserDetailViewModel")
    private val accountWatcher = CurrentAccountWatcher(userId?.accountId, accountRepository)

    private val _errors = MutableSharedFlow<Throwable>(extraBufferCapacity = 100)
    val errors = _errors.asSharedFlow()


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
        it as? User.Detail
    }.catch {
        logger.error("observe user error", it)
        _errors.tryEmit(it)
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val user = userState.asLiveData()
    @OptIn(ExperimentalCoroutinesApi::class)
    val account = accountWatcher.account.catch {
        logger.error("Accountの取得に失敗", it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)


    val isMine = combine(userState, accountStore.state) { userState, accountState ->
        userState?.id?.id == accountState.currentAccount?.remoteId
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    val birthday = userState.map {
        it?.info?.birthday
    }.filterNotNull().map {
        StringSource(R.string.birthday, "${it.year}/${it.monthNumber}/${it.dayOfMonth}")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val registrationDate = userState.map {
        it?.info?.createdAt?.toLocalDateTime(TimeZone.currentSystemDefault())?.date
    }.filterNotNull().map {
        StringSource(R.string.registration_date, "${it.year}/${it.monthNumber}/${it.dayOfMonth}")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val tabTypes = combine(
        accountStore.observeCurrentAccount.filterNotNull(), userState.filterNotNull()
    ) { account, user ->
        val isEnableGallery =
            featureEnables.isEnable(account.normalizedInstanceUri, FeatureType.Gallery)
        val isPublicReaction = featureEnables.isEnable(
                account.normalizedInstanceUri,
                FeatureType.UserReactionHistory
            ) && (user.info.isPublicReactions || user.id == User.Id(
                account.accountId, account.remoteId
            ))
        when(account.instanceType) {
            Account.InstanceType.MISSKEY -> {
                listOfNotNull(
                    UserDetailTabType.UserTimeline(user.id),
                    UserDetailTabType.UserTimelineWithReplies(user.id),
                    UserDetailTabType.PinNote(user.id),
                    UserDetailTabType.Media(user.id),
                    if (isEnableGallery) UserDetailTabType.Gallery(
                        user.id, accountId = account.accountId
                    ) else null,
                    if (isPublicReaction) UserDetailTabType.Reactions(user.id) else null,
                )
            }
            Account.InstanceType.MASTODON -> {
                listOf(
                    UserDetailTabType.MastodonUserTimeline(user.id),
                    UserDetailTabType.MastodonUserTimelineWithReplies(user.id),
                    UserDetailTabType.MastodonMedia(user.id)
                )
            }
        }

    }.catch {
        logger.error("ユーザープロフィールのタブの取得に失敗", it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val renoteMuteState = userState.filterNotNull().flatMapLatest {
        renoteMuteRepository.observeOne(it.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val showFollowers = EventBus<User?>()
    val showFollows = EventBus<User?>()

    init {
        require(userId != null || fqdnUserName != null) {
            "userIdかfqdnUserNameのいずれかが指定されている必要があります。"
        }
        sync()
    }


    private fun sync() {
        viewModelScope.launch {
            runCancellableCatching {
                getUserId()
            }.mapCancellableCatching { userId ->
                userRepository.sync(userId).getOrThrow()
            }.onFailure {
                logger.error("user sync error", it)
                _errors.tryEmit(it)
            }
        }
    }


    fun changeFollow() {
        viewModelScope.launch {
            userState.value?.let { user ->
                toggleFollowUseCase(user.id).mapCancellableCatching {
                    userRepository.sync(user.id).getOrThrow()
                }.onFailure {
                    logger.error("unmute failed", e = it)
                    _errors.tryEmit(it)
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
        viewModelScope.launch {
            userState.value?.let { user ->
                muteRepository.create(CreateMute(user.id, expiredAt)).mapCancellableCatching {
                    userRepository.sync(user.id).getOrThrow()
                }.onFailure {
                    logger.error("unmute", e = it)
                    _errors.tryEmit(it)
                }
            }
        }
    }

    fun unmute() {
        viewModelScope.launch {
            userState.value?.let { user ->
                muteRepository.delete(user.id).mapCancellableCatching{
                    userRepository.sync(user.id).getOrThrow()
                }.onFailure {
                    logger.error("unmute", e = it)
                    _errors.tryEmit(it)
                }
            }
        }
    }

    fun block() {
        viewModelScope.launch {
            userState.value?.let { user ->
                blockRepository.create(user.id).mapCancellableCatching {
                    userRepository.sync(user.id)
                }.onFailure {
                    logger.error("block failed", it)
                    _errors.tryEmit(it)
                }
            }
        }
    }

    fun unblock() {
        viewModelScope.launch {
            userState.value?.let { user ->
                blockRepository.delete(user.id).mapCancellableCatching {
                    userRepository.sync(user.id).getOrThrow()
                }.onFailure {
                    logger.info("unblock failed", e = it)
                    _errors.tryEmit(it)
                }
            }
        }
    }

    fun changeNickname(name: String) {
        viewModelScope.launch {
            runCancellableCatching {
                val user = findUser()
                updateNicknameUseCase(user, name)
            }.onSuccess {
                logger.debug("ニックネーム更新処理成功")
            }.onFailure {
                logger.error("ニックネーム更新処理失敗", e = it)
                _errors.tryEmit(it)
            }
        }
    }

    fun deleteNickname() {
        viewModelScope.launch {
            runCancellableCatching {
                val user = findUser()
                deleteNicknameUseCase(user)
            }.onSuccess {
                logger.debug("ニックネーム削除処理成功")
            }.onFailure {
                logger.error("ニックネーム削除失敗", e = it)
                _errors.tryEmit(it)
            }
        }
    }

    fun toggleUserTimelineTab() {
        viewModelScope.launch {
            runCancellableCatching {
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
                _errors.tryEmit(it)
            }
        }
    }

    fun muteRenotes() {
        viewModelScope.launch {
            runCancellableCatching {
                getUserId()
            }.mapCancellableCatching {
                renoteMuteRepository.create(it)
            }.onFailure {
                _errors.tryEmit(it)
            }
        }
    }

    fun unMuteRenotes() {
        viewModelScope.launch {
            runCancellableCatching {
                getUserId()
            }.mapCancellableCatching {
                renoteMuteRepository.delete(it)
            }.onFailure {
                _errors.tryEmit(it)
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
    assistedFactory: UserDetailViewModel.ViewModelAssistedFactory, userId: User.Id
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
    data class UserTimelineWithReplies(val userId: User.Id) : UserDetailTabType(R.string.notes_and_replies)
    data class PinNote(val userId: User.Id) : UserDetailTabType(R.string.pin)
    data class Gallery(val userId: User.Id, val accountId: Long) :
        UserDetailTabType(R.string.gallery)

    data class Reactions(val userId: User.Id) : UserDetailTabType(R.string.reaction)
    data class Media(val userId: User.Id) : UserDetailTabType(R.string.media)

    data class MastodonUserTimeline(val userId: User.Id) : UserDetailTabType(R.string.post)
    data class MastodonUserTimelineWithReplies(val userId: User.Id) : UserDetailTabType(R.string.notes_and_replies)
    data class MastodonMedia(val userId: User.Id) : UserDetailTabType(R.string.media)
}