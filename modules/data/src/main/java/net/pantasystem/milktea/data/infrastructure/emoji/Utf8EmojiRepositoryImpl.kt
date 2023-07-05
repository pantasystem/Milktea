package net.pantasystem.milktea.data.infrastructure.emoji
//
//import kotlinx.coroutines.*
//import kotlinx.coroutines.flow.catch
//import kotlinx.coroutines.flow.launchIn
//import kotlinx.coroutines.flow.onEach
//import kotlinx.serialization.decodeFromString
//import kotlinx.serialization.json.Json
//import net.pantasystem.milktea.common.Logger
//import net.pantasystem.milktea.common.runCancellableCatching
//import net.pantasystem.milktea.model.emoji.Utf8Emoji
//import net.pantasystem.milktea.model.emoji.UtfEmojiRepository
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import javax.inject.Inject
//
//const val RAW_EMOJI_SOURCE_URL: String =
//    "https://raw.githubusercontent.com/amio/emoji.json/master/emoji.json"
//
//class Utf8EmojiRepositoryImpl @Inject constructor(
//    coroutineScope: CoroutineScope,
//    private val loggerFactory: Logger.Factory?,
//    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
//    private val utf8EmojisDAO: Utf8EmojisDAO,
//) : UtfEmojiRepository {
//
//    private val logger by lazy {
//        loggerFactory?.create("Utf8EmojiRepository")
//    }
//    private var isFetched = false
//    private var emojis: List<Utf8Emoji> = emptyList()
//
//
//    private val client by lazy {
//        OkHttpClient.Builder()
//            .build()
//    }
//
//    private val json = Json { ignoreUnknownKeys = true }
//
//
//    init {
//        coroutineScope.launch(dispatcher) {
//            runCancellableCatching {
//                findAll()
//            }.onFailure {
//                logger?.error("絵文字の取得に失敗しました", it)
//            }
//        }
//        coroutineScope.launch(dispatcher) {
//            utf8EmojisDAO.findAll().onEach { list ->
//                emojis = list.map { it.toModel() }
//            }.catch {
//                logger?.error("絵文字の取得に失敗しました", it)
//            }.launchIn(this)
//        }
//    }
//
//    override suspend fun findAll(): List<Utf8Emoji> {
//        if (!isFetched) {
//            utf8EmojisDAO.clear()
//            val fetchedEmojis = fetchEmojis()
//            val list = fetchedEmojis.map { it.toDTO() }
//            utf8EmojisDAO.insertAll(list)
//            isFetched = true
//            emojis = fetchedEmojis
//            return fetchedEmojis
//        }
//        return emojis
//
//    }
//
//    override suspend fun exists(emoji: CharSequence): Boolean {
//        logger?.debug { "call exists emoji:$emoji, ${emoji.javaClass.simpleName}" }
//        return emojis.any {
//            emoji.startsWith(it.char)
//        }.also {
//            logger?.debug { "call exists emoji:$emoji, ${emoji.javaClass.simpleName} return :$it" }
//            logger?.debug { "target emojis:${emojis.filter { c ->emoji.startsWith(c.char) }}" }
//        }
//    }
//
//
//    @Suppress("BlockingMethodInNonBlockingContext")
//    private suspend fun fetchEmojis(): List<Utf8Emoji> {
//        return withContext(dispatcher) {
//            val res = client.newCall(Request.Builder().url(RAW_EMOJI_SOURCE_URL).build()).execute()
//            if (!res.isSuccessful) {
//                throw Exception("取得に失敗しました")
//            }
//            val body = res.body!!.string()
//            json.decodeFromString(body)
//        }
//    }
//}