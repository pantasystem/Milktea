package net.pantasystem.milktea.data.infrastructure.emoji.objectbox

import io.objectbox.Box
import io.objectbox.BoxStore
import io.objectbox.kotlin.awaitCallInTx
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.inValues
import io.objectbox.kotlin.toFlow
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ObjectBoxCustomEmojiRecordDAO @Inject constructor(
    val boxStore: BoxStore
) {

    private val customEmojiBoxStore: Box<CustomEmojiRecord> by lazy {
        boxStore.boxFor()
    }

    fun findBy(host: String, name: String): List<CustomEmojiRecord> {
        return customEmojiBoxStore.query()
            .equal(CustomEmojiRecord_.emojiHost, host, QueryBuilder.StringOrder.CASE_INSENSITIVE)
            .equal(CustomEmojiRecord_.name, name, QueryBuilder.StringOrder.CASE_INSENSITIVE)
            .build()
            .find()
    }

    fun findBy(host: String): List<CustomEmojiRecord> {
        return customEmojiBoxStore.query()
            .equal(CustomEmojiRecord_.emojiHost, host, QueryBuilder.StringOrder.CASE_INSENSITIVE)
            .build()
            .find()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeBy(host: String): Flow<MutableList<CustomEmojiRecord>> {
        return customEmojiBoxStore.query()
            .equal(CustomEmojiRecord_.emojiHost, host, QueryBuilder.StringOrder.CASE_INSENSITIVE)
            .build()
            .subscribe()
            .toFlow()
    }

    suspend fun replaceAll(host: String, records: List<CustomEmojiRecord>) {
        boxStore.awaitCallInTx {
            customEmojiBoxStore.remove(findBy(host))
            customEmojiBoxStore.put(records)
        }
    }

    fun deleteByHostAndNames(host: String, names: List<String>) {
        customEmojiBoxStore.query()
            .equal(CustomEmojiRecord_.emojiHost, host, QueryBuilder.StringOrder.CASE_INSENSITIVE)
            .inValues(CustomEmojiRecord_.name, names.toTypedArray(), QueryBuilder.StringOrder.CASE_INSENSITIVE)
            .build()
            .remove()
    }

    fun appendEmojis(records: List<CustomEmojiRecord>) {
        customEmojiBoxStore.put(records)
    }
}