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
        val url = (emoji.url ?: emoji.uri)
        if (aspectRatio <= 0f || url == null) {
            logger.debug {
                "cancel save emoji aspect. emoji:$emoji, aspect:$aspectRatio"
            }
            return
        }
        coroutineScope.launch {
            val ratio = customEmojiAspectRatioDataSource.findOne(url).getOrNull()
            if (ratio != null) {
                return@launch
            }
            customEmojiAspectRatioDataSource.save(CustomEmojiAspectRatio(
                uri = url,
                aspectRatio = aspectRatio,
            )).onFailure {
                logger.error("save failure", it)
            }.onSuccess {
                logger.debug {
                    "success save emoji aspect ratio:$emoji, $aspectRatio, $it"
                }
            }
        }
    }
}