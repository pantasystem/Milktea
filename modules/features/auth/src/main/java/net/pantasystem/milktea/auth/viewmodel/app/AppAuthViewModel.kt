package net.pantasystem.milktea.auth.viewmodel.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.api.misskey.MisskeyAPIServiceBuilder
import net.pantasystem.milktea.api.misskey.auth.UserKey
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.*
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.auth.Authorization
import net.pantasystem.milktea.data.infrastructure.auth.custom.toModel
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
    val misskeyAPIServiceBuilder: MisskeyAPIServiceBuilder,
    val misskeyAPIProvider: MisskeyAPIProvider,
    val getAccessToken: GetAccessToken,
) : ViewModel() {

    private val logger = loggerFactory.create("AppAuthViewModel")

    val instanceDomain = MutableStateFlow("misskey.io")

    val isOpenInWebView = MutableStateFlow(false)

    val username = MutableStateFlow("")

    val appName = MutableStateFlow("Milktea")

    val password = MutableStateFlow("")

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

    private val authUserInputState = combine(
        instanceDomain,
        appName,
        password
    ) { domain, name, password ->
        AuthUserInputState(
            instanceDomain = authService.toEnableUrl(domain),
            appName = name,
            rawInputInstanceDomain = domain,
            password = password
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AuthUserInputState("misskey.io", "", "Milktea", "")
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

    private val waiting4UserApprove = instanceInfo.filterNot {
        it.inputState.isIdPassword
    }.flatMapLatest { state ->
        startAuthEventFlow.map {
            state
        }
    }.flatMapLatest {
        suspend {
                when (val meta = it.meta.content) {
                is StateContent.Exist -> {
                    val instanceBase = when(val info = meta.rawContent) {
                        is InstanceType.Mastodon -> info.instance.uri
                        is InstanceType.Misskey -> info.instance.uri
                    }
                    authService.createWaiting4Approval(
                        instanceBase,
                        authService.createApp(
                            instanceBase,
                            instanceType = meta.rawContent,
                            appName = it.inputState.appName
                        )
                    )
                }

                is StateContent.NotExist -> throw IllegalStateException()
            }
        }.asLoadingStateFlow()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ResultState.Fixed(StateContent.NotExist())
    )

    private val generateTokenResult =
        MutableStateFlow<GenerateTokenResult>(GenerateTokenResult.Fixed)

    val waiting4UserAuthorizationStepEvent = waiting4UserApprove.mapNotNull {
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
        finished,
        generateTokenResult
    ) { formState, waiting4Approve, approved, finished, result ->
        AuthUiState(
            formState = formState.inputState,
            metaState = formState.meta,
            stateType = when {
                result is GenerateTokenResult.Failure -> Authorization.BeforeAuthentication
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

    val stateTypeEvents = state.map {
        it.stateType
    }.distinctUntilChanged().shareIn(viewModelScope, SharingStarted.WhileSubscribed(5_000))

    val errors = state.map {
        it.errors
    }

    init {

        waiting4UserApprove.mapNotNull {
            (it.content as? StateContent.Exist)?.rawContent
        }.filterNotNull().flatMapLatest { a ->
            (0..Int.MAX_VALUE).asFlow().map {
                delay(4000)
                if (a is Authorization.Waiting4UserAuthorization.Misskey) {
                    try {
                        val token = misskeyAPIServiceBuilder.buildAuthAPI(a.instanceBaseURL)
                            .getAccessToken(
                                UserKey(
                                    appSecret = a.appSecret,
                                    a.session.token
                                )
                            )
                            .throwIfHasError()
                            .body()
                            ?: throw IllegalStateException("response bodyがありません。")

                        val authenticated = Authorization.Approved(
                            a.instanceBaseURL,
                            accessToken = token.toModel(a.appSecret)
                        )
                        ResultState.Fixed(StateContent.Exist(authenticated))
                    } catch (e: Throwable) {
                        ResultState.Error(StateContent.NotExist(), e)
                    }
                } else {
                    ResultState.Fixed(StateContent.NotExist())
                }
            }
        }.mapNotNull {
            it.content as? StateContent.Exist
        }.onEach {
            approved.value = it.rawContent
        }.launchIn(viewModelScope + Dispatchers.IO)

        // NOTE: id, passwordのサポート
        authUserInputState.flatMapLatest {  state ->
            startAuthEventFlow.distinctUntilChanged().map {
                state
            }
        }.map {
            authService.signIn(it)
        }.onEach { result ->
            result.onSuccess {
                approved.value = Authorization.Approved(
                    instanceBaseURL = it.baseUrl,
                    accessToken = it
                )
            }.onFailure {
                logger.error("signIn failed", it)
            }
        }.launchIn(viewModelScope)
    }

    fun clearHostName() {
        instanceDomain.value = ""
    }


    fun auth() {
        startAuthEventFlow.tryEmit(Date().time)
    }


    fun getAccessToken(code: String? = null, w4a: Authorization.Waiting4UserAuthorization? = null) {
        val a = w4a ?: (waiting4UserApprove.value.content as? StateContent.Exist)?.rawContent
        ?: throw IllegalStateException("現在の状態: ${state.value}でアクセストークンを取得することはできません。")
        viewModelScope.launch(Dispatchers.IO) {

            getAccessToken.getAccessToken(a, code).onSuccess {
                val authenticated = Authorization.Approved(
                    a.instanceBaseURL,
                    it
                )
                generateTokenResult.tryEmit(GenerateTokenResult.Success)
                approved.value = authenticated
            }.onFailure {
                generateTokenResult.tryEmit(GenerateTokenResult.Failure)
            }
        }
    }

    fun onConfirmAddAccount() {
        confirmAddAccountEventFlow.tryEmit(Date().time)
    }

}
