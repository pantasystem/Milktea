@file:Suppress("UNCHECKED_CAST")

package net.pantasystem.milktea.data.infrastructure.settings

import android.content.SharedPreferences
import net.pantasystem.milktea.model.setting.*

fun RememberVisibility.Keys.str(): String {
    return when (this) {
        is RememberVisibility.Keys.IsLocalOnly -> "accountId:${accountId}:IS_LOCAL_ONLY"
        is RememberVisibility.Keys.IsRememberNoteVisibility -> "IS_LEARN_NOTE_VISIBILITY"
        is RememberVisibility.Keys.NoteVisibility -> "accountId:${accountId}:NOTE_VISIBILITY"
    }
}

fun SharedPreferences.getPrefTypes(keys: Set<Keys> = Keys.allKeys): Map<Keys, PrefType?> {
    val strKeys = keys.associateBy { it.str() }
    return all.filter {
        strKeys.contains(it.key)
    }.map {
        strKeys[it.key]?.let { key ->
            key to it.value?.let { value ->
                when(value::class) {
                    Boolean::class -> {
                        PrefType.BoolPref(value as Boolean)
                    }
                    String::class -> {
                        PrefType.StrPref(value as String)
                    }
                    Int::class -> {
                        PrefType.IntPref(value as Int)
                    }
                    Float::class -> {
                        PrefType.FloatPref(value as Float)
                    }
                    else -> null
                }
            }
        }
    }.filterNotNull().toMap()
}

fun Config.Companion.from(map: Map<Keys, PrefType?>): Config {
    return Config(
        isSimpleEditorEnabled = map.getValue<PrefType.BoolPref>(Keys.IsSimpleEditorEnabled)?.value
            ?: DefaultConfig.config.isSimpleEditorEnabled,
        reactionPickerType = (map.getValue<PrefType.IntPref>(Keys.ReactionPickerType)?.value)
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
        backgroundImagePath = map.getValue<PrefType.StrPref>(Keys.BackgroundImage)?.value
            ?: DefaultConfig.config.backgroundImagePath,
        isClassicUI = map.getValue<PrefType.BoolPref>(Keys.ClassicUI)?.value
            ?: DefaultConfig.config.isClassicUI,
        isUserNameDefault = map.getValue<PrefType.BoolPref>(Keys.IsUserNameDefault)?.value
            ?: DefaultConfig.config.isUserNameDefault,
        isPostButtonAtTheBottom = map.getValue<PrefType.BoolPref>(Keys.IsPostButtonToBottom)?.value
            ?: DefaultConfig.config.isPostButtonAtTheBottom,
        noteExpandedHeightSize = map.getValue<PrefType.IntPref>(Keys.NoteLimitHeight)?.value
            ?: DefaultConfig.config.noteExpandedHeightSize,
        theme = Theme.from(map.getValue<PrefType.IntPref>(Keys.ThemeType)?.value ?: 0),
        isIncludeRenotedMyNotes = map.getValue<PrefType.BoolPref>(Keys.IsIncludeRenotedMyNotes)?.value
            ?: DefaultConfig.config.isIncludeMyRenotes,
        isIncludeMyRenotes = map.getValue<PrefType.BoolPref>(Keys.IsIncludeMyRenotes)?.value
            ?: DefaultConfig.config.isIncludeMyRenotes,
        isIncludeLocalRenotes = map.getValue<PrefType.BoolPref>(Keys.IsIncludeLocalRenotes)?.value
            ?: DefaultConfig.config.isIncludeLocalRenotes,
        surfaceColorOpacity = map.getValue<PrefType.IntPref>(Keys.SurfaceColorOpacity)?.value
            ?: DefaultConfig.config.surfaceColorOpacity,
        isEnableTimelineScrollAnimation = map.getValue<PrefType.BoolPref>(Keys.IsEnableTimelineScrollAnimation)?.value
            ?: DefaultConfig.config.isEnableTimelineScrollAnimation,
        isCrashlyticsCollectionEnabled = IsCrashlyticsCollectionEnabled(
            isEnable = map.getValue<PrefType.BoolPref>(Keys.IsCrashlyticsCollectionEnabled)?.value
                ?: DefaultConfig.config.isCrashlyticsCollectionEnabled.isEnable,
            isConfirmed = map.getValue<PrefType.BoolPref>(Keys.IsConfirmedCrashlyticsCollection)?.value
                ?: DefaultConfig.config.isCrashlyticsCollectionEnabled.isConfirmed
        ),
        isAnalyticsCollectionEnabled = IsAnalyticsCollectionEnabled(
            isEnabled = map.getValue<PrefType.BoolPref>(Keys.IsAnalyticsCollectionEnabled)?.value
                ?: DefaultConfig.config.isAnalyticsCollectionEnabled.isEnabled,
            isConfirmed = map.getValue<PrefType.BoolPref>(Keys.IsConfirmedAnalyticsCollection)?.value
                ?: DefaultConfig.config.isAnalyticsCollectionEnabled.isConfirmed,
        ),
        isConfirmedPostNotification = map.getValue<PrefType.BoolPref>(
            Keys.IsConfirmedPostNotification
        )?.value ?: false,
        isEnableInstanceTicker = map.getValue<PrefType.BoolPref>(
            Keys.IsEnableInstanceTicker
        )?.value ?: DefaultConfig.config.isEnableInstanceTicker,
        isDriveUsingGridView = map.getValue<PrefType.BoolPref>(
            Keys.IsDriveUsingGridView
        )?.value ?: DefaultConfig.config.isDriveUsingGridView,
        isEnableNotificationSound = map.getValue<PrefType.BoolPref>(
            Keys.IsEnableNotificationSound
        )?.value ?: DefaultConfig.config.isEnableNotificationSound,
        isStopStreamingApiWhenBackground = map.getValue<PrefType.BoolPref>(
            Keys.IsStopStreamingApiWhenBackground
        )?.value ?: DefaultConfig.config.isStopStreamingApiWhenBackground,
        isStopNoteCaptureWhenBackground = map.getValue<PrefType.BoolPref>(
            Keys.IsStopNoteCaptureWhenBackground
        )?.value ?: DefaultConfig.config.isStopNoteCaptureWhenBackground,
        isEnableStreamingAPIAndNoteCapture = map.getValue<PrefType.BoolPref>(
            Keys.IsEnableStreamingAPIAndNoteCapture
        )?.value ?: DefaultConfig.config.isEnableStreamingAPIAndNoteCapture,
        isEnableNoteDivider = map.getValue<PrefType.BoolPref>(
            Keys.IsEnableNoteDivider
        )?.value ?: DefaultConfig.config.isEnableNoteDivider,
        isVisibleInstanceUrlInToolbar = map.getValue<PrefType.BoolPref>(
            Keys.IsVisibleInstanceUrlInToolbar
        )?.value ?: DefaultConfig.config.isVisibleInstanceUrlInToolbar,
        isHideMediaWhenMobileNetwork = map.getValue<PrefType.BoolPref>(
            Keys.IsHideMediaWhenMobileNetwork
        )?.value ?: DefaultConfig.config.isHideMediaWhenMobileNetwork,
        noteHeaderFontSize = map.getValue<PrefType.FloatPref>(
            Keys.NoteHeaderFontSize
        )?.value ?: DefaultConfig.config.noteHeaderFontSize,
        noteContentFontSize = map.getValue<PrefType.FloatPref>(
            Keys.NoteContentFontSize
        )?.value ?: DefaultConfig.config.noteContentFontSize,
        isDisplayTimestampsAsAbsoluteDates = map.getValue<PrefType.BoolPref>(
            Keys.IsDisplayTimestampsAsAbsoluteDates
        )?.value ?: DefaultConfig.config.isDisplayTimestampsAsAbsoluteDates,
        noteReactionCounterFontSize = map.getValue<PrefType.FloatPref>(
            Keys.NoteReactionCounterFontSize
        )?.value ?: DefaultConfig.config.noteReactionCounterFontSize,
        noteCustomEmojiScaleSizeInText = map.getValue<PrefType.FloatPref>(
            Keys.NoteCustomEmojiScaleSizeInText
        )?.value ?: DefaultConfig.config.noteCustomEmojiScaleSizeInText,
        emojiPickerEmojiDisplaySize = map.getValue<PrefType.IntPref>(
            Keys.EmojiPickerEmojiDisplaySize
        )?.value ?: DefaultConfig.config.emojiPickerEmojiDisplaySize,
    )
}

private fun <T : PrefType?> Map<Keys, PrefType?>.getValue(key: Keys): T? {
    return this[key] as? T
}

fun Config.pref(key: Keys): PrefType {
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

        Keys.ThemeType -> {
            PrefType.IntPref(theme.toInt())
        }
        Keys.IsIncludeLocalRenotes -> {
            PrefType.BoolPref(isIncludeLocalRenotes)
        }
        Keys.IsIncludeMyRenotes -> {
            PrefType.BoolPref(isIncludeMyRenotes)
        }
        Keys.IsIncludeRenotedMyNotes -> {
            PrefType.BoolPref(isIncludeRenotedMyNotes)
        }
        Keys.SurfaceColorOpacity -> {
            PrefType.IntPref(surfaceColorOpacity)
        }
        Keys.IsEnableTimelineScrollAnimation -> {
            PrefType.BoolPref(isEnableTimelineScrollAnimation)
        }
        Keys.IsCrashlyticsCollectionEnabled -> {
            PrefType.BoolPref(isCrashlyticsCollectionEnabled.isEnable)
        }
        Keys.IsConfirmedCrashlyticsCollection -> {
            PrefType.BoolPref(isCrashlyticsCollectionEnabled.isConfirmed)
        }
        Keys.IsAnalyticsCollectionEnabled -> {
            PrefType.BoolPref(isAnalyticsCollectionEnabled.isEnabled)
        }
        Keys.IsConfirmedAnalyticsCollection -> {
            PrefType.BoolPref(isAnalyticsCollectionEnabled.isConfirmed)
        }
        Keys.IsConfirmedPostNotification -> {
            PrefType.BoolPref(isConfirmedPostNotification)
        }
        Keys.IsEnableInstanceTicker -> {
            PrefType.BoolPref(isEnableInstanceTicker)
        }
        Keys.IsDriveUsingGridView -> {
            PrefType.BoolPref(isDriveUsingGridView)
        }
        Keys.IsEnableNotificationSound -> {
            PrefType.BoolPref(isEnableNotificationSound)
        }
        Keys.IsStopNoteCaptureWhenBackground -> {
            PrefType.BoolPref(isStopNoteCaptureWhenBackground)
        }
        Keys.IsStopStreamingApiWhenBackground -> {
            PrefType.BoolPref(isStopStreamingApiWhenBackground)
        }
        Keys.IsEnableStreamingAPIAndNoteCapture -> {
            PrefType.BoolPref(isEnableStreamingAPIAndNoteCapture)
        }
        Keys.IsEnableNoteDivider -> {
            PrefType.BoolPref(isEnableNoteDivider)
        }
        Keys.IsVisibleInstanceUrlInToolbar -> {
            PrefType.BoolPref(isVisibleInstanceUrlInToolbar)
        }
        Keys.IsHideMediaWhenMobileNetwork -> {
            PrefType.BoolPref(isHideMediaWhenMobileNetwork)
        }
        Keys.NoteContentFontSize -> {
            PrefType.FloatPref(noteContentFontSize)
        }
        Keys.NoteHeaderFontSize -> {
            PrefType.FloatPref(noteHeaderFontSize)
        }
        Keys.IsDisplayTimestampsAsAbsoluteDates -> {
            PrefType.BoolPref(isDisplayTimestampsAsAbsoluteDates)
        }
        Keys.NoteReactionCounterFontSize -> {
            PrefType.FloatPref(noteReactionCounterFontSize)
        }
        Keys.NoteCustomEmojiScaleSizeInText -> {
            PrefType.FloatPref(noteCustomEmojiScaleSizeInText)
        }
        Keys.EmojiPickerEmojiDisplaySize -> {
            PrefType.IntPref(emojiPickerEmojiDisplaySize)
        }
    }
}

fun Config.prefs(): Map<Keys, PrefType> {
    val map = mutableMapOf<Keys, PrefType>()
    Keys.allKeys.forEach { key ->
        pref(key).let {
            map[key] = it
        }
    }
    return map
}