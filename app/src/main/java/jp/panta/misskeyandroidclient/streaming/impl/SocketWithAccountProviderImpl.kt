package jp.panta.misskeyandroidclient.streaming.impl

import jp.panta.misskeyandroidclient.Logger
import jp.panta.misskeyandroidclient.model.Encryption
import jp.panta.misskeyandroidclient.model.UnauthorizedException
import jp.panta.misskeyandroidclient.model.account.Account
import jp.panta.misskeyandroidclient.model.account.AccountRepository
import jp.panta.misskeyandroidclient.streaming.BeforeConnectListener
import jp.panta.misskeyandroidclient.streaming.Socket
import jp.panta.misskeyandroidclient.streaming.network.SocketImpl
import okhttp3.OkHttpClient
import jp.panta.misskeyandroidclient.streaming.SocketWithAccountProvider as ISocketWithAccountProvider

/**
 * SocketをAccountに基づきいい感じにリソースを取得できるようにする
 */
class SocketWithAccountProviderImpl(
    val encryption: Encryption,
    val accountRepository: AccountRepository,
    val loggerFactory: Logger.Factory,
    val instanceCreatedListener: (account: Account, socket: Socket)-> Unit,
    val beforeConnectListener:(account: Account, socket: Socket)-> Boolean,
    val okHttpClient: OkHttpClient = OkHttpClient()
) : ISocketWithAccountProvider{

    private val logger = loggerFactory.create("SocketProvider")

    private val accountIdWithSocket = mutableMapOf<Long, Socket>()

    override fun get(accountId: Long): Socket? {
        synchronized(accountIdWithSocket) {
            return accountIdWithSocket[accountId]
        }
    }

    override fun get(account: Account): Socket {
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

            }catch (e: UnauthorizedException) {
                logger.debug("未認証アカウント:id=${account.accountId}, baseURL=${account.instanceDomain}")
            }
            logger.debug("url:$uri")

            socket = SocketImpl(
                url = uri,
                okHttpClient,
                {
                    beforeConnectListener.invoke(account, it)
                },
                loggerFactory,
            )
            accountIdWithSocket[account.accountId] = socket

            runCatching {
                instanceCreatedListener.invoke(account, socket)
            }.onFailure {
                logger.error("instanceCreatedListener.invoke error", e = it)
            }

            return socket
        }



    }

    override fun all(): List<Socket> {
        synchronized(accountIdWithSocket) {
            return accountIdWithSocket.values.toList()
        }
    }
}