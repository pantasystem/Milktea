package net.pantasystem.milktea.auth.viewmodel.app

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.api.mastodon.apps.CreateApp
import net.pantasystem.milktea.api.misskey.MisskeyAPIServiceBuilder
import net.pantasystem.milktea.api.misskey.auth.AppSecret
import net.pantasystem.milktea.api.misskey.auth.fromDTO
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.auth.viewmodel.Permissions
import net.pantasystem.milktea.common.BuildConfig
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.account.newAccount
import net.pantasystem.milktea.data.infrastructure.auth.Authorization
import net.pantasystem.milktea.data.infrastructure.auth.custom.AccessToken
import net.pantasystem.milktea.data.infrastructure.auth.custom.CustomAuthStore
import net.pantasystem.milktea.data.infrastructure.auth.custom.createAuth
import net.pantasystem.milktea.data.infrastructure.toUser
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.app.AppType
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.model.sw.register.SubscriptionRegistration
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import java.util.regex.Pattern
import javax.inject.Inject


class AuthStateHelper @Inject constructor(
    private val mastodonAPIProvider: MastodonAPIProvider,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    val metaRepository: MetaRepository,
    private val customAuthStore: CustomAuthStore,
    private val misskeyAPIServiceBuilder: MisskeyAPIServiceBuilder,
    val accountRepository: AccountRepository,
    val accountStore: AccountStore,
    val subscriptionRegistration: SubscriptionRegistration,
    val userDataSource: UserDataSource,

    ) {
    private val urlPattern =
        Pattern.compile("""(https?)(://)([-_.!~*'()\[\]a-zA-Z0-9;/?:@&=+${'$'},%#]+)""")

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
            is AppType.Misskey -> {
                val secret = app.secret
                val authApi = misskeyAPIServiceBuilder.buildAuthAPI(instanceBase)
                val session = authApi.generateSession(
                    AppSecret(
                        secret!!
                    )
                ).body()
                    ?: throw IllegalStateException("セッションの作成に失敗しました。")
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
            is InstanceType.Misskey -> {
                val version = instanceType.instance.getVersion()
                val misskeyAPI = misskeyAPIProvider.get(url, version)
                val app = misskeyAPI.createApp(
                    net.pantasystem.milktea.api.misskey.app.CreateApp(
                        null,
                        appName,
                        "misskey android application",
                        CALL_BACK_URL,
                        permission = Permissions.getPermission(version)
                    )
                ).throwIfHasError().body()
                    ?: throw IllegalStateException("Appの作成に失敗しました。")
                return AppType.fromDTO(app)
            }
        }

    }

    suspend fun getMeta(url: String): InstanceType {
        if (urlPattern.matcher(url).find()) {
            val misskey = withContext(Dispatchers.IO) {
                metaRepository.find(url).onFailure {
                    Log.e("AppAuthViewModel", "fetch meta error", it)
                }.getOrNull()
            }
            val mastodon =
                withContext(Dispatchers.IO) {
                    if (!BuildConfig.DEBUG) {
                        return@withContext null
                    }
                    runCatching {
                        mastodonAPIProvider.get(url)
                            .getInstance()
                    }.getOrNull()
                }
            if (misskey != null) {
                return InstanceType.Misskey(misskey)
            }
            if (mastodon != null) {
                return InstanceType.Mastodon(mastodon)
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
        val user = when (a.accessToken) {
            is AccessToken.Mastodon -> {
                (a.accessToken as AccessToken.Mastodon).account.toModel(account)
            }
            is AccessToken.Misskey -> {
                (a.accessToken as AccessToken.Misskey).user.toUser(
                    account,
                    true
                ) as User.Detail
            }
        }
        userDataSource.add(user)
        accountStore.addAccount(account)
        subscriptionRegistration.register(account.accountId)

        return Authorization.Finish(account, user)
    }

    fun checkUrlPattern(url: String): Boolean {
        return urlPattern.matcher(url).find()
    }
}