package net.pantasystem.milktea.data.infrastructure.settings

import net.pantasystem.milktea.model.setting.DefaultConfig
import net.pantasystem.milktea.model.setting.UrlPreviewConfig
import org.junit.Assert
import org.junit.Test

class ConfigKtTest {


    @Test
    fun prefs() {
        val config = DefaultConfig.config
        config.prefs().forEach { (k, u) ->
            when (k) {
                Keys.BackgroundImage -> Assert.assertEquals(
                    config.backgroundImagePath,
                    (u as PrefType.StrPref).value
                )
                Keys.ClassicUI -> Assert.assertEquals(
                    config.isClassicUI,
                    (u as PrefType.BoolPref).value
                )
                Keys.IsPostButtonToBottom -> Assert.assertEquals(
                    config.isPostButtonAtTheBottom,
                    (u as PrefType.BoolPref).value
                )
                Keys.IsSimpleEditorEnabled -> Assert.assertEquals(
                    config.isSimpleEditorEnabled,
                    (u as PrefType.BoolPref).value
                )
                Keys.IsUserNameDefault -> Assert.assertEquals(
                    config.isUserNameDefault,
                    (u as PrefType.BoolPref).value
                )
                Keys.NoteLimitHeight -> Assert.assertEquals(
                    config.noteExpandedHeightSize,
                    (u as PrefType.IntPref).value
                )
                Keys.ReactionPickerType -> Assert.assertEquals(
                    config.reactionPickerType.ordinal,
                    (u as PrefType.IntPref).value
                )
                Keys.SummalyServerUrl -> Assert.assertEquals(
                    (config.urlPreviewConfig.type as UrlPreviewConfig.Type.SummalyServer?)?.url,
                    (u as PrefType.StrPref).value
                )
                Keys.ThemeType -> Assert.assertEquals(
                    (config.theme.toInt()), (u as PrefType.IntPref).value
                )
                Keys.UrlPreviewSourceType -> Assert.assertEquals(
                    config.urlPreviewConfig.type.toInt(),
                    (u as PrefType.IntPref).value
                )
                Keys.IsIncludeLocalRenotes -> Assert.assertEquals(
                    config.isIncludeLocalRenotes, (u as PrefType.BoolPref).value
                )
                Keys.IsIncludeMyRenotes -> Assert.assertEquals(
                    config.isIncludeMyRenotes, (u as PrefType.BoolPref).value
                )
                Keys.IsIncludeRenotedMyNotes -> Assert.assertEquals(
                    config.isIncludeRenotedMyNotes, (u as PrefType.BoolPref).value
                )
            }
        }
    }
}