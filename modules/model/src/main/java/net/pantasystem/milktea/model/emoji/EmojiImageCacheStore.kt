package net.pantasystem.milktea.model.emoji

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.model.image.ImageCacheRepository
import javax.inject.Inject

class EmojiImageCacheStore @Inject constructor(
    coroutineScope: CoroutineScope,
    private val imageCacheRepository: ImageCacheRepository,
    private val loggerFactory: Logger.Factory,
) {

    private val logger by lazy {
        loggerFactory.create("EICStore")
    }

    private val queue = MutableSharedFlow<Emoji>(extraBufferCapacity = 100)

    init {
        queue.mapNotNull { emoji ->
            emoji.url ?: emoji.uri
        }.onEach { url ->
            try {
                imageCacheRepository.save(url)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.error("save failure", e)
            }
        }.launchIn(coroutineScope)
    }
    fun save(emoji: Emoji) {
        queue.tryEmit(emoji)
    }
}