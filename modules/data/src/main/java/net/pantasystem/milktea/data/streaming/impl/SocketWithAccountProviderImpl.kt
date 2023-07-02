package net.pantasystem.milktea.data.streaming.impl

import net.pantasystem.milktea.api.misskey.OkHttpClientProvider
import net.pantasystem.milktea.api_streaming.Socket
import net.pantasystem.milktea.api_streaming.network.SocketImpl
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.UnauthorizedException
import net.pantasystem.milktea.model.instance.Version
import net.pantasystem.milktea.model.nodeinfo.NodeInfo
import net.pantasystem.milktea.model.nodeinfo.NodeInfoRepository
import net.pantasystem.milktea.model.nodeinfo.getVersion
import javax.inject.Inject
import net.pantasystem.milktea.data.streaming.SocketWithAccountProvider as ISocketWithAccountProvider

/**
 * SocketをAccountに基づきいい感じにリソースを取得できるようにする
 */
class SocketWithAccountProviderImpl @Inject constructor(
    val loggerFactory: Logger.Factory,
    val okHttpClientProvider: OkHttpClientProvider,
    val nodeInfoRepository: NodeInfoRepository,
) : ISocketWithAccountProvider{

    private val logger = loggerFactory.create("SocketProvider")

    private val accountIdWithSocket = mutableMapOf<Long, SocketImpl>()

    /**
     * accountIdとそのTokenを管理している。
     * ここにTokenを入れておいてTokenが更新されていないかをチェックする
     */
    private val accountIdWithToken = mutableMapOf<Long, String>()

    override fun get(accountId: Long): Socket? {
        synchronized(accountIdWithSocket) {
            return accountIdWithSocket[accountId]
        }
    }

    override fun get(account: Account): Socket? {
        if (account.instanceType == Account.InstanceType.MASTODON) {
            return null
        }
        val isRequirePingPong = nodeInfoRepository.get(account.getHost())?.let {
            !(it.type is NodeInfo.SoftwareType.Misskey.Normal && it.type.getVersion() >= Version("13.13.2"))
        }
        synchronized(accountIdWithSocket) {
            var socket = accountIdWithSocket[account.accountId]
            if (socket != null) {
                // NOTE: tokenが異なる場合は再認証された可能性があるので、再生成を行う
                if (account.token == accountIdWithToken[account.accountId]) {
                    logger.debug { "すでにインスタンス化済み" }
                    socket.isRequirePingPong = isRequirePingPong ?: true
                    return socket
                } else {
                    socket.destroy()
                }
            }

            var uri = "wss://" + account.getHost() + "/streaming"
            try {
                val i = account.token
                uri = "${uri}?i=$i"

            }catch (e: UnauthorizedException) {
                logger.debug { "未認証アカウント:id=${account.accountId}, baseURL=${account.instanceDomain}" }
            }
            //logger.debug("url:$uri")

            socket = SocketImpl(
                url = uri,
                isRequirePingPong = isRequirePingPong ?: true,
                okHttpClientProvider = okHttpClientProvider,
                loggerFactory = loggerFactory,
            )
            accountIdWithSocket[account.accountId] = socket
            accountIdWithToken[account.accountId] = account.token

            return socket
        }



    }

    override fun all(): List<Socket> {
        synchronized(accountIdWithSocket) {
            return accountIdWithSocket.values.toList()
        }
    }
}