package net.pantasystem.milktea.data.infrastructure.notes.reaction.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.api.misskey.notes.reaction.RequestReactionHistoryDTO
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.converters.UserDTOEntityConverter
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.notes.reaction.ReactionHistory
import net.pantasystem.milktea.model.notes.reaction.ReactionHistoryDataSource
import net.pantasystem.milktea.model.notes.reaction.ReactionHistoryPaginator
import net.pantasystem.milktea.model.notes.reaction.ReactionHistoryRequest
import net.pantasystem.milktea.model.user.UserDataSource
import java.util.*
import javax.inject.Inject

class ReactionHistoryPaginatorImpl(
    override val reactionHistoryRequest: ReactionHistoryRequest,
    private val reactionHistoryDataSource: ReactionHistoryDataSource,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val accountRepository: AccountRepository,
    private val userDataSource: UserDataSource,
    private val userDTOEntityConverter: UserDTOEntityConverter,
) : ReactionHistoryPaginator {

    class Factory @Inject constructor(
        private val reactionHistoryDataSource: ReactionHistoryDataSource,
        private val misskeyAPIProvider: MisskeyAPIProvider,
        private val accountRepository: AccountRepository,
        private val userDataSource: UserDataSource,
        private val userDTOEntityConverter: UserDTOEntityConverter,
    ) : ReactionHistoryPaginator.Factory {
        override fun create(reactionHistoryRequest: ReactionHistoryRequest) : ReactionHistoryPaginator {
            return ReactionHistoryPaginatorImpl(
                reactionHistoryRequest,
                reactionHistoryDataSource,
                misskeyAPIProvider,
                accountRepository,
                userDataSource,
                userDTOEntityConverter,
            )
        }
    }

    val limit: Int = 20

    val lock = Mutex()

    private var offset: Int = 0

    override suspend fun next(): Boolean {
        return withContext(Dispatchers.IO) {
            lock.withLock {

                val account = accountRepository.get(reactionHistoryRequest.noteId.accountId).getOrThrow()
                val misskeyAPI = misskeyAPIProvider.get(account.normalizedInstanceUri)
                val res = misskeyAPI.reactions(
                    RequestReactionHistoryDTO(
                        i = account.token,
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
                    val user = userDTOEntityConverter.convert(account, it.user)
                    userDataSource.add(user)
                    ReactionHistory(
                        ReactionHistory.Id(it.id, account.accountId),
                        reactionHistoryRequest.noteId,
                        Date(it.createdAt.toEpochMilliseconds()),
                        user,
                        it.type
                    )
                }
                reactionHistoryDataSource.addAll(reactionHistories)
                reactionHistories.isNotEmpty()
            }
        }

    }
}