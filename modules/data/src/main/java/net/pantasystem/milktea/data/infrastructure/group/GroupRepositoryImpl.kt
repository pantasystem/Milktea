package net.pantasystem.milktea.data.infrastructure.group


import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.api.misskey.I
import net.pantasystem.milktea.api.misskey.groups.*
import net.pantasystem.milktea.api.misskey.v11.MisskeyAPIV11
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common.throwIfHasError
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.data.infrastructure.toGroup
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.group.*
import net.pantasystem.milktea.model.instance.IllegalVersionException
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val accountRepository: AccountRepository,
    private val groupDataSource: GroupDataSource,
    private val loggerFactory: Logger.Factory,
    private val userRepository: UserRepository,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : GroupRepository {

    private val logger: Logger by lazy {
        loggerFactory.create("GroupRepositoryImpl")
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun create(createGroup: CreateGroup): Group {
        return withContext(ioDispatcher) {
            val account = accountRepository.get(createGroup.author).getOrThrow()
            val api = getMisskeyAPI(account)

            val res = api.createGroup(
                CreateGroupDTO(
                    i = account.token,
                    name = createGroup.name
                )
            ).throwIfHasError()
            val group = res.body()?.toGroup(account.accountId)
            require(group != null)
            groupDataSource.add(group)
            group
        }
    }

    override suspend fun syncOne(groupId: Group.Id): Group {
        return withContext(ioDispatcher) {
            var group = groupDataSource.find(groupId).onFailure {
                logger.debug("ローカルには存在しません。:${groupId}")
            }.getOrNull()

            if (group != null) {
                return@withContext group
            }
            val account = accountRepository.get(groupId.accountId).getOrThrow()
            val api = getMisskeyAPI(account)

            val res =
                api.showGroup(ShowGroupDTO(account.token, groupId = groupId.groupId))
                    .throwIfHasError()
            val body = res.body()
                ?: throw GroupNotFoundException(groupId)
            group = body.toGroup(account.accountId)
            groupDataSource.add(group)
            userRepository.syncIn(group.userIds).getOrThrow()

            group
        }

    }

    override suspend fun syncByJoined(accountId: Long): List<Group> {
        return withContext(ioDispatcher) {
            val account = accountRepository.get(accountId).getOrThrow()
            val api =
                getMisskeyAPI(account).joinedGroups(I(account.token)).throwIfHasError()
            val groups = api.body()?.map {
                it.toGroup(account.accountId)
            } ?: emptyList()
            groupDataSource.addAll(groups)
            val userIds = groups.map {
                it.userIds
            }.flatten()
            userRepository.syncIn(userIds).getOrThrow()
            groups
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun syncByOwned(accountId: Long): List<Group> {
        return withContext(ioDispatcher) {
            val account = accountRepository.get(accountId).getOrThrow()
            val api =
                getMisskeyAPI(account).ownedGroups(I(account.token)).throwIfHasError()
            val groups = api.body()?.map {
                it.toGroup(account.accountId)
            } ?: emptyList()
            groupDataSource.addAll(groups)
            val userIds = groups.map {
                it.userIds
            }.flatten()
            userRepository.syncIn(userIds).getOrThrow()
            groups
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun pull(pull: Pull): Group {
        return withContext(ioDispatcher) {
            var group = syncOne(pull.groupId)
            val account = accountRepository.get(pull.groupId.accountId).getOrThrow()
            getMisskeyAPI(account).pullUser(
                RemoveUserDTO(
                    i = account.token,
                    userId = pull.userId.id,
                    groupId = pull.groupId.groupId
                )
            )
                .throwIfHasError()

            group = group.copy(userIds = group.userIds.filterNot {
                it == pull.userId
            })
            groupDataSource.add(group)
            group
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun transfer(transfer: Transfer): Group {
        return withContext(ioDispatcher) {
            val account = accountRepository.get(transfer.groupId.accountId).getOrThrow()
            val body = getMisskeyAPI(account).transferGroup(
                TransferGroupDTO(
                    i = account.token,
                    groupId = transfer.groupId.groupId,
                    userId = transfer.userId.id
                )
            ).throwIfHasError().body()

            require(body != null)
            body.toGroup(account.accountId).also {
                groupDataSource.add(it)
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun update(updateGroup: UpdateGroup): Group {
        return withContext(ioDispatcher) {
            val account = accountRepository.get(updateGroup.groupId.accountId).getOrThrow()
            val body = getMisskeyAPI(account).updateGroup(
                UpdateGroupDTO(
                    i = account.token,
                    groupId = updateGroup.groupId.groupId,
                    name = updateGroup.name
                )
            )
                .throwIfHasError().body()
            require(body != null)

            val group = body.toGroup(account.accountId)
            groupDataSource.add(group)
            group
        }
    }

    override suspend fun invite(invite: Invite): Result<Unit> = runCancellableCatching<Unit> {
        return@runCancellableCatching withContext(ioDispatcher) {
            val account = accountRepository.get(invite.groupId.accountId).getOrThrow()
            getMisskeyAPI(account).invite(
                InviteUserDTO(
                    groupId = invite.groupId.groupId,
                    i = account.token,
                    userId = invite.userId.id,
                )
            ).throwIfHasError()
        }
    }

    override suspend fun accept(invitationId: InvitationId): Result<Unit> = runCancellableCatching {
        return@runCancellableCatching withContext(ioDispatcher) {
            val account = accountRepository.get(invitationId.accountId).getOrThrow()
            getMisskeyAPI(account).acceptInvitation(
                AcceptInvitationDTO(
                    i = account.token,
                    invitationId = invitationId.invitationId
                )
            ).throwIfHasError()
        }
    }

    override suspend fun reject(invitationId: InvitationId): Result<Unit> = runCancellableCatching {
        return@runCancellableCatching withContext(ioDispatcher) {
            val account = accountRepository.get(invitationId.accountId).getOrThrow()
            getMisskeyAPI(account).rejectInvitation(
                RejectInvitationDTO(
                    i = account.token,
                    invitationId = invitationId.invitationId
                )
            ).throwIfHasError()
        }
    }

    private fun getMisskeyAPI(account: Account): MisskeyAPIV11 {
        return misskeyAPIProvider.get(account.normalizedInstanceDomain) as? MisskeyAPIV11
            ?: throw IllegalVersionException()
    }
}