package net.pantasystem.milktea.note.media.viewmodel

import net.pantasystem.milktea.model.drive.FileProperty
import net.pantasystem.milktea.model.drive.make
import net.pantasystem.milktea.model.file.AppFile
import net.pantasystem.milktea.model.file.FilePreviewSource
import net.pantasystem.milktea.model.setting.DefaultConfig
import net.pantasystem.milktea.model.setting.MediaDisplayMode
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class MediaViewDataTest {

    @Test
    fun initialFiles_GiveAlwaysShow() {

        val config = DefaultConfig.config.copy(
            mediaDisplayMode = MediaDisplayMode.ALWAYS_SHOW
        )
        val files = listOf(
            FilePreviewSource.Remote(
                AppFile.Remote(id = FileProperty.Id(0L, "1")),
                FileProperty.make(id = FileProperty.Id(0L, "1")),
            ),
            FilePreviewSource.Remote(
                AppFile.Remote(id = FileProperty.Id(0L, "1")),
                FileProperty.make(id = FileProperty.Id(0L, "1")),
            ),
            FilePreviewSource.Remote(
                AppFile.Remote(id = FileProperty.Id(0L, "1")),
                FileProperty.make(id = FileProperty.Id(0L, "1"), isSensitive = true),
            ),
            FilePreviewSource.Remote(
                AppFile.Remote(id = FileProperty.Id(0L, "1")),
                FileProperty.make(id = FileProperty.Id(0L, "1")),
            )
        )
        val value = MediaViewData(files, config).files.value
        Assertions.assertEquals(
            listOf(
                PreviewAbleFile.VisibleType.Visible,
                PreviewAbleFile.VisibleType.Visible,
                PreviewAbleFile.VisibleType.Visible,
                PreviewAbleFile.VisibleType.Visible
            ),
            value.map {
                it.initialVisibleType
            }
        )
    }

    /**
     * configのmediaDisplayModeがMediaDisplayMode.ALWAYS_HIDEの場合のテストコード
     */
    @Test
    fun initialFiles_GiveAlwaysHide() {

        val config = DefaultConfig.config.copy(
            mediaDisplayMode = MediaDisplayMode.ALWAYS_HIDE
        )
        val files = listOf(
            FilePreviewSource.Remote(
                AppFile.Remote(id = FileProperty.Id(0L, "1")),
                FileProperty.make(id = FileProperty.Id(0L, "1")),
            ),
            FilePreviewSource.Remote(
                AppFile.Remote(id = FileProperty.Id(0L, "1")),
                FileProperty.make(id = FileProperty.Id(0L, "1")),
            ),
            FilePreviewSource.Remote(
                AppFile.Remote(id = FileProperty.Id(0L, "1")),
                FileProperty.make(id = FileProperty.Id(0L, "1"), isSensitive = true),
            ),
            FilePreviewSource.Remote(
                AppFile.Remote(id = FileProperty.Id(0L, "1")),
                FileProperty.make(id = FileProperty.Id(0L, "1")),
            )
        )
        val value = MediaViewData(files, config).files.value
        Assertions.assertEquals(
            listOf(
                PreviewAbleFile.VisibleType.HideWhenMobileNetwork,
                PreviewAbleFile.VisibleType.HideWhenMobileNetwork,
                PreviewAbleFile.VisibleType.SensitiveHide,
                PreviewAbleFile.VisibleType.HideWhenMobileNetwork
            ),
            value.map {
                it.initialVisibleType
            }
        )
    }

    /**
     * configのmediaDisplayModeがMediaDisplayMode.ALWAYS_HIDE_WHEN_MOBILE_NETWORKの場合のテストコード
     */
    @Test
    fun initialFiles_GiveAlwaysHideWhenMobileNetwork() {

        val config = DefaultConfig.config.copy(
            mediaDisplayMode = MediaDisplayMode.ALWAYS_HIDE_WHEN_MOBILE_NETWORK
        )
        val files = listOf(
            FilePreviewSource.Remote(
                AppFile.Remote(id = FileProperty.Id(0L, "1")),
                FileProperty.make(id = FileProperty.Id(0L, "1")),
            ),
            FilePreviewSource.Remote(
                AppFile.Remote(id = FileProperty.Id(0L, "1")),
                FileProperty.make(id = FileProperty.Id(0L, "1")),
            ),
            FilePreviewSource.Remote(
                AppFile.Remote(id = FileProperty.Id(0L, "1")),
                FileProperty.make(id = FileProperty.Id(0L, "1"), isSensitive = true),
            ),
            FilePreviewSource.Remote(
                AppFile.Remote(id = FileProperty.Id(0L, "1")),
                FileProperty.make(id = FileProperty.Id(0L, "1")),
            )
        )
        val value = MediaViewData(files, config).files.value
        Assertions.assertEquals(
            listOf(
                PreviewAbleFile.VisibleType.HideWhenMobileNetwork,
                PreviewAbleFile.VisibleType.HideWhenMobileNetwork,
                PreviewAbleFile.VisibleType.SensitiveHide,
                PreviewAbleFile.VisibleType.HideWhenMobileNetwork
            ),
            value.map {
                it.initialVisibleType
            }
        )
    }

    /**
     * configのmediaDisplayModeがMediaDisplayMode.AUTOの場合のテストコード
     */
    @Test
    fun initialFiles_GiveAuto() {

        val config = DefaultConfig.config.copy(
            mediaDisplayMode = MediaDisplayMode.AUTO
        )
        val files = listOf(
            FilePreviewSource.Remote(
                AppFile.Remote(id = FileProperty.Id(0L, "1")),
                FileProperty.make(id = FileProperty.Id(0L, "1")),
            ),
            FilePreviewSource.Remote(
                AppFile.Remote(id = FileProperty.Id(0L, "1")),
                FileProperty.make(id = FileProperty.Id(0L, "1")),
            ),
            FilePreviewSource.Remote(
                AppFile.Remote(id = FileProperty.Id(0L, "1")),
                FileProperty.make(id = FileProperty.Id(0L, "1"), isSensitive = true),
            ),
            FilePreviewSource.Remote(
                AppFile.Remote(id = FileProperty.Id(0L, "1")),
                FileProperty.make(id = FileProperty.Id(0L, "1")),
            )
        )
        val value = MediaViewData(files, config).files.value
        Assertions.assertEquals(
            listOf(
                PreviewAbleFile.VisibleType.Visible,
                PreviewAbleFile.VisibleType.Visible,
                PreviewAbleFile.VisibleType.SensitiveHide,
                PreviewAbleFile.VisibleType.Visible
            ),
            value.map { it.initialVisibleType }
        )
    }
}