package net.pantasystem.milktea.data.infrastructure.settings

import android.content.SharedPreferences
import androidx.core.content.edit
import net.pantasystem.milktea.model.setting.*
import java.util.regex.Pattern






fun Config.pref(key: Keys): PrefType? {
    return when (key) {
        Keys.BackgroundImage -> {
            PrefType.StrPref(backgroundImagePath)
        }
        Keys.ClassicUI -> {
            PrefType.BoolPref(isClassicUI)
        }
        Keys.IsPostButtonToBottom -> {
            PrefType.BoolPref(isPostButtonAtTheBottom)
        }
        Keys.IsSimpleEditorEnabled -> {
            PrefType.BoolPref(isSimpleEditorEnabled)
        }
        Keys.IsUserNameDefault -> {
            PrefType.BoolPref(isUserNameDefault)
        }
        Keys.NoteLimitHeight -> {
            PrefType.IntPref(noteExpandedHeightSize)
        }
        Keys.ReactionPickerType -> {
            PrefType.IntPref(
                when (reactionPickerType) {
                    ReactionPickerType.LIST -> 0
                    ReactionPickerType.SIMPLE -> 1
                }
            )
        }
        Keys.SummalyServerUrl -> {
            val type = urlPreviewConfig.type
            if (type is UrlPreviewConfig.Type.SummalyServer) {
                PrefType.StrPref(type.url)
            } else {
                null
            }
        }
        Keys.UrlPreviewSourceType -> {
            when (urlPreviewConfig.type) {
                is UrlPreviewConfig.Type.Misskey -> {
                    PrefType.IntPref(UrlPreviewSourceSetting.MISSKEY)
                }
                is UrlPreviewConfig.Type.SummalyServer -> {
                    PrefType.IntPref(UrlPreviewSourceSetting.SUMMALY)
                }
                is UrlPreviewConfig.Type.InApp -> {
                    PrefType.IntPref(UrlPreviewSourceSetting.APP)
                }
            }
        }
        Keys.ThemeType -> {
            PrefType.IntPref(theme.toInt())
        }
    }
}

fun Config.prefs(): Map<String, PrefType> {
    val map = mutableMapOf<String, PrefType>()
    Keys.allKeys.forEach { key ->
        pref(key)?.let {
            map[key.str()] = it
        }
    }
    return map
}

class LocalConfigRepositoryImpl(
    private val sharedPreference: SharedPreferences
) : LocalConfigRepository {
    private val urlPattern =
        Pattern.compile("""(https)(://)([-_.!~*'()\[\]a-zA-Z0-9;/?:@&=+${'$'},%#]+)""")

    override fun get(): Result<Config> {
        return runCatching {
            Config(
                isSimpleEditorEnabled = sharedPreference.getBoolean(
                    Keys.IsSimpleEditorEnabled.str(), DefaultConfig.config.isSimpleEditorEnabled,
                ),
                reactionPickerType = sharedPreference.getInt(Keys.ReactionPickerType.str(), 0)
                    .let {
                        when (it) {
                            0 -> {
                                ReactionPickerType.LIST
                            }
                            1 -> {
                                ReactionPickerType.SIMPLE
                            }
                            else -> {
                                DefaultConfig.config.reactionPickerType
                            }
                        }
                    },
                backgroundImagePath = sharedPreference.getString(
                    Keys.BackgroundImage.str(),
                    DefaultConfig.config.backgroundImagePath
                ),
                isClassicUI = sharedPreference.getBoolean(
                    Keys.ClassicUI.str(),
                    DefaultConfig.config.isClassicUI
                ),
                isUserNameDefault = sharedPreference.getBoolean(
                    Keys.IsUserNameDefault.str(),
                    DefaultConfig.config.isUserNameDefault
                ),
                isPostButtonAtTheBottom = sharedPreference.getBoolean(
                    Keys.IsPostButtonToBottom.str(),
                    DefaultConfig.config.isPostButtonAtTheBottom
                ),
                urlPreviewConfig = UrlPreviewConfig(
                    type = getSourceType(),
                ),
                noteExpandedHeightSize = sharedPreference.getInt(
                    Keys.NoteLimitHeight.str(),
                    DefaultConfig.config.noteExpandedHeightSize
                ),
                theme = Theme.from(sharedPreference.getInt(Keys.ThemeType.str(), 0))
            )
        }
    }

    override suspend fun save(config: Config): Result<Unit> {
        return runCatching {
            val old = get().getOrThrow().prefs()
            sharedPreference.edit {
                config.prefs().filter {
                    old[it.key] == it.value
                }.map {
                    when (val entry = it.value) {
                        is PrefType.BoolPref -> putBoolean(it.key, entry.value)
                        is PrefType.IntPref -> putInt(it.key, entry.value)
                        is PrefType.StrPref -> putString(it.key, entry.value)
                    }
                }
            }
        }
    }

    fun getSourceType(): UrlPreviewConfig.Type {
        val type = sharedPreference.getInt(
            UrlPreviewSourceSetting.URL_PREVIEW_SOURCE_TYPE_KEY,
            UrlPreviewSourceSetting.MISSKEY
        )
        if (type in UrlPreviewSourceSetting.MISSKEY..UrlPreviewSourceSetting.APP) {
            return if (type == UrlPreviewSourceSetting.SUMMALY && getSummalyUrl() == null) {
                return UrlPreviewConfig.Type.SummalyServer(getSummalyUrl()!!)
            } else if (type == UrlPreviewSourceSetting.APP) {
                UrlPreviewConfig.Type.InApp
            } else {
                UrlPreviewConfig.Type.Misskey
            }

        } else {
            return UrlPreviewConfig.Type.Misskey
        }
    }

    fun getSummalyUrl(): String? {
        return sharedPreference.getString(Keys.SummalyServerUrl.str(), null)
            ?.let { url ->
                if (urlPattern.matcher(url).find()) {
                    url
                } else {
                    null
                }
            }
    }


}

