package net.pantasystem.milktea.data.infrastructure.emoji

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.model.emoji.CustomEmojiAspectRatio
import net.pantasystem.milktea.model.emoji.CustomEmojiAspectRatioDataSource
import javax.inject.Inject

class CustomEmojiAspectRatioDataSourceImpl @Inject constructor(
    private val customEmojiAspectRatioDAO: CustomEmojiAspectRatioDAO,
    @IODispatcher val coroutineDispatcher: CoroutineDispatcher,
) : CustomEmojiAspectRatioDataSource {


    override suspend fun findIn(uris: List<String>): Result<List<CustomEmojiAspectRatio>> =
        runCancellableCatching {
            withContext(coroutineDispatcher) {
                uris.chunked(50).map {
                    customEmojiAspectRatioDAO.findIn(it)
                }
            }.flatten().map {
                CustomEmojiAspectRatio(it.uri, it.aspectRatio)
            }
        }

    override suspend fun findOne(uri: String): Result<CustomEmojiAspectRatio> = runCancellableCatching {
        withContext(coroutineDispatcher) {
            customEmojiAspectRatioDAO.findOne(uri)?.let {
                CustomEmojiAspectRatio(it.uri, it.aspectRatio)
            } ?: throw NoSuchElementException()
        }
    }

    override suspend fun save(ratio: CustomEmojiAspectRatio): Result<CustomEmojiAspectRatio> = runCancellableCatching {
        withContext(coroutineDispatcher) {
            customEmojiAspectRatioDAO.upsert(
                CustomEmojiAspectRatioEntity(
                    ratio.uri,
                    ratio.aspectRatio,
                )
            )
        }
        findOne(ratio.uri).getOrThrow()
    }

    override suspend fun delete(ratio: CustomEmojiAspectRatio): Result<Unit> = runCancellableCatching {
        withContext(coroutineDispatcher) {
            customEmojiAspectRatioDAO.delete(ratio.uri)
        }
    }
}