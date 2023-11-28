package net.pantasystem.milktea.data.infrastructure.emoji

import io.objectbox.Box
import io.objectbox.kotlin.awaitCallInTx
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.inValues
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.data.infrastructure.BoxStoreHolder
import net.pantasystem.milktea.model.emoji.CustomEmojiAspectRatio
import net.pantasystem.milktea.model.emoji.CustomEmojiAspectRatioDataSource
import javax.inject.Inject

class CustomEmojiAspectRatioDataSourceImpl @Inject constructor(
    private val boxStoreHolder: BoxStoreHolder,
    @IODispatcher val coroutineDispatcher: CoroutineDispatcher,
) : CustomEmojiAspectRatioDataSource {
    private val aspectBox: Box<CustomEmojiAspectRatioRecord> by lazy {
        boxStoreHolder.boxStore.boxFor()
    }

    override suspend fun findIn(uris: List<String>): Result<List<CustomEmojiAspectRatio>> =
        runCancellableCatching {
            withContext(coroutineDispatcher) {
                uris.chunked(125) { uris ->
                    aspectBox.query().inValues(
                        CustomEmojiAspectRatioRecord_.uri,
                        uris.toTypedArray(),
                        QueryBuilder.StringOrder.CASE_SENSITIVE
                    ).build().find().map {
                        CustomEmojiAspectRatio(
                            uri = it.uri,
                            aspectRatio = it.aspectRatio
                        )
                    }
                }.flatten()
            }
        }

    override suspend fun findOne(uri: String): Result<CustomEmojiAspectRatio> = runCancellableCatching {
        withContext(coroutineDispatcher) {
            aspectBox.query().equal(
                CustomEmojiAspectRatioRecord_.uri,
                uri,
                QueryBuilder.StringOrder.CASE_SENSITIVE
            ).build().findFirst()?.let {
                CustomEmojiAspectRatio(
                    uri = it.uri,
                    aspectRatio = it.aspectRatio
                )
            } ?: throw NoSuchElementException()
        }
    }

    override suspend fun save(ratio: CustomEmojiAspectRatio): Result<CustomEmojiAspectRatio> = runCancellableCatching {
        withContext(coroutineDispatcher) {
            boxStoreHolder.boxStore.awaitCallInTx {
                val exists = aspectBox.query().equal(
                    CustomEmojiAspectRatioRecord_.uri,
                    ratio.uri,
                    QueryBuilder.StringOrder.CASE_SENSITIVE
                ).build().findFirst()
                if (exists == null) {
                    aspectBox.put(CustomEmojiAspectRatioRecord.from(ratio))
                } else {
                    aspectBox.put(exists.copy(aspectRatio = ratio.aspectRatio))
                }
            }
        }
        findOne(ratio.uri).getOrThrow()
    }

    override suspend fun delete(ratio: CustomEmojiAspectRatio): Result<Unit> = runCancellableCatching {
        withContext(coroutineDispatcher) {
            aspectBox.query().equal(
                CustomEmojiAspectRatioRecord_.uri,
                ratio.uri,
                QueryBuilder.StringOrder.CASE_SENSITIVE
            ).build().remove()
        }
    }
}