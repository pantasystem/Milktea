package net.pantasystem.milktea.model.emoji

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.pantasystem.milktea.common.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomEmojiAspectRatioStore @Inject constructor(
    coroutineScope: CoroutineScope,
    private val customEmojiAspectRatioDataSource: CustomEmojiAspectRatioDataSource,
    loggerFactory: Logger.Factory,
) {

    private val logger by lazy {
        loggerFactory.create("CEARStore")
    }

    private val saveEvents = MutableSharedFlow<Pair<Emoji, Float>>(
        extraBufferCapacity = 200,
        onBufferOverflow = BufferOverflow.DROP_LATEST
    )

    init {
        saveEvents.onEach { (emoji, aspectRatio) ->
            doSave(emoji, aspectRatio)
        }.launchIn(coroutineScope)
    }

    fun save(emoji: Emoji, aspectRatio: Float) {
        val url = (emoji.url ?: emoji.uri)
        if (aspectRatio <= 0f || url == null) {
            logger.debug {
                "cancel save emoji aspect. emoji:$emoji, aspect:$aspectRatio"
            }
            return
        }
        saveEvents.tryEmit(emoji to aspectRatio)
    }

    private suspend fun doSave(emoji: Emoji, aspectRatio: Float) {
        val url = (emoji.url ?: emoji.uri)
        if (aspectRatio <= 0f || url == null) {
            logger.debug {
                "cancel save emoji aspect. emoji:$emoji, aspect:$aspectRatio"
            }
            return
        }
        val ratio = customEmojiAspectRatioDataSource.findOne(url).getOrNull()
        if (ratio != null) {
            return
        }
        customEmojiAspectRatioDataSource.save(
            CustomEmojiAspectRatio(
                uri = url,
                aspectRatio = aspectRatio,
            )
        ).onFailure {
            logger.error("save failure", it)
        }.onSuccess {
            logger.debug {
                "success save emoji aspect ratio:$emoji, $aspectRatio, $it"
            }
        }
    }
}