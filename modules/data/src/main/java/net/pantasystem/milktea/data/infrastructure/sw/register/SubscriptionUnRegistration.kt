package net.pantasystem.milktea.data.infrastructure.sw.register

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.api.misskey.register.UnSubscription
import net.pantasystem.milktea.common.APIError
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.sw.register.SubscriptionUnRegistration
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class SubscriptionUnRegistrationImpl @Inject constructor(
    val accountRepository: AccountRepository,
    val lang: String,
    val misskeyAPIProvider: MisskeyAPIProvider,
    private val publicKey: String,
    private val auth: String,
    private val endpointBase: String,
    private val context: Context,
    private val mastodonAPIProvider: MastodonAPIProvider,
) : SubscriptionUnRegistration {


    override suspend fun unregister(accountId: Long) {
        withContext(Dispatchers.IO) {
            when(GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)) {
                ConnectionResult.SUCCESS -> {
                    val account = accountRepository.get(accountId).getOrThrow()
                    when(account.instanceType) {
                        Account.InstanceType.MISSKEY,Account.InstanceType.FIREFISH -> {
                            val token = FirebaseMessaging.getInstance().token.asSuspend()
                            val apiProvider = misskeyAPIProvider.get(account)
                            val endpoint = EndpointBuilder(
                                accountId = account.accountId,
                                deviceToken = token,
                                lang = lang,
                                publicKey = publicKey,
                                endpointBase = endpointBase,
                                auth = auth,
                                instanceType = account.instanceType
                            ).build()
                            try {
                                apiProvider.swUnRegister(
                                    UnSubscription(
                                        i = account.token,
                                        endpoint = endpoint
                                    )
                                ).throwIfHasError()
                            } catch (e: APIError.ForbiddenException) {
                                return@withContext
                            }
                            catch (e: APIError.AuthenticationException) {
                                return@withContext
                            } catch (e: APIError.SomethingException) {
                                if (e.statusCode == 410) {
                                    return@withContext
                                }
                                throw e
                            }
                        }
                        Account.InstanceType.MASTODON,Account.InstanceType.PLEROMA -> {
                            try {
                                mastodonAPIProvider.get(account).unSubscribePushNotification()
                                    .throwIfHasError()
                            } catch (e: APIError.ForbiddenException) {
                                return@withContext
                            }
                            catch (e: APIError.AuthenticationException) {
                                return@withContext
                            } catch (e: APIError.SomethingException) {
                                if (e.statusCode == 410) {
                                    return@withContext
                                }
                                throw e
                            }
                        }
                    }


                }
                else -> {

                }
            }
        }
    }
}

suspend fun<T> Task<T>.asSuspend() = suspendCoroutine<T> { continuation ->
    addOnSuccessListener {
        continuation.resume(it)
    }
    addOnFailureListener {
        throw it
    }
}