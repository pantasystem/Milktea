package net.pantasystem.milktea.data.streaming.impl

import net.pantasystem.milktea.api.misskey.OkHttpClientProvider
import net.pantasystem.milktea.api_streaming.Socket
import net.pantasystem.milktea.api_streaming.network.MastodonSocketImpl
import net.pantasystem.milktea.api_streaming.network.SocketImpl
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.AccountRepository
import net.pantasystem.milktea.model.account.UnauthorizedException
import javax.inject.Inject
import net.pantasystem.milktea.data.streaming.SocketWithAccountProvider as ISocketWithAccountProvider

/**
 * SocketをAccountに基づきいい感じにリソースを取得できるようにする
 */
class SocketWithAccountProviderImpl @Inject constructor(
    val accountRepository: AccountRepository,
    val loggerFactory: Logger.Factory,
    val okHttpClientProvider: OkHttpClientProvider
) : ISocketWithAccountProvider{

    private val logger = loggerFactory.create("SocketProvider")

    private val accountIdWithSocket = mutableMapOf<Long, Socket>()

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

    override fun get(account: Account): Socket {
        synchronized(accountIdWithSocket) {
            var socket = accountIdWithSocket[account.accountId]
            if (socket != null) {
                // NOTE: tokenが異なる場合は再認証された可能性があるので、再生成を行う
                if (account.token == accountIdWithToken[account.accountId]) {
                    logger.debug("すでにインスタンス化済み")
                    return socket
                } else {
                    if (socket is SocketImpl) {
                        socket.destroy()

                    }
                }
            }

            var uri = account.normalizedInstanceDomain
            if(uri.startsWith("https")) {
                uri = "wss://" + account.getHost() + "/streaming"
            }
            try {
                val i = account.token
                uri = "${uri}?i=$i"

            }catch (e: UnauthorizedException) {
                logger.debug("未認証アカウント:id=${account.accountId}, baseURL=${account.instanceDomain}")
            }
            //logger.debug("url:$uri")

            socket = when(account.instanceType) {
                Account.InstanceType.MISSKEY -> SocketImpl(
                    url = uri,
                    okHttpClientProvider = okHttpClientProvider,
                    loggerFactory = loggerFactory,
                )
                Account.InstanceType.MASTODON -> MastodonSocketImpl()
            }
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