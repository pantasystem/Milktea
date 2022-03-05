package jp.panta.misskeyandroidclient.model.notes.reaction.impl

import jp.panta.misskeyandroidclient.api.misskey.MisskeyAPIProvider
import jp.panta.misskeyandroidclient.api.misskey.notes.reaction.RequestReactionHistoryDTO
import jp.panta.misskeyandroidclient.api.misskey.throwIfHasError
import jp.panta.misskeyandroidclient.api.misskey.users.toUser
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionHistory
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionHistoryDataSource
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionHistoryPaginator
import jp.panta.misskeyandroidclient.model.notes.reaction.ReactionHistoryRequest
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

    val limit: Int = 20

    val lock = Mutex()

    private var offset: Int = 0

    override suspend fun next(): Boolean {
        lock.withLock {
            val account = accountRepository.get(reactionHistoryRequest.noteId.accountId)
            val misskeyAPI = misskeyAPIProvider.get(account.instanceDomain)
            val res = misskeyAPI.reactions(
                RequestReactionHistoryDTO(
                    i = account.getI(encryption),
                    offset = offset,
                    limit = limit,
                    noteId = reactionHistoryRequest.noteId.noteId,
                    type = reactionHistoryRequest.type
                )
            ).throwIfHasError().body()?: emptyList()

            if(res.isNotEmpty()) {
                offset += res.size
            }
            val reactionHistories = res.map {
                val user = it.user.toUser(account)
                userDataSource.add(user)
                ReactionHistory(
                    ReactionHistory.Id(it.id, account.accountId),
                    reactionHistoryRequest.noteId,
                    it.createdAt,
                    user,
                    it.type
                )
            }
            reactionHistoryDataSource.addAll(reactionHistories)
            return reactionHistories.isNotEmpty()
        }
    }
}