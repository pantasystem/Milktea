package net.pantasystem.milktea.data.infrastructure.user.renote.mute

import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.account.GetAccount
import net.pantasystem.milktea.model.instance.Version
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import net.pantasystem.milktea.model.nodeinfo.NodeInfoRepository
import net.pantasystem.milktea.model.nodeinfo.getVersion
import javax.inject.Inject

interface IsSupportRenoteMuteInstance {
    suspend operator fun invoke(accountId: Long): Boolean
}

class IsSupportRenoteMuteInstanceImpl @Inject constructor(
    private val getAccount: GetAccount,
    private val nodeInfoRepository: NodeInfoRepository,
    private val loggerFactory: Logger.Factory,
): IsSupportRenoteMuteInstance {

    private val logger by lazy {
        loggerFactory.create("IsSupportRenoteMuteInstance")
    }
    override suspend fun invoke(accountId: Long): Boolean {
        return runCancellableCatching {
            val account = getAccount.get(accountId)
            val nodeInfo = nodeInfoRepository.find(account.getHost()).getOrThrow()
            nodeInfo.type is NodeInfo.SoftwareType.Misskey.Normal
                    && nodeInfo.type.getVersion() >= Version("13.10.0")
        }.onFailure {
            logger.error("Failed to check support renote mute instance", it)
        }.getOrElse {
            false
        }
    }
}