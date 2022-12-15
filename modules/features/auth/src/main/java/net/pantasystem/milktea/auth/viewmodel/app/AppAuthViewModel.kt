package net.pantasystem.milktea.auth.viewmodel.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.asLoadingStateFlow
import net.pantasystem.milktea.data.infrastructure.auth.Authorization
import net.pantasystem.milktea.model.account.AccountRepository
import java.util.*
import javax.inject.Inject


const val CALL_BACK_URL = "misskey://app_auth_callback"

@ExperimentalCoroutinesApi
@Suppress("UNCHECKED_CAST")
@HiltViewModel
class AppAuthViewModel @Inject constructor(
    private val authService: AuthStateHelper,
    loggerFactory: Logger.Factory,
    val accountRepository: AccountRepository,
    val accountStore: AccountStore,
) : ViewModel() {

    private val logger = loggerFactory.create("AppAuthViewModel")

    val instanceDomain = MutableStateFlow("")

    val isOpenInWebView = MutableStateFlow(false)

    val username = MutableStateFlow("")

    val appName = MutableStateFlow("Milktea")

    private val metaState = instanceDomain.map {
        authService.toEnableUrl(it)
    }.filter {
        authService.checkUrlPattern(it)
    }.flatMapLatest {
        suspend {
            authService.getMeta(it)
        }.asLoadingStateFlow()
    }.flowOn(Dispatchers.IO).stateIn(
        viewModelScope, SharingStarted.Lazily, ResultState.Fixed(
            StateContent.NotExist()
        )
    )



    private val authUserInputState = combine(instanceDomain, appName) { domain, name ->
        AuthUserInputState(
            instanceDomain = authService.toEnableUrl(domain),
            appName = name,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AuthUserInputState("misskey.io", "Milktea")
    )

    private val instanceInfo = authUserInputState.flatMapLatest {
        metaState
    }.combine(authUserInputState) { meta, inputState ->
        BeforeAuthState(
            meta = meta,
            inputState = inputState
        )
    }.flowOn(Dispatchers.IO).stateIn(
        viewModelScope, SharingStarted.Lazily, BeforeAuthState(
            meta = ResultState.Fixed(
                StateContent.NotExist()
            ),
            inputState = authUserInputState.value,
        )
    )

    private val startAuthEventFlow = MutableSharedFlow<Long>(extraBufferCapacity = 100)

    private val waiting4UserApprove = startAuthEventFlow.flatMapLatest {
        instanceInfo
    }.flatMapLatest {
        suspend {
            when (val meta = it.meta.content) {
                is StateContent.Exist -> authService.createWaiting4Approval(
                    it.inputState.instanceDomain,
                    authService.createApp(
                        it.inputState.instanceDomain,
                        instanceType = meta.rawContent,
                        appName = it.inputState.appName
                    )
                )

                is StateContent.NotExist -> throw IllegalStateException()
            }
        }.asLoadingStateFlow()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ResultState.Fixed(StateContent.NotExist())
    )

    val waiting4UserAuthorizationStepEvent = waiting4UserApprove.mapNotNull{
        (it.content as? StateContent.Exist)?.rawContent
    }.distinctUntilChanged().shareIn(viewModelScope, SharingStarted.WhileSubscribed(5_000))

    private val approved = MutableStateFlow<Authorization.Approved?>(null)


    private val confirmAddAccountEventFlow = MutableSharedFlow<Long>(extraBufferCapacity = 100)
    val finished = confirmAddAccountEventFlow.flatMapLatest {
        approved.filterNotNull()
    }.map {
        runCatching {
            authService.createAccount(it)
        }.onFailure {
            logger.error("アカウント登録処理失敗", it)
        }.getOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)


    val state = combine(
        instanceInfo,
        waiting4UserApprove,
        approved,
        finished
    ) { formState, waiting4Approve, approved, finished ->
        AuthUiState(
            formState = formState.inputState,
            metaState = formState.meta,
            stateType = when {
                finished != null -> finished
                approved != null -> approved
                waiting4Approve.content is StateContent.Exist -> (waiting4Approve.content as StateContent.Exist).rawContent
                else -> Authorization.BeforeAuthentication
            },
            waiting4ApproveState = waiting4Approve
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AuthUiState(
            formState = authUserInputState.value,
            metaState = metaState.value,
            stateType = Authorization.BeforeAuthentication
        )
    )

    val errors = state.map {
        it.errors
    }

    init {
        // NOTE: misskey.ioにログインしているアカウントが一つもなければmisskey.ioをデフォルト表示する
        // NOTE: またioにログインしていた場合は空にする
        viewModelScope.launch(Dispatchers.IO) {
            accountRepository.findAll().onSuccess { accounts ->
                if (!accounts.any { it.getHost() == "misskey.io" }) {
                    instanceDomain.value = "misskey.io"
                }
            }.onFailure {
                logger.error("findAll accounts failure", it)
            }
        }
    }

    fun clearHostName() {
        instanceDomain.value = ""
    }


    fun auth() {
        startAuthEventFlow.tryEmit(Date().time)
    }
}

