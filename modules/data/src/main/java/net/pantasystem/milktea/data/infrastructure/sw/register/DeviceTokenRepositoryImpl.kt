package net.pantasystem.milktea.data.infrastructure.sw.register

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.sw.register.DeviceTokenRepository
import javax.inject.Inject
import kotlin.time.Duration.Companion.days

class DeviceTokenRepositoryImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val context: Context,
) : DeviceTokenRepository {

    companion object {
        const val TOKEN =
            "net.pantasystem.milktea.data.infrastructure.sw.register.DeviceTokenRepositoryImpl.TOKEN"
        const val GET_START_AT =
            "net.pantasystem.milktea.data.infrastructure.sw.register.DeviceTokenRepositoryImpl.GET_START_AT"
    }

    val lock = Mutex()

    override fun clear(): Result<Unit> = runCancellableCatching{
        sharedPreferences.edit {
            putString(TOKEN, null)
            putString(GET_START_AT, null)
        }
    }

    override suspend fun getOrCreate(): Result<String> = runCancellableCatching {
        lock.withLock {
            var token = sharedPreferences.getString(TOKEN, null)

            // NOTE: 既にTokenが入っている場合はそれを返す
            if (!token.isNullOrBlank()) {
                return@runCancellableCatching token
            }

            val now = Clock.System.now()

            // NOTE: 前回Tokenの取得を実施した日時を取得する
            // NOTE: 成功時に必ずToken取得日の記録を削除するようにしているので、取得日が残っているということは、前回取得に失敗していると言える
            val beforeGetStartAt = sharedPreferences.getString(GET_START_AT, null)?.let {
                try {
                    Instant.parse(it)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }

            // NOTE: 前回のTokenの取得に失敗していて、まだ24時間経過していない場合はエラー扱いにする
            if (beforeGetStartAt != null) {
                val diff = now - beforeGetStartAt
                if (diff < 1.days) {
                    throw IllegalArgumentException("Tokenの取得は24時間以内に失敗している可能性があります。前回失敗した場合は24時間以上時間を空けて取得する必要性があります。")
                }
            }
            sharedPreferences.edit {
                putString(GET_START_AT, now.toString())
            }

            when (val state =
                GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)) {
                ConnectionResult.SUCCESS -> {
                    token = FirebaseMessaging.getInstance().token.asSuspend()
                    save(token)
                }
                else -> {
                    throw IllegalStateException("Google Play Serviceが有効ではないあるいは接続に失敗しためキャンセルしました。state:${state}")
                }
            }
            token
        }

    }

    override fun save(deviceToken: String): Result<Unit> = runCancellableCatching {
        sharedPreferences.edit {
            putString(TOKEN, deviceToken)
            putString(GET_START_AT, null)
        }
    }
}