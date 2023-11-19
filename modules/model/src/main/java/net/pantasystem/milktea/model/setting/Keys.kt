package net.pantasystem.milktea.model.setting

val Keys.Companion.allKeys by lazy {
    setOf(
        Keys.IsSimpleEditorEnabled,
        Keys.ReactionPickerType,
        Keys.BackgroundImage,
        Keys.ClassicUI,
        Keys.IsUserNameDefault,
        Keys.IsPostButtonToBottom,
        Keys.NoteLimitHeight,
        Keys.IsIncludeLocalRenotes,
        Keys.IsIncludeRenotedMyNotes,
        Keys.IsIncludeMyRenotes,
        Keys.SurfaceColorOpacity,
        Keys.ThemeType,
        Keys.IsEnableTimelineScrollAnimation,
        Keys.IsCrashlyticsCollectionEnabled,
        Keys.IsConfirmedCrashlyticsCollection,
        Keys.IsConfirmedAnalyticsCollection,
        Keys.IsAnalyticsCollectionEnabled,
        Keys.IsConfirmedPostNotification,
        Keys.IsEnableInstanceTicker,
        Keys.IsDriveUsingGridView,
        Keys.IsEnableNotificationSound,
        Keys.IsStopStreamingApiWhenBackground,
        Keys.IsStopNoteCaptureWhenBackground,
        Keys.IsEnableStreamingAPIAndNoteCapture,
        Keys.IsEnableNoteDivider,
        Keys.IsVisibleInstanceUrlInToolbar,
        Keys.NoteHeaderFontSize,
        Keys.NoteContentFontSize,
        Keys.IsDisplayTimestampsAsAbsoluteDates,
        Keys.NoteReactionCounterFontSize,
        Keys.NoteCustomEmojiScaleSizeInText,
        Keys.EmojiPickerEmojiDisplaySize,
        Keys.AvatarIconShapeType,
        Keys.MediaDisplayMode,
        Keys.IsSafeSearchEnabled,
        Keys.IsConfirmedSafeSearchEnabled,
    )
}

sealed interface Keys {


    data object IsSimpleEditorEnabled : Keys

    data object ReactionPickerType : Keys

    data object BackgroundImage : Keys
    data object ClassicUI : Keys

    data object IsUserNameDefault : Keys

    data object IsPostButtonToBottom : Keys

    data object NoteLimitHeight : Keys


    data object ThemeType : Keys

    data object IsIncludeMyRenotes : Keys
    data object IsIncludeRenotedMyNotes : Keys
    data object IsIncludeLocalRenotes : Keys
    data object SurfaceColorOpacity : Keys
    data object IsEnableTimelineScrollAnimation : Keys
    data object IsCrashlyticsCollectionEnabled : Keys
    data object IsConfirmedCrashlyticsCollection : Keys

    data object IsAnalyticsCollectionEnabled : Keys
    data object IsConfirmedAnalyticsCollection : Keys

    data object IsConfirmedPostNotification : Keys

    data object IsEnableInstanceTicker : Keys

    data object IsDriveUsingGridView : Keys

    data object IsEnableNotificationSound : Keys

    data object IsStopStreamingApiWhenBackground : Keys

    data object IsStopNoteCaptureWhenBackground : Keys

    data object IsEnableStreamingAPIAndNoteCapture : Keys

    data object IsEnableNoteDivider: Keys

    data object IsVisibleInstanceUrlInToolbar : Keys

    data object NoteHeaderFontSize: Keys

    data object NoteContentFontSize: Keys

    data object IsDisplayTimestampsAsAbsoluteDates: Keys

    data object NoteReactionCounterFontSize : Keys

    data object NoteCustomEmojiScaleSizeInText : Keys

    data object EmojiPickerEmojiDisplaySize : Keys

    data object AvatarIconShapeType : Keys

    data object MediaDisplayMode : Keys

    data object IsSafeSearchEnabled : Keys

    data object IsConfirmedSafeSearchEnabled : Keys

    data object IsShowWarningDisplayingSensitiveMedia : Keys

    companion object
}

fun Keys.str(): String {
    return when (this) {
        is Keys.BackgroundImage -> "BackgroundImage"
        is Keys.ClassicUI -> "HIDE_BOTTOM_NAVIGATION"
        is Keys.IsPostButtonToBottom -> "IS_POST_BUTTON_TO_BOTTOM"
        is Keys.IsSimpleEditorEnabled -> "IS_SIMPLE_EDITOR_ENABLED"
        is Keys.IsUserNameDefault -> "IS_USER_NAME_DEFAULT"
        is Keys.NoteLimitHeight -> "HEIGHT"
        is Keys.ReactionPickerType -> "ReactionPickerType"
        is Keys.ThemeType -> "THEME"
        is Keys.IsIncludeMyRenotes -> "INCLUDE_MY_RENOTES"
        is Keys.IsIncludeLocalRenotes -> "INCLUDE_LOCAL_RENOTES"
        is Keys.IsIncludeRenotedMyNotes -> "INCLUDE_RENOTED_MY_NOTES"
        is Keys.SurfaceColorOpacity -> "jp.panta.misskeyandroidclient.model.settings.SURFACE_COLOR_OPAQUE_KEY"
        is Keys.IsEnableTimelineScrollAnimation -> "IS_ENABLE_TIMELINE_SCROLL_ANIMATION"
        is Keys.IsCrashlyticsCollectionEnabled -> "IsCrashlyticsCollectionEnabled"
        is Keys.IsConfirmedCrashlyticsCollection -> "IsConfirmedCrashlyticsCollection"
        is Keys.IsConfirmedAnalyticsCollection -> "IsConfirmedAnalyticsCollection"
        is Keys.IsAnalyticsCollectionEnabled -> "IsAnalyticsCollectionEnabled"
        is Keys.IsConfirmedPostNotification -> "IsConfirmedPostNotification"
        is Keys.IsEnableInstanceTicker -> "IsEnableInstanceTicker"
        is Keys.IsDriveUsingGridView -> "IsDriveUsingGridView"
        is Keys.IsEnableNotificationSound -> "IsEnableNotificationSound"
        is Keys.IsStopNoteCaptureWhenBackground -> "IsStopNoteCaptureWhenBackground"
        is Keys.IsStopStreamingApiWhenBackground -> "IsStopStreamingApiWhenBackground"
        is Keys.IsEnableStreamingAPIAndNoteCapture -> "IsEnableStreamingAPIAndNoteCapture"
        is Keys.IsEnableNoteDivider -> "IsEnableNoteDivider"
        is Keys.IsVisibleInstanceUrlInToolbar -> "IsVisibleInstanceUrlInToolbar"
        is Keys.NoteContentFontSize -> "NoteContentFontSize"
        is Keys.NoteHeaderFontSize -> "NoteHeaderFontSize"
        is Keys.IsDisplayTimestampsAsAbsoluteDates -> "IsDisplayTimestampsAsAbsoluteDates"
        is Keys.NoteReactionCounterFontSize -> "NoteReactionCounterFontSize"
        is Keys.NoteCustomEmojiScaleSizeInText -> "NoteCustomEmojiScaleSizeInText"
        is Keys.EmojiPickerEmojiDisplaySize -> "EmojiPickerEmojiDisplaySize"
        is Keys.AvatarIconShapeType -> "AvatarIconShapeType"
        is Keys.MediaDisplayMode -> "MediaDisplayMode"
        is Keys.IsSafeSearchEnabled -> "ExcludeIfExistsSensitiveMedia"
        is Keys.IsConfirmedSafeSearchEnabled -> "IsConfirmedSafeSearchEnabled"
        is Keys.IsShowWarningDisplayingSensitiveMedia -> "IsShowWarningDisplayingSensitiveMedia"
    }
}
