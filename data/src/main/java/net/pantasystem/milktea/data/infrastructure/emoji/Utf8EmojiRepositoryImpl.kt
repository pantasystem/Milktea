package net.pantasystem.milktea.data.infrastructure.emoji

import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.model.emoji.Utf8Emoji
import net.pantasystem.milktea.model.emoji.UtfEmojiRepository
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

const val RAW_EMOJI_SOURCE_URL: String =
    "https://raw.githubusercontent.com/amio/emoji.json/master/emoji.json"

class Utf8EmojiRepositoryImpl @Inject constructor(
    val coroutineScope: CoroutineScope,
    val loggerFactory: Logger.Factory?,
    val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ) : UtfEmojiRepository {

    private val logger by lazy {
        loggerFactory?.create("Utf8EmojiRepository")
    }
    private var isFetched = false
    private var emojis: List<Utf8Emoji> = emptyList()


    private val client by lazy {
        OkHttpClient.Builder()
            .build()
    }

    private val json = Json{ignoreUnknownKeys = true}


    init {
        coroutineScope.launch(Dispatchers.IO) {
            runCatching {
                findAll()
            }.onFailure {
                logger?.error("絵文字の取得に失敗しました", it)
            }
        }
    }

    override suspend fun findAll(): List<Utf8Emoji> {
        if (!isFetched) {
            emojis = fetchEmojis()
            isFetched = true
        }
        return emojis

    }

    override suspend fun exists(emoji: CharSequence): Boolean {
        if (!isFetched) {
            findAll()
        }
        return emojis.any {
            emoji.startsWith(it.char)
        }
    }


    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun fetchEmojis(): List<Utf8Emoji> {
        return withContext(dispatcher) {
            val res = client.newCall(Request.Builder().url(RAW_EMOJI_SOURCE_URL).build()).execute()
            if (!res.isSuccessful) {
                throw Exception("取得に失敗しました")
            }
            val body = res.body!!.string()
            json.decodeFromString(body)
        }
    }
}