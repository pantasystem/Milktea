package net.pantasystem.milktea.model.emoji

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomEmojiAspectRatioStore  @Inject constructor(
    private val coroutineScope: CoroutineScope,
    private val customEmojiAspectRatioDataSource: CustomEmojiAspectRatioDataSource,
){

    fun save(emoji: Emoji, aspectRatio: Float) {
        val url = emoji.url ?: emoji.uri ?: return
        if (aspectRatio <= 0f) {
            return
        }
        coroutineScope.launch {
            customEmojiAspectRatioDataSource.save(CustomEmojiAspectRatio(
                uri = url,
                aspectRatio = aspectRatio,
            ))
        }
    }
}