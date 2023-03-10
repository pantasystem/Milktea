package net.pantasystem.milktea.data.infrastructure.notes.wordmute

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common.BuildConfig
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.model.notes.muteword.FilterConditionType
import net.pantasystem.milktea.model.notes.muteword.WordFilterConfig
import net.pantasystem.milktea.model.notes.muteword.WordFilterConfigRepository
import javax.inject.Inject

class WordFilterConfigRepositoryImpl @Inject constructor(
    coroutineScope: CoroutineScope,
    private val wordFilterConfigDao: WordFilterConfigDao,
    loggerFactory: Logger.Factory,
    private val cache: WordFilterConfigCache,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher
) : WordFilterConfigRepository {

    val logger = loggerFactory.create("WordFilterConfigRepositoryImpl")

    private val shared = wordFilterConfigDao.observeAll().map {
        it.toModel()
    }.catch {
        logger.error("observe error", it)
    }.onEach {
        cache.put(it)
    }.flowOn(ioDispatcher).stateIn(coroutineScope, SharingStarted.WhileSubscribed(5_000), null)

    override suspend fun get(): Result<WordFilterConfig> = runCancellableCatching{
        withContext(ioDispatcher) {
            val inMem = cache.get()
            if (inMem != null) {
                return@withContext inMem
            }
            val inDb = wordFilterConfigDao.findAll().toModel()
            cache.put(inDb)
            inDb
        }
    }

    override fun observe(): Flow<WordFilterConfig> {
        return shared.filterNotNull()
    }

    override suspend fun save(config: WordFilterConfig): Result<Unit> = runCancellableCatching {
        withContext(ioDispatcher) {
            wordFilterConfigDao.clear()
            val records = config.conditions.map {
                WordFilterConditionRecord()
            }
            val ids = wordFilterConfigDao.insertAll(records)
            config.conditions.forEachIndexed { index, filterConditionType ->
                val id = ids[index]
                when(filterConditionType) {
                    is FilterConditionType.Normal -> {
                        val wordRecords = filterConditionType.words.map {
                            WordFilterConditionWordRecord(it, parentId = id)
                        }
                        wordFilterConfigDao.insertWords(wordRecords)
                    }
                    is FilterConditionType.Regex -> {
                        wordFilterConfigDao.insertRegex(WordFilterConditionRegexRecord(filterConditionType.pattern, id))
                    }
                }
            }
            cache.put(config)
            if (BuildConfig.DEBUG) {
                val dbModel = wordFilterConfigDao.findAll().toModel()
                require(dbModel == config) {
                    "正常に保存できていない可能性 db:$dbModel, args:$config"
                }
            }
        }
    }
}