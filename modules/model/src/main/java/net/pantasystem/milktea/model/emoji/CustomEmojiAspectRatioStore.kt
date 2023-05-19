package net.pantasystem.milktea.model.emoji

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common.Logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomEmojiAspectRatioStore  @Inject constructor(
    private val coroutineScope: CoroutineScope,
    private val customEmojiAspectRatioDataSource: CustomEmojiAspectRatioDataSource,
    loggerFactory: Logger.Factory,
){

    private val logger by lazy {
        loggerFactory.create("CEARStore")
    }

    fun save(emoji: Emoji, aspectRatio: Float) {
        val url = emoji.url ?: emoji.uri ?: return
        if (aspectRatio <= 0f) {
            return
        }
        coroutineScope.launch {
            customEmojiAspectRatioDataSource.save(CustomEmojiAspectRatio(
                uri = url,
                aspectRatio = aspectRatio,
            )).onFailure {
                logger.error("save failure", it)
            }
        }
    }
}