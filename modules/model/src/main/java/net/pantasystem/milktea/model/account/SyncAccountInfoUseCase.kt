package net.pantasystem.milktea.model.account

import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import net.pantasystem.milktea.model.nodeinfo.NodeInfoRepository
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.model.user.UserRepository
import javax.inject.Inject

class SyncAccountInfoUseCase @Inject constructor(
    private val nodeInfoRepository: NodeInfoRepository,
    private val userRepository: UserRepository,
    private val accountRepository: AccountRepository,
) {

    suspend operator fun invoke(account: Account): Result<Unit> = runCancellableCatching {
        val nodeInfo = nodeInfoRepository.find(account.getHost()).getOrThrow()
        val user = userRepository.find(User.Id(account.accountId, account.remoteId))
        val remoteSoftwareType = when (nodeInfo.type) {
            is NodeInfo.SoftwareType.Firefish -> Account.InstanceType.FIREFISH
            is NodeInfo.SoftwareType.Mastodon -> Account.InstanceType.MASTODON
            is NodeInfo.SoftwareType.Misskey -> Account.InstanceType.MISSKEY
            is NodeInfo.SoftwareType.Pleroma -> Account.InstanceType.PLEROMA
            is NodeInfo.SoftwareType.Other -> throw IllegalStateException("unknown type of software:${nodeInfo.type}")
        }
        if (account.instanceType != remoteSoftwareType || user.userName != account.userName) {
            accountRepository.add(
                account.copy(
                    instanceType = remoteSoftwareType,
                    userName = user.userName
                ), false
            )
        }
    }
}