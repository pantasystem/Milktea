package net.pantasystem.milktea.data.infrastructure.emoji

import net.pantasystem.milktea.api_streaming.EmojiAdded
import net.pantasystem.milktea.api_streaming.SocketMessageEventListener
import net.pantasystem.milktea.api_streaming.StreamingEvent
import net.pantasystem.milktea.data.streaming.SocketWithAccountProvider
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.emoji.EmojiEventHandler
import net.pantasystem.milktea.model.instance.SyncMetaExecutor
import javax.inject.Inject

class EmojiEventHandlerImpl @Inject constructor(
    private val socketProvider: SocketWithAccountProvider,
    private val executor: SyncMetaExecutor
) : EmojiEventHandler, SocketMessageEventListener {

    private var currentAccount: Account? = null

    override fun observe(account: Account?) {
        synchronized(this) {
            currentAccount?.let {
                socketProvider.get(it)
            }?.removeMessageEventListener(this)
            if (account != null) {
                socketProvider.get(account.accountId)?.addMessageEventListener(this)
            }
            currentAccount = account

        }
    }

    override fun onMessage(e: StreamingEvent): Boolean {
        return when(e) {
            is EmojiAdded -> {
                if (currentAccount != null) {
                    executor.invoke(requireNotNull(currentAccount).normalizedInstanceDomain)
                }
                true
            }
            else -> {
                false
            }
        }
    }
}