package net.pantasystem.milktea.data.infrastructure.ap

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.api.misskey.ap.ApResolveRequest
import net.pantasystem.milktea.api.misskey.ap.ApResolveResult
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.converters.UserDTOEntityConverter
import net.pantasystem.milktea.data.infrastructure.notes.NoteDataSourceAdder
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.GetAccount
import net.pantasystem.milktea.model.ap.ApResolver
import net.pantasystem.milktea.model.ap.ApResolverRepository
import net.pantasystem.milktea.model.user.UserDataSource
import javax.inject.Inject

class ApResolverRepositoryImpl @Inject constructor(
    private val apiProvider: MisskeyAPIProvider,
    private val mastodonAPIProvider: MastodonAPIProvider,
    private val getAccount: GetAccount,
    private val noteDataSourceAdder: NoteDataSourceAdder,
    private val userDataSource: UserDataSource,
    private val userDTOEntityConverter: UserDTOEntityConverter,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
): ApResolverRepository {

    override suspend fun resolve(accountId: Long, uri: String): Result<ApResolver> = runCancellableCatching {
        withContext(ioDispatcher) {
            val account = getAccount.get(accountId)
            val result = apiProvider.get(account).resolve(ApResolveRequest(i = account.token, uri = uri))
                .throwIfHasError()
                .body()!!
            when(account.instanceType) {
                Account.InstanceType.MISSKEY -> {
                    when(result) {
                        is ApResolveResult.TypeNote -> {
                            val note = noteDataSourceAdder.addNoteDtoToDataSource(account, result.note)
                            ApResolver.TypeNote(
                                note
                            )
                        }

                        is ApResolveResult.TypeUser -> {
                            val user = userDTOEntityConverter.convert(account, result.user, true)
                            userDataSource.add(user)
                            ApResolver.TypeUser(
                                user
                            )
                        }
                    }
                }
                Account.InstanceType.MASTODON -> {
                    val body = mastodonAPIProvider.get(account).search(
                        q = uri,
                        resolve = true
                    ).throwIfHasError().body()
                    requireNotNull(body).let { res ->
                        val accounts = res.accounts.map {
                            it.toModel(account)
                        }
                        userDataSource.addAll(accounts)
                        val statuses = res.statuses.map {
                            noteDataSourceAdder.addTootStatusDtoIntoDataSource(account, it)
                        }
                        when(val aAccount = accounts.firstOrNull()) {
                            null -> {
                                ApResolver.TypeNote(statuses.first())
                            }
                            else -> {
                                ApResolver.TypeUser(aAccount)
                            }
                        }
                    }
                }
            }

        }

    }
}