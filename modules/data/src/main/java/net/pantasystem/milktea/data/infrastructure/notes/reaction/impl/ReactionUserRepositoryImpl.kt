package net.pantasystem.milktea.data.infrastructure.notes.reaction.impl

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import net.pantasystem.milktea.api.misskey.notes.reaction.ReactionHistoryDTO
import net.pantasystem.milktea.api.misskey.notes.reaction.RequestReactionHistoryDTO
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.data.api.mastodon.MastodonAPIProvider
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.converters.UserDTOEntityConverter
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.reaction.ReactionUserRepository
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserDataSource
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject

class ReactionUserRepositoryImpl @Inject constructor(
    private val accountRepository: AccountRepository,
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val mastodonAPIProvider: MastodonAPIProvider,
    private val userRepository: UserRepository,
    private val userDataSource: UserDataSource,
    private val userDTOEntityConverter: UserDTOEntityConverter,
    private val dao: ReactionUserDAO,
) : ReactionUserRepository {

    override suspend fun syncBy(noteId: Note.Id, reaction: String?): Result<Unit> =
        runCancellableCatching {
            val account = accountRepository.get(noteId.accountId).getOrThrow()
            dao.remove(noteId, reaction)
            dao.createEmptyIfNotExists(noteId, reaction)
            when (account.instanceType) {
                Account.InstanceType.MISSKEY, Account.InstanceType.FIREFISH -> {
                    var reactions: List<ReactionHistoryDTO>
                    var offset = 0
                    do {
                        reactions = requireNotNull(
                            misskeyAPIProvider.get(account).reactions(
                                RequestReactionHistoryDTO(
                                    i = account.token,
                                    noteId = noteId.noteId,
                                    type = reaction,
                                    offset = offset,
                                    limit = 10
                                )
                            ).throwIfHasError().body()
                        )
                        offset += reactions.size
                        dao.appendAccountIds(noteId, reaction, reactions.map { it.user.id })
                        userDataSource.addAll(reactions.map {
                            userDTOEntityConverter.convert(
                                account,
                                it.user
                            )
                        })
                    } while (reactions.size >= 10)

                }
                Account.InstanceType.MASTODON, Account.InstanceType.PLEROMA -> {
                    val resBody = requireNotNull(
                        mastodonAPIProvider.get(account).getStatus(noteId.noteId)
                            .throwIfHasError()
                            .body()
                    )
                    val emojiReaction = resBody.emojiReactions?.firstOrNull {
                        it.reaction == reaction
                    }
                    val accountIds = if (reaction == null) {
                        resBody.emojiReactions?.map {
                            it.accountIds
                        }?.flatten()
                    } else {
                        emojiReaction?.accountIds
                    } ?: emptyList()

                    userRepository.syncIn(accountIds.map {
                        User.Id(account.accountId, it)
                    })
                    dao.update(noteId, reaction, accountIds)
                }

            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeBy(noteId: Note.Id, reaction: String?): Flow<List<User>> {
        return flow {
            dao.createEmptyIfNotExists(noteId, reaction)
            emit(accountRepository.get(noteId.accountId).getOrThrow())
        }.flatMapLatest { account ->
            dao.observeBy(noteId, reaction).filterNotNull().flatMapLatest {
                userDataSource.observeIn(account.accountId, it.accountIds)
            }
        }
    }

    override suspend fun findBy(noteId: Note.Id, reaction: String?): Result<List<User>> =
        runCancellableCatching {
            val accountIds = dao.findBy(noteId, reaction)?.accountIds ?: emptyList()
            userDataSource.getIn(noteId.accountId, accountIds).getOrThrow()
        }

}