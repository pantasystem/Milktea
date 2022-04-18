package net.pantasystem.milktea.data.infrastructure.group.impl


import net.pantasystem.milktea.api.misskey.groups.*
import net.pantasystem.milktea.api.misskey.throwIfHasError
import net.pantasystem.milktea.api.misskey.v11.MisskeyAPIV11
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.data.api.misskey.MisskeyAPIProvider
import net.pantasystem.milktea.api.misskey.I
import net.pantasystem.milktea.model.instance.IllegalVersionException
import net.pantasystem.milktea.data.infrastructure.toGroup
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.group.*
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    private val misskeyAPIProvider: MisskeyAPIProvider,
    private val accountRepository: AccountRepository,
    private val groupDataSource: GroupDataSource,
    private val encryption: Encryption,
    private val loggerFactory: Logger.Factory
) : GroupRepository {

    private val logger: Logger by lazy {
        loggerFactory.create("GroupRepositoryImpl")
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun create(createGroup: CreateGroup): Group {
        val account = accountRepository.get(createGroup.author)
        val api = getMisskeyAPI(account)

        val res = api.createGroup(CreateGroupDTO(i = account.getI(encryption), name = createGroup.name)).throwIfHasError()
        val group = res.body()?.toGroup(account.accountId)
        require(group != null)
        groupDataSource.add(group)
        return group
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun find(groupId: Group.Id): Group {
        var group = runCatching {
            groupDataSource.find(groupId)
        }.onFailure {
            logger.debug("ローカルには存在しません。:${groupId}")
        }.getOrNull()

        if(group != null) {
            return group
        }
        val account = accountRepository.get(groupId.accountId)
        val api = getMisskeyAPI(account)

        val res = api.showGroup(ShowGroupDTO(account.getI(encryption), groupId = groupId.groupId)).throwIfHasError()
        val body = res.body()
            ?: throw GroupNotFoundException(groupId)
        group = body.toGroup(account.accountId)
        groupDataSource.add(group)

        return group
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun joined(accountId: Long): List<Group> {
        val account = accountRepository.get(accountId)
        val api = getMisskeyAPI(account).joinedGroups(I(account.getI(encryption))).throwIfHasError()
        val groups = api.body()?.map {
            it.toGroup(account.accountId)
        }?: emptyList()
        groupDataSource.addAll(groups)
        return groups
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun owned(accountId: Long): List<Group> {
        val account = accountRepository.get(accountId)
        val api = getMisskeyAPI(account).ownedGroups(I(account.getI(encryption))).throwIfHasError()
        val groups = api.body()?.map {
            it.toGroup(account.accountId)
        }?: emptyList()
        groupDataSource.addAll(groups)
        return groups
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun pull(pull: Pull): Group {
        var group = find(pull.groupId)
        val account = accountRepository.get(pull.groupId.accountId)
        getMisskeyAPI(account).pullUser(RemoveUserDTO(i = account.getI(encryption), userId = pull.userId.id, groupId = pull.groupId.groupId))
            .throwIfHasError()

        group = group.copy( userIds = group.userIds.filterNot {
            pull.userId == pull.userId
        })
        groupDataSource.add(group)
        return group
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun transfer(transfer: Transfer): Group {
        val account = accountRepository.get(transfer.groupId.accountId)
        val body = getMisskeyAPI(account).transferGroup(
            TransferGroupDTO(
            i = account.getI(encryption),
            groupId = transfer.groupId.groupId,
            userId = transfer.userId.id
        )
        ).throwIfHasError().body()

        require(body != null)
        return body.toGroup(account.accountId).also {
            groupDataSource.add(it)
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun update(updateGroup: UpdateGroup): Group {
        val account = accountRepository.get(updateGroup.groupId.accountId)
        val body = getMisskeyAPI(account).updateGroup(UpdateGroupDTO(i = account.getI(encryption), groupId = updateGroup.groupId.groupId, name = updateGroup.name))
            .throwIfHasError().body()
        require(body != null)

        val group = body.toGroup(account.accountId)
        groupDataSource.add(group)
        return group
    }

    private fun getMisskeyAPI(account: Account): MisskeyAPIV11 {
        return misskeyAPIProvider.get(account.instanceDomain) as? MisskeyAPIV11
            ?: throw IllegalVersionException()
    }
}