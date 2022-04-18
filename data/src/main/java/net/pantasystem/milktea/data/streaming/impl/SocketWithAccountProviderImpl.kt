package net.pantasystem.milktea.data.streaming.impl

import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.Encryption
import net.pantasystem.milktea.data.streaming.Socket
import net.pantasystem.milktea.data.streaming.network.SocketImpl
import okhttp3.OkHttpClient
import javax.inject.Inject
import net.pantasystem.milktea.data.streaming.SocketWithAccountProvider as ISocketWithAccountProvider

/**
 * SocketをAccountに基づきいい感じにリソースを取得できるようにする
 */
class SocketWithAccountProviderImpl @Inject constructor(
    val encryption: Encryption,
    val accountRepository: net.pantasystem.milktea.model.account.AccountRepository,
    val loggerFactory: Logger.Factory,
) : ISocketWithAccountProvider{
    val okHttpClient: OkHttpClient = OkHttpClient()

    private val logger = loggerFactory.create("SocketProvider")

    private val accountIdWithSocket = mutableMapOf<Long, Socket>()

    override fun get(accountId: Long): Socket? {
        synchronized(accountIdWithSocket) {
            return accountIdWithSocket[accountId]
        }
    }

    override fun get(account: net.pantasystem.milktea.model.account.Account): Socket {
        synchronized(accountIdWithSocket) {
            var socket = accountIdWithSocket[account.accountId]
            if(socket != null){
                logger.debug("すでにインスタンス化済み")
                return socket
            }


            var uri = account.instanceDomain
            if(uri.startsWith("https")) {
                uri = "wss" + uri.substring(5, uri.length) + "/streaming"
            }
            try {
                val i = account.getI(encryption)
                uri = "${uri}?i=$i"

            }catch (e: net.pantasystem.milktea.model.account.UnauthorizedException) {
                logger.debug("未認証アカウント:id=${account.accountId}, baseURL=${account.instanceDomain}")
            }
            //logger.debug("url:$uri")

            socket = SocketImpl(
                url = uri,
                okHttpClient,
                loggerFactory,
            )
            accountIdWithSocket[account.accountId] = socket

            return socket
        }



    }

    override fun all(): List<Socket> {
        synchronized(accountIdWithSocket) {
            return accountIdWithSocket.values.toList()
        }
    }
}