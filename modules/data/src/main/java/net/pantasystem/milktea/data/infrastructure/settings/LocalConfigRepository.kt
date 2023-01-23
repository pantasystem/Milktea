package net.pantasystem.milktea.data.infrastructure.settings

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import net.pantasystem.milktea.common.runCancellableCatching
import net.pantasystem.milktea.model.notes.CanLocalOnly
import net.pantasystem.milktea.model.notes.Visibility
import net.pantasystem.milktea.model.notes.isLocalOnly
import net.pantasystem.milktea.model.setting.*


class LocalConfigRepositoryImpl(
    private val sharedPreference: SharedPreferences
) : LocalConfigRepository {

    override fun get(): Result<Config> {
        return runCancellableCatching {
            Config.from(sharedPreference.getPrefTypes())
        }
    }

    override suspend fun save(config: Config): Result<Unit> {
        return runCancellableCatching {
            val old = get().getOrThrow().prefs()
            sharedPreference.edit {
                config.prefs().filterNot {
                    old[it.key] == it.value
                }.map {
                    when (val entry = it.value) {
                        is PrefType.BoolPref -> putBoolean(it.key.str(), entry.value)
                        is PrefType.IntPref -> putInt(it.key.str(), entry.value)
                        is PrefType.StrPref -> putString(it.key.str(), entry.value)
                    }
                }
            }
        }
    }

    override fun getRememberVisibility(accountId: Long): Result<RememberVisibility> {
        return runCancellableCatching {
            val isRemember = sharedPreference.getBoolean(
                RememberVisibility.Keys.IsRememberNoteVisibility.str(),
                true
            )
            if (isRemember) {
                val localOnly = sharedPreference.getBoolean(
                    RememberVisibility.Keys.IsLocalOnly(accountId).str(),
                    DefaultConfig.getRememberVisibilityConfig(accountId).visibility.isLocalOnly()
                )
                val visibility = when (sharedPreference.getString(
                    RememberVisibility.Keys.NoteVisibility(accountId).str(),
                    "public"
                )) {
                    "home" -> Visibility.Home(localOnly)
                    "followers" -> Visibility.Followers(localOnly)
                    "specified" -> Visibility.Specified(emptyList())
                    "mutual" -> Visibility.Mutual
                    "personal" -> Visibility.Personal
                    else -> Visibility.Public(localOnly)
                }
                RememberVisibility.Remember(
                    accountId = accountId,
                    visibility = visibility,
                )
            } else {
                RememberVisibility.None
            }
        }
    }

    override suspend fun save(remember: RememberVisibility): Result<Unit> {
        return runCancellableCatching {
            when (remember) {
                is RememberVisibility.Remember -> {
                    val localOnly = (remember.visibility as? CanLocalOnly)?.isLocalOnly ?: false
                    val str = when (remember.visibility) {
                        is Visibility.Public -> "public"
                        is Visibility.Home -> "home"
                        is Visibility.Followers -> "followers"
                        is Visibility.Specified -> "specified"
                        is Visibility.Limited -> "limited"
                        Visibility.Mutual -> "mutual"
                        Visibility.Personal -> "personal"
                    }
                    sharedPreference.edit {
                        putString(
                            RememberVisibility.Keys.NoteVisibility(remember.accountId).str(),
                            str
                        )
                        putBoolean(
                            RememberVisibility.Keys.IsLocalOnly(remember.accountId).str(),
                            localOnly
                        )
                        putBoolean(RememberVisibility.Keys.IsRememberNoteVisibility.str(), true)
                    }
                }
                is RememberVisibility.None -> {
                    sharedPreference.edit {
                        putBoolean(RememberVisibility.Keys.IsRememberNoteVisibility.str(), false)
                    }
                }
            }
        }
    }

    override fun observe(): Flow<Config> {
        return sharedPreference.asFlow(Keys.allKeys.first().str()).map {
            get().getOrThrow()
        }.distinctUntilChanged()
    }

    override fun observeRememberVisibility(accountId: Long): Flow<RememberVisibility> {
        return sharedPreference.asFlow("").map {
            getRememberVisibility(accountId).getOrThrow()
        }.distinctUntilChanged()
    }

}

@Suppress("ObjectLiteralToLambda")
private fun SharedPreferences.asFlow(initialEvent: String? = null): Flow<String> {
    return channelFlow {
        // NOTE: SharedPreferenceは初期イベントを流してくれないので初期イベントを流したい場合困るので流している
        if (initialEvent != null) {
            trySend(initialEvent)
        }
        // NOTE: ラムダだとWeakReferenceが解放してしまうようなのでインスタンスにする必要がある。
        val listener: OnSharedPreferenceChangeListener =
            object : OnSharedPreferenceChangeListener {
                override fun onSharedPreferenceChanged(
                    sharedPreferences: SharedPreferences?,
                    key: String?
                ) {
                    if (key != null) {
                        trySend(key)
                    }
                }
            }

        registerOnSharedPreferenceChangeListener(listener)
        awaitClose {
            unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

}

