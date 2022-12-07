package net.pantasystem.milktea.data.infrastructure.ap

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.api.misskey.ap.ApResolveRequest
import net.pantasystem.milktea.api.misskey.ap.ApResolveResult
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.notes.NoteDataSourceAdder
import net.pantasystem.milktea.data.infrastructure.toUser
import net.pantasystem.milktea.model.account.GetAccount
import net.pantasystem.milktea.model.ap.ApResolver
import net.pantasystem.milktea.model.ap.ApResolverRepository
import net.pantasystem.milktea.model.user.UserDataSource
import javax.inject.Inject

class ApResolverRepositoryImpl @Inject constructor(
    private val apiProvider: MisskeyAPIProvider,
    private val getAccount: GetAccount,
    private val noteDataSourceAdder: NoteDataSourceAdder,
    private val userDataSource: UserDataSource,
): ApResolverRepository {

    override suspend fun resolve(accountId: Long, uri: String): Result<ApResolver> = runCatching {
        withContext(Dispatchers.IO) {
            val account = getAccount.get(accountId)
            val result = apiProvider.get(account).resolve(ApResolveRequest(i = account.token, uri = uri))
                .throwIfHasError()
                .body()!!
            when(result) {
                is ApResolveResult.TypeNote -> {
                    val note = noteDataSourceAdder.addNoteDtoToDataSource(account, result.note)
                    ApResolver.TypeNote(
                        note
                    )
                }

                is ApResolveResult.TypeUser -> {
                    val user = result.user.toUser(account, isDetail = true)
                    userDataSource.add(user)
                    ApResolver.TypeUser(
                        user
                    )
                }
            }
        }

    }
}