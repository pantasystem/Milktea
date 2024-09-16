package net.pantasystem.milktea.auth.viewmodel.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import net.pantasystem.milktea.api.misskey.MisskeyAPIServiceBuilder
import net.pantasystem.milktea.api.misskey.auth.UserKey
import net.pantasystem.milktea.auth.suggestions.InstanceSuggestionsPagingModel
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.asLoadingStateFlow
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.infrastructure.auth.Authorization
import net.pantasystem.milktea.data.infrastructure.auth.custom.toFirefishModel
import net.pantasystem.milktea.data.infrastructure.auth.custom.toModel
import net.pantasystem.milktea.model.account.ClientIdRepository
import net.pantasystem.milktea.model.instance.SyncMetaExecutor
import java.util.Date
import javax.inject.Inject


const val CALL_BACK_URL = "misskey://app_auth_callback"

@ExperimentalCoroutinesApi
@HiltViewModel
class AppAuthViewModel @Inject constructor(
    private val authService: AuthStateHelper,
    loggerFactory: Logger.Factory,
    private val misskeyAPIServiceBuilder: MisskeyAPIServiceBuilder,
    private val getAccessToken: GetAccessToken,
    private val clientIdRepository: ClientIdRepository,
    private val syncMetaExecutor: SyncMetaExecutor,
    private val instanceSuggestionsPagingModel: InstanceSuggestionsPagingModel,
) : ViewModel() {

    private val logger = loggerFactory.create("AppAuthViewModel")

    val instanceDomain = MutableStateFlow("")

    val isOpenInWebView = MutableStateFlow(false)

    val username = MutableStateFlow("")

    val appName = MutableStateFlow("Milktea")

    val password = MutableStateFlow("")

    private val metaState = instanceDomain.map {
        authService.convertEnableUrl(it)
    }.filter {
        authService.checkUrlPattern(it)
    }.flatMapLatest {
        suspend {
            authService.getInstanceInfoType(it)
        }.asLoadingStateFlow()
    }.flowOn(Dispatchers.IO).stateIn(
        viewModelScope, SharingStarted.Lazily, ResultState.Fixed(
            StateContent.NotExist()
        )
    )

    private val misskeyInstances = instanceSuggestionsPagingModel.state.map {
        (it.content as? StateContent.Exist)?.rawContent ?: emptyList()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    private val isPrivacyPolicyAgreement = MutableStateFlow(false)
    private val isTermsOfServiceAgreement = MutableStateFlow(false)

    private val isAcceptMastodonAlphaTest = MutableStateFlow(false)

    private val checkBoxes = combine(
        isPrivacyPolicyAgreement,
        isTermsOfServiceAgreement,
        isAcceptMastodonAlphaTest,
    ) { privacyPolicy, termsOfService, isAcceptMastodonAlphaTest ->
        CheckBoxes(
            isPrivacyPolicyAgreement = privacyPolicy,
            isTermsOfServiceAgreement = termsOfService,
            isAcceptMastodonAlphaTest = isAcceptMastodonAlphaTest
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        CheckBoxes()
    )

    private val authUserInputState = combine(
        instanceDomain,
        appName,
        password,
        checkBoxes,
    ) { domain, name, password, checkBoxes ->
        AuthUserInputState(
            instanceDomain = authService.convertEnableUrl(domain),
            appName = name,
            rawInputInstanceDomain = domain,
            password = password,
            isPrivacyPolicyAgreement = checkBoxes.isPrivacyPolicyAgreement,
            isTermsOfServiceAgreement = checkBoxes.isTermsOfServiceAgreement,
            isAcceptMastodonAlphaTest = checkBoxes.isAcceptMastodonAlphaTest,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AuthUserInputState(
            "misskey.io",
            "",
            "Milktea",
            "",
            isTermsOfServiceAgreement = false,
            isPrivacyPolicyAgreement = false,
            isAcceptMastodonAlphaTest = false,
        )
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
                    val instanceBase = meta.rawContent.uri
                    logger.debug { "instanceBaseUrl: $instanceBase" }
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
    }.catch {
        logger.error("アプリの作成に失敗", it)
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
    private val finished = confirmAddAccountEventFlow.flatMapLatest {
        approved.filterNotNull()
    }.map {
        runCancellableCatching {
            authService.createAccount(it)
        }.onFailure {
            logger.error("アカウント登録処理失敗", it)
        }.onSuccess {
            syncMetaExecutor(it.account.normalizedInstanceUri)
        }.getOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val combineStates = combine(
        waiting4UserApprove,
        approved,
        finished,
        generateTokenResult
    ) { waiting4Approve, approved, finished, result ->
        CombineStates(waiting4Approve, approved, finished, result)
    }

    val state = combine(
        instanceInfo,
        combineStates,
        misskeyInstances,
    ) { formState, (waiting4Approve, approved, finished, result), misskeyInstances ->
        AuthUiState(
            formState = formState.inputState,
            instanceInfoResultState = formState.meta,
            stateType = when {
                result is GenerateTokenResult.Failure -> Authorization.BeforeAuthentication
                finished != null -> finished
                approved != null -> approved
                waiting4Approve.content is StateContent.Exist -> (waiting4Approve.content as StateContent.Exist).rawContent
                else -> Authorization.BeforeAuthentication
            },
            waiting4ApproveState = waiting4Approve,
            clientId = "clientId: ${clientIdRepository.getOrCreate().clientId}",
            misskeyInstanceInfosResponse = misskeyInstances,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AuthUiState(
            formState = authUserInputState.value,
            instanceInfoResultState = metaState.value,
            stateType = Authorization.BeforeAuthentication,
            misskeyInstanceInfosResponse = emptyList(),
        )
    )


    init {
        instanceDomain.onEach {
            instanceSuggestionsPagingModel.setQueryName(it)
            instanceSuggestionsPagingModel.onLoadNext(viewModelScope)
        }.launchIn(viewModelScope)

        waiting4UserApprove.mapNotNull {
            (it.content as? StateContent.Exist)?.rawContent
        }.filterNotNull().flatMapLatest { a ->
            (0..Int.MAX_VALUE).asFlow().map {
                delay(4000)
                when (a) {
                    is Authorization.Waiting4UserAuthorization.Misskey -> {
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
                    }

                    is Authorization.Waiting4UserAuthorization.Firefish -> {
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
                                accessToken = token.toFirefishModel(a.appSecret)
                            )
                            ResultState.Fixed(StateContent.Exist(authenticated))
                        } catch (e: Throwable) {
                            ResultState.Error(StateContent.NotExist(), e)
                        }
                    }

                    else -> {
                        ResultState.Fixed(StateContent.NotExist())
                    }
                }
            }
        }.mapNotNull {
            it.content as? StateContent.Exist
        }.onEach {
            approved.value = it.rawContent
        }.launchIn(viewModelScope + Dispatchers.IO)

        // NOTE: id, passwordのサポート
        authUserInputState.flatMapLatest { state ->
            startAuthEventFlow.distinctUntilChanged().map {
                state
            }
        }.filter {
            it.isIdPassword
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

    fun auth() {
        startAuthEventFlow.tryEmit(Date().time)
    }


    fun getAccessToken(code: String? = null, w4a: Authorization.Waiting4UserAuthorization? = null) {
        val a = w4a ?: (waiting4UserApprove.value.content as? StateContent.Exist)?.rawContent
        ?: throw IllegalStateException("現在の状態: ${state.value}でアクセストークンを取得することはできません。")
        viewModelScope.launch {

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

    fun onToggleTermsOfServiceAgreement(value: Boolean) {
        isTermsOfServiceAgreement.value = value
    }

    fun onTogglePrivacyPolicyAgreement(value: Boolean) {
        isPrivacyPolicyAgreement.value = value
    }

    fun onToggleAcceptMastodonAlphaTest(value: Boolean) {
        isAcceptMastodonAlphaTest.value = value
    }

    fun onBottomReached() {
        instanceSuggestionsPagingModel.onLoadNext(viewModelScope)
    }

}

private data class CombineStates(
    val waiting4UserApprove: ResultState<Authorization.Waiting4UserAuthorization>,
    val approved: Authorization.Approved? = null,
    val finished: Authorization.Finish? = null,
    val generateTokenResult: GenerateTokenResult = GenerateTokenResult.Fixed
)

private data class CheckBoxes(
    val isPrivacyPolicyAgreement: Boolean = false,
    val isTermsOfServiceAgreement: Boolean = false,
    val isAcceptMastodonAlphaTest: Boolean = false,
)