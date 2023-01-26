package net.pantasystem.milktea.data.infrastructure.settings

import net.pantasystem.milktea.model.setting.Keys
import net.pantasystem.milktea.model.setting.allKeys
import net.pantasystem.milktea.model.setting.str
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class KeysKtTest {

    @Test
    fun str() {
        Keys.allKeys.forEach { key ->
            when (key) {
                Keys.BackgroundImage -> Assertions.assertEquals("BackgroundImage", key.str())
                Keys.ClassicUI -> Assertions.assertEquals("HIDE_BOTTOM_NAVIGATION", key.str())
                Keys.IsPostButtonToBottom -> Assertions.assertEquals(
                    "IS_POST_BUTTON_TO_BOTTOM",
                    key.str()
                )
                Keys.IsSimpleEditorEnabled -> Assertions.assertEquals(
                    "IS_SIMPLE_EDITOR_ENABLED",
                    key.str()
                )
                Keys.IsUserNameDefault -> Assertions.assertEquals("IS_USER_NAME_DEFAULT", key.str())
                Keys.NoteLimitHeight -> Assertions.assertEquals("HEIGHT", key.str())
                Keys.ReactionPickerType -> Assertions.assertEquals("ReactionPickerType", key.str())
                Keys.ThemeType -> Assertions.assertEquals("THEME", key.str())

                Keys.IsIncludeLocalRenotes -> Assertions.assertEquals(
                    "INCLUDE_LOCAL_RENOTES",
                    key.str()
                )
                Keys.IsIncludeMyRenotes -> Assertions.assertEquals("INCLUDE_MY_RENOTES", key.str())
                Keys.IsIncludeRenotedMyNotes -> Assertions.assertEquals(
                    "INCLUDE_RENOTED_MY_NOTES",
                    key.str()
                )
                Keys.SurfaceColorOpacity -> Assertions.assertEquals(
                    "jp.panta.misskeyandroidclient.model.settings.SURFACE_COLOR_OPAQUE_KEY",
                    key.str()
                )
                Keys.IsEnableTimelineScrollAnimation -> Assertions.assertEquals(
                    "IS_ENABLE_TIMELINE_SCROLL_ANIMATION",
                    key.str()
                )
                Keys.IsCrashlyticsCollectionEnabled -> Assertions.assertEquals(
                    "IsCrashlyticsCollectionEnabled",
                    key.str()
                )
                Keys.IsConfirmedCrashlyticsCollection -> Assertions.assertEquals(
                    "IsConfirmedCrashlyticsCollection",
                    key.str()
                )
                Keys.IsAnalyticsCollectionEnabled -> Assertions.assertEquals(
                    "IsAnalyticsCollectionEnabled",
                    key.str()
                )
                Keys.IsConfirmedAnalyticsCollection -> Assertions.assertEquals(
                    "IsConfirmedAnalyticsCollection",
                    key.str()
                )
                Keys.IsConfirmedPostNotification -> Assertions.assertEquals(
                    "IsConfirmedPostNotification",
                    key.str()
                )
                Keys.IsEnableInstanceTicker -> Assertions.assertEquals(
                    "IsEnableInstanceTicker",
                    key.str()
                )
                Keys.IsDriveUsingGridView -> Assertions.assertEquals(
                    "IsDriveUsingGridView",
                    key.str()
                )
                Keys.IsEnableNotificationSound -> Assertions.assertEquals(
                    "IsEnableNotificationSound",
                    key.str()
                )
                Keys.IsStopNoteCaptureWhenBackground -> Assertions.assertEquals(
                    "IsStopNoteCaptureWhenBackground",
                    key.str()
                )
                Keys.IsStopStreamingApiWhenBackground -> Assertions.assertEquals(
                    "IsStopStreamingApiWhenBackground",
                    key.str()
                )
            }
        }
    }


    @Test
    fun checkAllKeysCount() {
        Assertions.assertEquals(23, Keys.allKeys.size)
        Assertions.assertEquals(23, Keys.allKeys.map { it.str() }.toSet().size)
    }


}