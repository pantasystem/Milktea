package net.pantasystem.milktea.model.emoji

import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.UseCase
import net.pantasystem.milktea.model.image.ImageCache
import net.pantasystem.milktea.model.image.ImageCacheRepository
import javax.inject.Inject

class SaveCustomEmojiImageUseCase @Inject constructor(
    val imageCacheRepository: ImageCacheRepository,
    val customEmojiAspectRatioDataSource: CustomEmojiAspectRatioDataSource,
) : UseCase {
    suspend operator fun invoke(emoji: CustomEmoji): Result<ImageCache?> = runCancellableCatching {
        val url = emoji.url ?: emoji.uri ?: return@runCancellableCatching null
        val result = imageCacheRepository.save(url).getOrThrow()
        val width = result.width?.takeIf {
            it > 0
        } ?: return@runCancellableCatching result
        val height = result.height?.takeIf {
            it > 0
        } ?: return@runCancellableCatching result
        val aspectRatio = width.toFloat() / height.toFloat()
        customEmojiAspectRatioDataSource.save(
            CustomEmojiAspectRatio(
                uri = url,
                aspectRatio = aspectRatio,
            )
        ).getOrThrow()
        result
    }
}