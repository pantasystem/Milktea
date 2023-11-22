package net.pantasystem.milktea.data.infrastructure.statistics

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.model.statistics.InAppPostCounterRepository
import javax.inject.Inject

class InAppPostCounterRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    scope: CoroutineScope,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val loggerFactory: Logger.Factory,
) : InAppPostCounterRepository {

    private val countStateFlow = MutableStateFlow(0)

    private val logger by lazy {
        loggerFactory.create("InAppPostCounterRepositoryImpl")
    }

    private val sharedPreferences by lazy {
        context.getSharedPreferences("in_app_post_counter", Context.MODE_PRIVATE)
    }

    init {
        scope.launch(ioDispatcher) {
            try {
                countStateFlow.value = sharedPreferences.getInt("count", 0)
            } catch (e: Exception) {
                logger.error("InAppPostCounterRepositoryImpl初期化エラー", e = e)
            }
        }
    }

    override fun observe(): Flow<Int> {
        return countStateFlow
    }

    override suspend fun get(): Result<Int> {
        return Result.success(countStateFlow.value)
    }

    override suspend fun increment(): Result<Unit> = runCancellableCatching{
        countStateFlow.value++
        sharedPreferences.edit().putInt("count", countStateFlow.value).apply()
    }

    override suspend fun clear(): Result<Unit> = runCancellableCatching {
        countStateFlow.value = 0
        sharedPreferences.edit().putInt("count", 0).apply()
    }

}