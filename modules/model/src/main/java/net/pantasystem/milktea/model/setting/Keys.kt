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
    )
}

sealed interface Keys {


    object IsSimpleEditorEnabled : Keys

    object ReactionPickerType : Keys

    object BackgroundImage : Keys
    object ClassicUI : Keys

    object IsUserNameDefault : Keys

    object IsPostButtonToBottom : Keys

    object NoteLimitHeight : Keys


    object ThemeType : Keys

    object IsIncludeMyRenotes : Keys
    object IsIncludeRenotedMyNotes : Keys
    object IsIncludeLocalRenotes : Keys
    object SurfaceColorOpacity : Keys
    object IsEnableTimelineScrollAnimation : Keys
    object IsCrashlyticsCollectionEnabled : Keys
    object IsConfirmedCrashlyticsCollection : Keys

    object IsAnalyticsCollectionEnabled : Keys
    object IsConfirmedAnalyticsCollection : Keys

    object IsConfirmedPostNotification : Keys

    object IsEnableInstanceTicker : Keys

    object IsDriveUsingGridView : Keys

    object IsEnableNotificationSound : Keys

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
    }
}
