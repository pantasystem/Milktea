package net.pantasystem.milktea.auth.viewmodel.app

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.api.mastodon.apps.CreateApp
import net.pantasystem.milktea.api.misskey.I
import net.pantasystem.milktea.api.misskey.MisskeyAPIServiceBuilder
import net.pantasystem.milktea.api.misskey.auth.AppSecret
import net.pantasystem.milktea.api.misskey.auth.Session
import net.pantasystem.milktea.api.misskey.auth.SignInRequest
import net.pantasystem.milktea.api.misskey.auth.fromDTO
import net.pantasystem.milktea.api.misskey.auth.fromFirefishDTO
import net.pantasystem.milktea.api.misskey.auth.fromPleromaDTO
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.auth.viewmodel.Permissions
import net.pantasystem.milktea.common.APIError
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.converters.UserDTOEntityConverter
import net.pantasystem.milktea.data.infrastructure.account.newAccount
import net.pantasystem.milktea.data.infrastructure.auth.Authorization
import net.pantasystem.milktea.data.infrastructure.auth.custom.AccessToken
import net.pantasystem.milktea.data.infrastructure.auth.custom.CustomAuthStore
import net.pantasystem.milktea.data.infrastructure.auth.custom.createAuth
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.app.AppType
import net.pantasystem.milktea.model.instance.MastodonInstanceInfo
import net.pantasystem.milktea.model.instance.MastodonInstanceInfoRepository
import net.pantasystem.milktea.model.instance.Meta
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import net.pantasystem.milktea.model.nodeinfo.NodeInfoRepository
import net.pantasystem.milktea.model.sw.register.SubscriptionRegistration
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import java.net.URL
import java.util.regex.Pattern
import javax.inject.Inject

val urlPattern: Pattern = Pattern.compile("""(https?)(://)([-_.!~*'()\[\]a-zA-Z0-9;/?:@&=+${'$'},%#]+)""")

class AuthStateHelper @Inject constructor(
    private val mastodonAPIProvider: MastodonAPIProvider,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val metaRepository: MetaRepository,
    private val customAuthStore: CustomAuthStore,
    private val misskeyAPIServiceBuilder: MisskeyAPIServiceBuilder,
    private val accountRepository: AccountRepository,
    private val accountStore: AccountStore,
    private val subscriptionRegistration: SubscriptionRegistration,
    private val userDataSource: UserDataSource,
    private val nodeInfoRepository: NodeInfoRepository,
    private val mastodonInstanceInfoRepository: MastodonInstanceInfoRepository,
    private val userDTOEntityConverter: UserDTOEntityConverter
) {


    suspend fun createWaiting4Approval(
        instanceBase: String,
        app: AppType,
    ): Authorization.Waiting4UserAuthorization {
        return when (app) {
            is AppType.Mastodon -> {
                val authState = app.createAuth(instanceBase, "read write")
                customAuthStore.setCustomAuthBridge(authState)
                Authorization.Waiting4UserAuthorization.Mastodon(
                    instanceBase,
                    client = app,
                    scope = "read write"
                )
            }
            is AppType.Pleroma -> {
                val authState = app.createAuth(instanceBase, "read write")
                customAuthStore.setCustomAuthBridge(authState)
                Authorization.Waiting4UserAuthorization.Pleroma(
                    instanceBase,
                    client = app,
                    scope = "read write"
                )
            }
            is AppType.Misskey -> {
                val secret = app.secret
                val session = generateMisskeySession(instanceBase, secret)

                customAuthStore.setCustomAuthBridge(
                    app.createAuth(instanceBase, session)
                )
                Authorization.Waiting4UserAuthorization.Misskey(
                    instanceBase,
                    appSecret = app.secret!!,
                    session = session,
                    viaName = app.name
                )
            }
            is AppType.Firefish -> {
                val secret = app.secret
                val session = generateMisskeySession(instanceBase, secret)

                customAuthStore.setCustomAuthBridge(
                    app.createAuth(instanceBase, session)
                )
                Authorization.Waiting4UserAuthorization.Firefish(
                    instanceBase,
                    appSecret = app.secret!!,
                    session = session,
                    viaName = app.name
                )
            }
        }
    }

    suspend fun createApp(
        url: String,
        instanceType: InstanceType,
        appName: String
    ): AppType {
        when (instanceType) {
            is InstanceType.Mastodon -> {
                val app = mastodonAPIProvider.get(url)
                    .createApp(
                        CreateApp(
                            clientName = appName,
                            redirectUris = CALL_BACK_URL,
                            scopes = "read write"
                        )
                    ).throwIfHasError().body()
                    ?: throw IllegalStateException("Appの作成に失敗しました。")
                return AppType.fromDTO(app)
            }
            is InstanceType.Pleroma -> {
                val app = mastodonAPIProvider.get(url)
                    .createApp(
                        CreateApp(
                            clientName = appName,
                            redirectUris = CALL_BACK_URL,
                            scopes = "read write"
                        )
                    ).throwIfHasError().body()
                    ?: throw IllegalStateException("Appの作成に失敗しました。")

                return AppType.fromPleromaDTO(app)
            }
            is InstanceType.Misskey -> {
                val version = instanceType.instance.getVersion()
                val misskeyAPI = misskeyAPIProvider.get(url)
                val app = misskeyAPI.createApp(
                    net.pantasystem.milktea.api.misskey.app.CreateApp(
                        null,
                        appName,
                        "misskey android application",
                        CALL_BACK_URL,
                        permission = Permissions.getPermission(instanceType.softwareType)
                            ?: Permissions.getPermission(version)
                    )
                ).throwIfHasError().body()
                    ?: throw IllegalStateException("Appの作成に失敗しました。")
                return AppType.fromDTO(app)
            }
            is InstanceType.Firefish -> {
                val misskeyAPI = misskeyAPIProvider.get(url)
                val app = misskeyAPI.createApp(
                    net.pantasystem.milktea.api.misskey.app.CreateApp(
                        null,
                        appName,
                        "misskey android application",
                        CALL_BACK_URL,
                        permission = Permissions.getPermission(instanceType.softwareType)
                    )
                ).throwIfHasError().body()
                    ?: throw IllegalStateException("Appの作成に失敗しました。")
                return AppType.fromFirefishDTO(app)
            }
        }

    }

    suspend fun getMeta(url: String): InstanceType {
        if (urlPattern.matcher(url).find()) {
            val nodeInfo = nodeInfoRepository.find(URL(url).host).getOrNull()

            val misskey: Meta?
            val mastodon: MastodonInstanceInfo?
            val pleroma: MastodonInstanceInfo?
            val firefish: Meta?

            suspend fun fetchMeta(): Meta? {
                return withContext(Dispatchers.IO) {
                    metaRepository.find(url).onFailure {
                        Log.e("AppAuthViewModel", "fetch meta error", it)
                    }.getOrNull()
                }
            }

            suspend fun fetchInstance(): MastodonInstanceInfo? {
                return withContext(Dispatchers.IO) {
                    mastodonInstanceInfoRepository.find(url).getOrNull()
                }
            }
            when (nodeInfo?.type) {
                is NodeInfo.SoftwareType.Mastodon -> {
                    mastodon = fetchInstance()
                    misskey = null
                    pleroma = null
                    firefish = null
                }
                is NodeInfo.SoftwareType.Misskey -> {
                    misskey = fetchMeta()
                    mastodon = null
                    pleroma = null
                    firefish = null
                }
                is NodeInfo.SoftwareType.Pleroma -> {
                    pleroma = fetchInstance()
                    misskey = null
                    mastodon = null
                    firefish = null
                }
                is NodeInfo.SoftwareType.Firefish -> {
                    misskey = null
                    mastodon = null
                    pleroma = null
                    firefish = fetchMeta()
                }
                else -> {
                    misskey = fetchMeta()
                    mastodon = if (misskey == null) {
                        fetchInstance()
                    } else {
                        null
                    }
                    pleroma = null
                    firefish = null
                }
            }

            if (misskey != null) {
                return InstanceType.Misskey(misskey, nodeInfo?.type as? NodeInfo.SoftwareType.Misskey)
            }
            if (mastodon != null) {
                return InstanceType.Mastodon(mastodon, nodeInfo?.type as? NodeInfo.SoftwareType.Mastodon)
            }

            if (pleroma != null) {
                return InstanceType.Pleroma(pleroma, nodeInfo?.type as? NodeInfo.SoftwareType.Pleroma)
            }
            if (firefish != null) {
                return InstanceType.Firefish(firefish, nodeInfo?.type as? NodeInfo.SoftwareType.Firefish)
            }
            throw IllegalArgumentException()
        } else {
            throw IllegalArgumentException("not support pattern url: $url")
        }
    }

    fun toEnableUrl(base: String): String {
        var url = if (base.startsWith("https://")) {
            base
        } else {
            "https://$base"
        }.replace(" ", "").replace("\t", "").replace("　", "")

        if (url.endsWith("/")) {
            url = url.substring(url.indices)
        }
        return url
    }

    suspend fun createAccount(a: Authorization.Approved): Authorization.Finish {
        val account = accountRepository.add(
            a.accessToken.newAccount(a.instanceBaseURL),
            false
        ).getOrThrow()
        val user = when (val token = a.accessToken) {
            is AccessToken.Mastodon -> {
                token.account.toModel(account)
            }
            is AccessToken.Misskey -> {
                userDTOEntityConverter.convert(
                    account,
                    token.user,
                    true
                ) as User.Detail
            }
            is AccessToken.MisskeyIdAndPassword -> {
                userDTOEntityConverter.convert(
                    account,
                    token.user,
                    true
                ) as User.Detail
            }
            is AccessToken.Pleroma -> {
                (a.accessToken as AccessToken.Pleroma).account.toModel(account)
            }
            is AccessToken.Firefish -> {
                userDTOEntityConverter.convert(
                    account,
                    token.user,
                    true
                ) as User.Detail
            }
        }
        userDataSource.add(user)
        accountStore.addAccount(account)
        subscriptionRegistration.register(account.accountId)

        return Authorization.Finish(account, user)
    }

    suspend fun signIn(formState: AuthUserInputState): Result<AccessToken.MisskeyIdAndPassword> =
        runCancellableCatching {
            withContext(Dispatchers.IO) {

                val baseUrl = toEnableUrl(requireNotNull(formState.host))
                val api = misskeyAPIServiceBuilder.buildAuthAPI(baseUrl)
                require(formState.isIdPassword) {
                    "入力がid, passwordのパターンと異なります"
                }
                val res = api.signIn(
                    SignInRequest(
                        username = requireNotNull(formState.username),
                        password = formState.password,
                    )
                ).throwIfHasError().body()
                requireNotNull(res)
                val userDTO = misskeyAPIProvider.get(baseUrl).i(
                    I(
                        res.i
                    )
                ).throwIfHasError().body()
                AccessToken.MisskeyIdAndPassword(
                    baseUrl = baseUrl,
                    accessToken = res.i,
                    user = requireNotNull(userDTO)
                )
            }
        }

    fun checkUrlPattern(url: String): Boolean {
        return urlPattern.matcher(url).find()
    }

    private suspend fun generateMisskeySession(instanceBase: String, secret: String?, retryCount: Int = 0): Session {
        val authApi = misskeyAPIServiceBuilder.buildAuthAPI(instanceBase)
        try {
            return authApi.generateSession(
                AppSecret(
                    secret!!
                )
            ).throwIfHasError().body()!!
        } catch (e: APIError) {
            if (retryCount < 100) {
                delay(100)
                return generateMisskeySession(instanceBase, secret, retryCount + 1)
            }
            throw e
        }
    }
}