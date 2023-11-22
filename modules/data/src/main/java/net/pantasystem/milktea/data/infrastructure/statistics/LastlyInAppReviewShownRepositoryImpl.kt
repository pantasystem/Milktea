package net.pantasystem.milktea.data.infrastructure.statistics

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.common_android.hilt.IODispatcher
import net.pantasystem.milktea.model.statistics.LastlyInAppReviewShownRepository
import javax.inject.Inject

class LastlyInAppReviewShownRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    scope: CoroutineScope,
    @IODispatcher private val ioDispatcher: CoroutineDispatcher,
    private val loggerFactory: Logger.Factory,
) : LastlyInAppReviewShownRepository {

    private val logger by lazy {
        loggerFactory.create("LastlyInAppReviewShownRepositoryImpl")
    }

    private val sharedPreferences by lazy {
        context.getSharedPreferences(StatisticsPreferencesKeys.PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    private val lastlyInAppReviewShown = MutableStateFlow<Instant?>(null)
    init {
        scope.launch(ioDispatcher) {
            try {
                sharedPreferences.getString(StatisticsPreferencesKeys.LASTLY_IN_APP_REVIEW_SHOWN, null)?.let {
                    lastlyInAppReviewShown.value = Instant.parse(it)
                }
            } catch (e: Exception) {
                logger.error("LastlyInAppReviewShownRepositoryImpl初期化エラー", e = e)
            }
        }
    }
    override fun observe(): Flow<Instant?> {
        return lastlyInAppReviewShown
    }

    override suspend fun get(): Result<Instant?> {
        return Result.success(lastlyInAppReviewShown.value)
    }

    override suspend fun set(time: Instant): Result<Unit> = runCancellableCatching{
        sharedPreferences.edit().putString(StatisticsPreferencesKeys.LASTLY_IN_APP_REVIEW_SHOWN, time.toString()).apply()
    }

    override suspend fun clear(): Result<Unit> = runCancellableCatching {
        sharedPreferences.edit().remove(StatisticsPreferencesKeys.LASTLY_IN_APP_REVIEW_SHOWN).apply()
    }
}