package net.pantasystem.milktea.data.infrastructure.emoji

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.pantasystem.milktea.api_streaming.*
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.data.streaming.SocketWithAccountProvider
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.emoji.CustomEmojiRepository
import net.pantasystem.milktea.model.emoji.EmojiEventHandler
import net.pantasystem.milktea.model.instance.SyncMetaExecutor
import javax.inject.Inject

class EmojiEventHandlerImpl @Inject constructor(
    private val socketProvider: SocketWithAccountProvider,
    private val executor: SyncMetaExecutor,
    private val customEmojiRepository: CustomEmojiRepository,
    private val loggerFactory: Logger.Factory
) : EmojiEventHandler, SocketMessageEventListener {

    private var currentAccount: Account? = null
    private val coroutineScope = CoroutineScope(SupervisorJob())
    private val logger by lazy {
        loggerFactory.create("EmojiEventHandlerImpl")
    }

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
                    executor.invoke(requireNotNull(currentAccount).normalizedInstanceUri)
                }
                true
            }
            is EmojiUpdated -> {
                if (currentAccount != null) {
                    coroutineScope.launch {
                        customEmojiRepository.addEmojis(
                            requireNotNull(currentAccount?.getHost()),
                            e.body.emojis
                        ).onFailure {
                            logger.error("addEmojis failed", it)
                        }
                    }
                }
               true
            }
            is EmojiDeleted -> {
                if (currentAccount != null) {
                    coroutineScope.launch {
                        customEmojiRepository.deleteEmojis(
                            requireNotNull(currentAccount?.getHost()),
                            e.body.emojis
                        ).onFailure {
                            logger.error("deleteEmojis failed", it)
                        }
                    }
                }
                true
            }
            else -> {
                false
            }
        }
    }
}