package jp.panta.misskeyandroidclient.model.reaction.impl

import jp.panta.misskeyandroidclient.api.MisskeyAPIProvider
import jp.panta.misskeyandroidclient.api.reaction.RequestReactionHistoryDTO
import jp.panta.misskeyandroidclient.api.throwIfHasError
import jp.panta.misskeyandroidclient.api.users.toUser
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.model.reaction.ReactionHistory
import jp.panta.misskeyandroidclient.model.reaction.ReactionHistoryDataSource
import jp.panta.misskeyandroidclient.model.reaction.ReactionHistoryPaginator
import jp.panta.misskeyandroidclient.model.reaction.ReactionHistoryRequest
import jp.panta.misskeyandroidclient.model.users.UserDataSource
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ReactionHistoryPaginatorImpl(
    override val reactionHistoryRequest: ReactionHistoryRequest,
    private val reactionHistoryDataSource: ReactionHistoryDataSource,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val accountRepository: AccountRepository,
    private val encryption: Encryption,
    private val userDataSource: UserDataSource
) : ReactionHistoryPaginator {

    class Factory(
        private val reactionHistoryDataSource: ReactionHistoryDataSource,
        private val misskeyAPIProvider: MisskeyAPIProvider,
        private val accountRepository: AccountRepository,
        private val encryption: Encryption,
        private val userDataSource: UserDataSource
    ) : ReactionHistoryPaginator.Factory {
        override fun create(reactionHistoryRequest: ReactionHistoryRequest) : ReactionHistoryPaginator {
            return ReactionHistoryPaginatorImpl(
                reactionHistoryRequest,
                reactionHistoryDataSource,
                misskeyAPIProvider,
                accountRepository,
                encryption,
                userDataSource
            )
        }
    }

    val lock = Mutex()

    private var sinceId: ReactionHistory.Id? = null
    private var untilId: ReactionHistory.Id? = null

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun loadFuture() {
        lock.withLock {
            val account: Account = accountRepository.get(reactionHistoryRequest.noteId.accountId)
            val misskeyAPI = misskeyAPIProvider.get(account.instanceDomain)
            val body = misskeyAPI.reactions(
                RequestReactionHistoryDTO(
                    sinceId= sinceId?.reactionId,
                    i = account.getI(encryption),
                    type = reactionHistoryRequest.type,
                    noteId = reactionHistoryRequest.noteId.noteId
                )
            ).execute().throwIfHasError().body()?: emptyList()
            val histories = body.map {
                val user = it.user.toUser(account, false)
                userDataSource.add(user)
                ReactionHistory(
                    ReactionHistory.Id(it.id, reactionHistoryRequest.noteId.accountId),
                    reactionHistoryRequest.noteId,
                    it.createdAt,
                    user,
                    it.type
                )
            }
            reactionHistoryDataSource.addAll(histories)

            histories.lastOrNull()?.let {
                untilId = it.id
            }

            if(sinceId == null) {
                sinceId = histories.firstOrNull()?.id
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun loadPast() {
        lock.withLock {
            val account: Account = accountRepository.get(reactionHistoryRequest.noteId.accountId)
            val misskeyAPI = misskeyAPIProvider.get(account.instanceDomain)
            val body = misskeyAPI.reactions(
                RequestReactionHistoryDTO(
                    untilId = untilId?.reactionId,
                    i = account.getI(encryption),
                    type = reactionHistoryRequest.type,
                    noteId = reactionHistoryRequest.noteId.noteId
                )
            ).execute().throwIfHasError().body()?: emptyList()
            val histories = body.map {
                val user = it.user.toUser(account, false)
                userDataSource.add(user)
                ReactionHistory(
                    ReactionHistory.Id(it.id, reactionHistoryRequest.noteId.accountId),
                    reactionHistoryRequest.noteId,
                    it.createdAt,
                    user,
                    it.type
                )
            }
            reactionHistoryDataSource.addAll(histories)

            histories.lastOrNull()?.let {
                untilId = it.id
            }

            if(sinceId == null) {
                sinceId = histories.firstOrNull()?.id
            }
        }
    }
}