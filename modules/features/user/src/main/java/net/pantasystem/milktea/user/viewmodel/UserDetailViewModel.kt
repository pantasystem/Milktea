package net.pantasystem.milktea.user.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
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
import net.pantasystem.milktea.common_android_ui.account.viewmodel.AccountViewModelUiStateHelper
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.account.page.PageableTemplate
import net.pantasystem.milktea.model.ap.ApResolverService
import net.pantasystem.milktea.model.instance.InstanceInfoService
import net.pantasystem.milktea.model.setting.DefaultConfig
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.model.user.*
import net.pantasystem.milktea.model.user.block.BlockUserUseCase
import net.pantasystem.milktea.model.user.block.UnBlockUserUseCase
import net.pantasystem.milktea.model.user.mute.CreateMute
import net.pantasystem.milktea.model.user.mute.MuteRepository
import net.pantasystem.milktea.model.user.nickname.DeleteNicknameUseCase
import net.pantasystem.milktea.model.user.nickname.UpdateNicknameUseCase
import net.pantasystem.milktea.model.user.renote.mute.RenoteMuteRepository
import net.pantasystem.milktea.user.R
import net.pantasystem.milktea.user.activity.UserDetailActivity
import javax.inject.Inject

@HiltViewModel
class UserDetailViewModel @Inject constructor(
    private val deleteNicknameUseCase: DeleteNicknameUseCase,
    private val updateNicknameUseCase: UpdateNicknameUseCase,
    private val accountStore: AccountStore,
    private val accountRepository: AccountRepository,
    private val settingStore: SettingStore,
    private val renoteMuteRepository: RenoteMuteRepository,
    private val blockUserUseCase: BlockUserUseCase,
    private val unBlockUserUseCase: UnBlockUserUseCase,
    private val muteRepository: MuteRepository,
    userDataSource: UserDataSource,
    loggerFactory: Logger.Factory,
    instanceInfoService: InstanceInfoService,
    private val userRepository: UserRepository,
    private val toggleFollowUseCase: ToggleFollowUseCase,
    private val apResolverService: ApResolverService,
    private val savedStateHandle: SavedStateHandle,
    private val userDetailTabTypeFactory: UserDetailTabTypeFactory,
    configRepository: LocalConfigRepository,
) : ViewModel() {

    companion object;

    private val logger = loggerFactory.create("UserDetailViewModel")

    //    private val currentAccountId = MutableStateFlow(userId?.accountId)
    private val userId =
        savedStateHandle.getStateFlow<String?>(UserDetailActivity.EXTRA_USER_ID, null)
    private val specifiedAccountId =
        savedStateHandle.getStateFlow<Long?>(UserDetailActivity.EXTRA_ACCOUNT_ID, null)


    @OptIn(ExperimentalCoroutinesApi::class)
    val currentAccount = specifiedAccountId.flatMapLatest { accountId ->
        accountStore.state.map { state ->
            accountId?.let {
                state.get(it)
            } ?: state.currentAccount
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null,
    )

    private val fqdnUserName =
        savedStateHandle.getStateFlow<String?>(UserDetailActivity.EXTRA_USER_NAME, null)

    private val _errors = MutableSharedFlow<Throwable>(extraBufferCapacity = 100)
    val errors = _errors.asSharedFlow()

    private val userProfileArgType = UserProfileArgTypeCombiner(viewModelScope).create(
        userId,
        fqdnUserName,
        currentAccount,
    ).stateIn(viewModelScope, SharingStarted.Lazily, UserProfileArgType.None)


    @OptIn(ExperimentalCoroutinesApi::class)
    val userState = userProfileArgType.flatMapLatest {
        when (val type = it) {
            is UserProfileArgType.FqdnUserName -> {
                userDataSource.observe(type.currentAccount.accountId, type.fqdnUserName)
            }

            UserProfileArgType.None -> flowOf(null)
            is UserProfileArgType.UserId -> {
                userDataSource.observe(type.userId)
            }
        }
    }.mapNotNull {
        it as? User.Detail
    }.catch {
        logger.error("observe user error", it)
        _errors.tryEmit(it)
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)


    val user = userState.asLiveData()

    val isMine = combine(userState, currentAccount) { userState, account ->
        userState?.id?.id == account?.remoteId
    }.stateIn(viewModelScope, SharingStarted.Lazily, false)

    val birthday = userState.map {
        it?.info?.birthday
    }.filterNotNull().map {
        StringSource(R.string.user_birthday, "${it.year}/${it.monthNumber}/${it.dayOfMonth}")
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val registrationDate = userState.map {
        it?.info?.createdAt?.toLocalDateTime(TimeZone.currentSystemDefault())?.date
    }.filterNotNull().map {
        StringSource(
            R.string.user_registration_date,
            "${it.year}/${it.monthNumber}/${it.dayOfMonth}"
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val tabTypes = combine(
        currentAccount.filterNotNull(), userState.filterNotNull()
    ) { account, user ->
        userDetailTabTypeFactory.createTabsForInstanceType(
            account,
            user,
        )
    }.catch {
        logger.error("ユーザープロフィールのタブの取得に失敗", it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val config = configRepository.observe().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        DefaultConfig.config,
    )

    val accountUiState = AccountViewModelUiStateHelper(
        currentAccount,
        accountStore,
        userDataSource,
        instanceInfoService,
        viewModelScope,
    ).uiState

    @OptIn(ExperimentalCoroutinesApi::class)
    val renoteMuteState = userState.filterNotNull().flatMapLatest {
        renoteMuteRepository.observeOne(it.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val showFollowers = EventBus<User?>()
    val showFollows = EventBus<User?>()

    init {
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
                toggleFollowUseCase(user.id).onFailure {
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
                muteRepository.delete(user.id).mapCancellableCatching {
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
                blockUserUseCase(user.id).onFailure {
                    logger.error("block failed", it)
                    _errors.tryEmit(it)
                }
            }
        }
    }

    fun unblock() {
        viewModelScope.launch {
            userState.value?.let { user ->
                unBlockUserUseCase(user.id).onFailure {
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
                renoteMuteRepository.create(it).getOrThrow()
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
                renoteMuteRepository.delete(it).getOrThrow()
            }.onFailure {
                _errors.tryEmit(it)
            }
        }
    }

    fun setCurrentAccount(accountId: Long) {
        viewModelScope.launch {
            accountRepository.get(accountId).mapCancellableCatching {
                it to apResolverService.resolve(getUserId(), accountId).getOrThrow()
            }.onSuccess { (account, resolved) ->
                savedStateHandle[UserDetailActivity.EXTRA_USER_ID] = resolved.id.id
                savedStateHandle[UserDetailActivity.EXTRA_ACCOUNT_ID] = resolved.id.accountId
                accountStore.setCurrent(account)
            }.onFailure {
                logger.error("setCurrentAccount failed", it)
                _errors.tryEmit(it)
            }
        }
    }

    private suspend fun findUser(): User {
        return userRepository.find(getUserId())
    }


    private suspend fun getUserId(): User.Id {
        val strUserId = savedStateHandle.get<String?>(UserDetailActivity.EXTRA_USER_ID)
        val specifiedAccountId = savedStateHandle.get<Long?>(UserDetailActivity.EXTRA_ACCOUNT_ID)
        val fqdnUserName = savedStateHandle.get<String?>(UserDetailActivity.EXTRA_USER_NAME)
        val currentAccount = specifiedAccountId?.let {
            accountRepository.get(it).getOrThrow()
        } ?: accountRepository.getCurrentAccount().getOrThrow()
        val argType = when {
            strUserId != null -> {
                UserProfileArgType.UserId(User.Id(currentAccount.accountId, strUserId))
            }

            fqdnUserName != null -> {
                UserProfileArgType.FqdnUserName(fqdnUserName, currentAccount)
            }

            else -> {
                UserProfileArgType.None
            }
        }

        return when (argType) {
            is UserProfileArgType.FqdnUserName -> {
                val (userName, host) = Acct(argType.fqdnUserName).let {
                    it.userName to it.host
                }
                userRepository.findByUserName(currentAccount.accountId, userName, host).id
            }

            is UserProfileArgType.UserId -> {
                argType.userId
            }

            UserProfileArgType.None -> throw IllegalStateException()
        }

    }
}

sealed interface UserProfileArgType {
    data class UserId(val userId: User.Id) : UserProfileArgType

    data class FqdnUserName(val fqdnUserName: String, val currentAccount: Account) :
        UserProfileArgType

    object None : UserProfileArgType

}

class UserProfileArgTypeCombiner(
    private val scope: CoroutineScope,
) {
    fun create(
        userIdFlow: StateFlow<String?>,
        fqdnUserNameFlow: StateFlow<String?>,
        currentAccountFlow: StateFlow<Account?>
    ): StateFlow<UserProfileArgType> {
        return combine(
            userIdFlow,
            fqdnUserNameFlow,
            currentAccountFlow,
        ) { userId, fqdnUserName, currentAccount ->
            when {
                userId != null && currentAccount != null -> {
                    UserProfileArgType.UserId(User.Id(currentAccount.accountId, userId))
                }

                fqdnUserName != null && currentAccount != null -> {
                    UserProfileArgType.FqdnUserName(fqdnUserName, currentAccount)
                }

                else -> {
                    UserProfileArgType.None
                }
            }
        }.stateIn(
            scope,
            SharingStarted.WhileSubscribed(5_000),
            UserProfileArgType.None
        )
    }
}