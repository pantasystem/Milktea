package net.pantasystem.milktea.model.emoji

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.pantasystem.milktea.common.Logger
import javax.inject.Inject

class EmojiImageCacheStore @Inject constructor(
    coroutineScope: CoroutineScope,
    private val saveCustomEmojiImageUseCase: SaveCustomEmojiImageUseCase,
    private val loggerFactory: Logger.Factory,
) {

    private val logger by lazy {
        loggerFactory.create("EICStore")
    }

    private val queue = MutableSharedFlow<CustomEmoji>(extraBufferCapacity = 25)

    init {
        queue.onEach { emoji ->
            try {
                saveCustomEmojiImageUseCase(emoji).getOrThrow()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.error("save failure", e)
            }
        }.launchIn(coroutineScope)
    }
    fun save(emoji: CustomEmoji) {
        queue.tryEmit(emoji)
    }
}