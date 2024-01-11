package net.pantasystem.milktea.user.profile.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.handler.AppGlobalError
import net.pantasystem.milktea.app_store.handler.UserActionAppGlobalErrorStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.flatMapCancellableCatching
import net.pantasystem.milktea.common.mapCancellableCatching
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.resource.StringSource
import net.pantasystem.milktea.common_android_ui.account.viewmodel.AccountViewModelUiStateHelper
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.ap.ApResolverService
import net.pantasystem.milktea.model.instance.InstanceInfoService
import net.pantasystem.milktea.model.setting.DefaultConfig
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.model.user.*
import net.pantasystem.milktea.model.user.block.BlockUserUseCase
import net.pantasystem.milktea.model.user.block.UnBlockUserUseCase
import net.pantasystem.milktea.model.user.follow.ToggleNotifyUserPostsUseCase
import net.pantasystem.milktea.model.user.mute.CreateMute
import net.pantasystem.milktea.model.user.mute.MuteUserUseCase
import net.pantasystem.milktea.model.user.mute.UnMuteUserUseCase
import net.pantasystem.milktea.model.user.nickname.DeleteNicknameUseCase
import net.pantasystem.milktea.model.user.nickname.UpdateNicknameUseCase
import net.pantasystem.milktea.model.user.renote.mute.RenoteMuteRepository
import net.pantasystem.milktea.user.R
import net.pantasystem.milktea.user.profile.UserDetailActivity
import javax.inject.Inject

@HiltViewModel
class UserDetailViewModel @Inject constructor(
    private val deleteNicknameUseCase: DeleteNicknameUseCase,
    private val updateNicknameUseCase: UpdateNicknameUseCase,
    private val accountStore: AccountStore,
    private val accountRepository: AccountRepository,
    private val renoteMuteRepository: RenoteMuteRepository,
    private val blockUserUseCase: BlockUserUseCase,
    private val unBlockUserUseCase: UnBlockUserUseCase,
    private val muteUserUseCase: MuteUserUseCase,
    private val unMuteUserUseCase: UnMuteUserUseCase,
    loggerFactory: Logger.Factory,
    instanceInfoService: InstanceInfoService,
    private val userRepository: UserRepository,
    private val toggleFollowUseCase: ToggleFollowUseCase,
    private val apResolverService: ApResolverService,
    private val savedStateHandle: SavedStateHandle,
    private val userDetailTabTypeFactory: UserDetailTabTypeFactory,
    private val toggleUserTimelineAddTabUseCase: ToggleUserTimelineAddTabUseCase,
    private val toggleNotifyUserPostsUseCase: ToggleNotifyUserPostsUseCase,
    private val userIdResolver: UserIdResolver,
    private val userActionAppGlobalErrorStore: UserActionAppGlobalErrorStore,
    configRepository: LocalConfigRepository,
) : ViewModel() {

    companion object;

    private val logger = loggerFactory.create("UserDetailViewModel")

    private val userId =
        savedStateHandle.getStateFlow<String?>(UserDetailActivity.EXTRA_USER_ID, null)
    private val specifiedAccountId =
        savedStateHandle.getStateFlow<Long?>(UserDetailActivity.EXTRA_ACCOUNT_ID, null)


    @OptIn(ExperimentalCoroutinesApi::class)
    val currentAccount = specifiedAccountId.flatMapLatest { accountId ->
        accountStore.getOrCurrent(accountId)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null,
    )

    private val fqdnUserName =
        savedStateHandle.getStateFlow<String?>(UserDetailActivity.EXTRA_USER_NAME, null)

//    private val _errors = MutableSharedFlow<Throwable>(extraBufferCapacity = 100)
//    val errors = _errors.asSharedFlow()

    private val userProfileArgType = UserProfileArgTypeCombiner(viewModelScope).create(
        userId,
        fqdnUserName,
        currentAccount,
    ).stateIn(viewModelScope, SharingStarted.Lazily, UserProfileArgType.None)


    @OptIn(ExperimentalCoroutinesApi::class)
    val userState = userProfileArgType.flatMapLatest {
        when (val type = it) {
            is UserProfileArgType.FqdnUserName -> {
                userRepository.observe(type.currentAccount.accountId, type.fqdnUserName)
            }

            UserProfileArgType.None -> flowOf(null)
            is UserProfileArgType.UserId -> {
                userRepository.observe(type.userId)
            }
        }
    }.mapNotNull {
        it?.castAndPartiallyFill()
    }.catch {
        logger.error("observe user error", it)
        userActionAppGlobalErrorStore.dispatch(
            AppGlobalError(
                "UserDetailViewModel.userState",
                AppGlobalError.ErrorLevel.Warning,
                StringSource("Load user failed"),
                it
            )
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    val isTodayBirthday = userState.map {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        it?.info?.birthday?.let { birthday ->
            birthday.monthNumber == today.monthNumber && birthday.dayOfMonth == today.dayOfMonth
        } ?: false
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val originProfileUrl = userState.filterNotNull().map {
        val account = accountRepository.get(it.id.accountId).getOrThrow()
        it.getRemoteProfileUrl(account)
    }.catch {
        logger.error("get profile url error", it)
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

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
        userRepository,
        instanceInfoService,
        viewModelScope,
    ).uiState

    @OptIn(ExperimentalCoroutinesApi::class)
    val renoteMuteState = userState.filterNotNull().flatMapLatest {
        renoteMuteRepository.observeOne(it.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val canVisibleFollowFollower = combine(
        userState.filterNotNull(),
        currentAccount.filterNotNull(),
    ) { user, account ->
        val isFollowing = user.related?.isFollowing ?: false
        val isMe = user.id.id == account.remoteId
        when(user.info.ffVisibility) {
            User.FollowerFollowerVisibility.Public -> true
            User.FollowerFollowerVisibility.Followers -> isMe || isFollowing
            User.FollowerFollowerVisibility.Private -> isMe
            null -> true
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)


    init {
        sync()
    }


    private fun sync() {
        viewModelScope.launch {
            getUserId().flatMapCancellableCatching { userId ->
                userRepository.sync(userId)
            }.onFailure {
                logger.error("user sync error", it)
                userActionAppGlobalErrorStore.dispatch(
                    AppGlobalError(
                        "UserDetailViewModel.sync",
                        AppGlobalError.ErrorLevel.Warning,
                        StringSource("User data sync failed"),
                        it
                    )
                )
            }
        }
    }


    fun changeFollow() {
        viewModelScope.launch {
            userState.value?.let { user ->
                toggleFollowUseCase(user.id).onFailure {
                    logger.error("unmute failed", e = it)
                    userActionAppGlobalErrorStore.dispatch(
                        AppGlobalError(
                            "UserDetailViewModel.changeFollow",
                            AppGlobalError.ErrorLevel.Warning,
                            StringSource("follow/unfollow failed"),
                            it
                        )
                    )
                }
            }

        }
    }

    fun mute(expiredAt: Instant?) {
        viewModelScope.launch {
            userState.value?.let { user ->
                muteUserUseCase(CreateMute(user.id, expiredAt)).onFailure {
                    logger.error("unmute", e = it)
                    userActionAppGlobalErrorStore.dispatch(
                        AppGlobalError(
                            "UserDetailViewModel.mute",
                            AppGlobalError.ErrorLevel.Warning,
                            StringSource("mute failed"),
                            it
                        )
                    )
                }
            }
        }
    }

    fun unmute() {
        viewModelScope.launch {
            userState.value?.let { user ->
                unMuteUserUseCase(user.id).onFailure {
                    logger.error("unmute", e = it)
                    userActionAppGlobalErrorStore.dispatch(
                        AppGlobalError(
                            "UserDetailViewModel.unmute",
                            AppGlobalError.ErrorLevel.Warning,
                            StringSource("unmute failed"),
                            it
                        )
                    )
                }
            }
        }
    }

    fun block() {
        viewModelScope.launch {
            userState.value?.let { user ->
                blockUserUseCase(user.id).onFailure {
                    logger.error("block failed", it)
                    userActionAppGlobalErrorStore.dispatch(
                        AppGlobalError(
                            "UserDetailViewModel.block",
                            AppGlobalError.ErrorLevel.Warning,
                            StringSource("block failed"),
                            it
                        )
                    )
                }
            }
        }
    }

    fun unblock() {
        viewModelScope.launch {
            userState.value?.let { user ->
                unBlockUserUseCase(user.id).onFailure {
                    logger.info("unblock failed", e = it)
                    userActionAppGlobalErrorStore.dispatch(
                        AppGlobalError(
                            "UserDetailViewModel.unblock",
                            AppGlobalError.ErrorLevel.Warning,
                            StringSource("unblock failed"),
                            it
                        )
                    )
                }
            }
        }
    }

    fun changeNickname(name: String) {
        viewModelScope.launch {
            findUser().mapCancellableCatching { user ->
                updateNicknameUseCase(user, name)
            }.onSuccess {
                logger.debug("ニックネーム更新処理成功")
            }.onFailure {
                logger.error("ニックネーム更新処理失敗", e = it)
                userActionAppGlobalErrorStore.dispatch(
                    AppGlobalError(
                        "UserDetailViewModel.changeNickname",
                        AppGlobalError.ErrorLevel.Warning,
                        StringSource("change nickname failed"),
                        it
                    )
                )
            }
        }
    }

    fun deleteNickname() {
        viewModelScope.launch {
            findUser().mapCancellableCatching { user ->
                deleteNicknameUseCase(user)
            }.onSuccess {
                logger.debug("ニックネーム削除処理成功")
            }.onFailure {
                logger.error("ニックネーム削除失敗", e = it)
                userActionAppGlobalErrorStore.dispatch(
                    AppGlobalError(
                        "UserDetailViewModel.deleteNickname",
                        AppGlobalError.ErrorLevel.Warning,
                        StringSource("delete nickname failed"),
                        it
                    )
                )
            }
        }
    }

    fun toggleUserTimelineTab() {
        viewModelScope.launch {
            getUserId().flatMapCancellableCatching {
                toggleUserTimelineAddTabUseCase(it)
            }.onFailure {
                logger.error("toggle user timeline tab failed", it)
                userActionAppGlobalErrorStore.dispatch(
                    AppGlobalError(
                        "UserDetailViewModel.toggleUserTimelineTab",
                        AppGlobalError.ErrorLevel.Warning,
                        StringSource("add/remove user timeline tab failed"),
                        it
                    )
                )
            }
        }
    }

    fun toggleNotifyUserPosts() {
        viewModelScope.launch {
            getUserId().flatMapCancellableCatching {
                toggleNotifyUserPostsUseCase(it)
            }.onFailure {
                logger.error("toggle user notify posts failed", it)
                userActionAppGlobalErrorStore.dispatch(
                    AppGlobalError(
                        "UserDetailViewModel.toggleNotifyUserPosts",
                        AppGlobalError.ErrorLevel.Warning,
                        StringSource("on/off notify user posts failed"),
                        it
                    )
                )
            }
        }
    }

    fun muteRenotes() {
        viewModelScope.launch {
            getUserId().flatMapCancellableCatching {
                renoteMuteRepository.create(it)
            }.onFailure {
                userActionAppGlobalErrorStore.dispatch(
                    AppGlobalError(
                        "UserDetailViewModel.muteRenotes",
                        AppGlobalError.ErrorLevel.Warning,
                        StringSource("mute renote failed"),
                        it
                    )
                )
            }
        }
    }

    fun unMuteRenotes() {
        viewModelScope.launch {
            getUserId().flatMapCancellableCatching {
                renoteMuteRepository.delete(it)
            }.onFailure {
                userActionAppGlobalErrorStore.dispatch(
                    AppGlobalError(
                        "UserDetailViewModel.unMuteRenotes",
                        AppGlobalError.ErrorLevel.Warning,
                        StringSource("unmute renote failed"),
                        it
                    )
                )
            }
        }
    }

    fun setCurrentAccount(accountId: Long) {
        viewModelScope.launch {
            accountRepository.get(accountId).mapCancellableCatching {
                it to apResolverService.resolve(getUserId().getOrThrow(), accountId).getOrThrow()
            }.onSuccess { (account, resolved) ->
                savedStateHandle[UserDetailActivity.EXTRA_USER_ID] = resolved.id.id
                savedStateHandle[UserDetailActivity.EXTRA_ACCOUNT_ID] = resolved.id.accountId
                accountStore.setCurrent(account)
            }.onFailure {
                logger.error("setCurrentAccount failed", it)
                userActionAppGlobalErrorStore.dispatch(
                    AppGlobalError(
                        "UserDetailViewModel.setCurrentAccount",
                        AppGlobalError.ErrorLevel.Warning,
                        StringSource("switch account failed"),
                        it
                    )
                )
            }
        }
    }

    private suspend fun findUser(): Result<User> = runCancellableCatching {
        userRepository.find(getUserId().getOrThrow())
    }


    private suspend fun getUserId(): Result<User.Id> = runCancellableCatching {
        val strUserId = savedStateHandle.get<String?>(UserDetailActivity.EXTRA_USER_ID)
        val specifiedAccountId = savedStateHandle.get<Long?>(UserDetailActivity.EXTRA_ACCOUNT_ID)
        val fqdnUserName = savedStateHandle.get<String?>(UserDetailActivity.EXTRA_USER_NAME)
        userIdResolver(
            userId = strUserId,
            acct = fqdnUserName,
            specifiedAccountId = specifiedAccountId,
        ).getOrThrow()
    }
}

sealed interface UserProfileArgType {
    data class UserId(val userId: User.Id) : UserProfileArgType

    data class FqdnUserName(val fqdnUserName: String, val currentAccount: Account) :
        UserProfileArgType

    object None : UserProfileArgType

}

