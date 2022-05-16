package net.pantasystem.milktea.data.infrastructure.settings

import org.junit.Assert
import org.junit.Test

class KeysKtTest {

    @Test
    fun str() {
        Keys.allKeys.forEach { key ->
            when (key) {
                Keys.BackgroundImage -> Assert.assertEquals("BackgroundImage", key.str())
                Keys.ClassicUI -> Assert.assertEquals(
                    "HIDE_BOTTOM_NAVIGATION",
                    key.str()
                )
                Keys.IsPostButtonToBottom -> Assert.assertEquals(
                    "IS_POST_BUTTON_TO_BOTTOM",
                    key.str()
                )
                Keys.IsSimpleEditorEnabled -> Assert.assertEquals(
                    "IS_SIMPLE_EDITOR_ENABLED",
                    key.str()
                )
                Keys.IsUserNameDefault -> Assert.assertEquals(
                    "IS_USER_NAME_DEFAULT",
                    key.str()
                )
                Keys.NoteLimitHeight -> Assert.assertEquals(
                    "HEIGHT",
                    key.str()
                )
                Keys.ReactionPickerType -> Assert.assertEquals("ReactionPickerType", key.str())
                Keys.SummalyServerUrl -> Assert.assertEquals(
                    "jp.panta.misskeyandroidclient.model.settings.SUMMALY_SERVER_URL_KEY",
                    key.str()
                )
                Keys.ThemeType -> Assert.assertEquals("THEME", key.str())
                Keys.UrlPreviewSourceType -> Assert.assertEquals(
                    "jp.panta.misskeyandroidclient.model.settings.URL_PREVIEW_SOURCE_TYPE",
                    key.str()
                )
            }
        }
    }
}