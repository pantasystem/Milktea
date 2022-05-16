package net.pantasystem.milktea.model.setting

import net.pantasystem.milktea.model.notes.Visibility

data class UrlPreviewConfig(
    val type: Type = Type.Misskey
) {
    sealed interface Type {
        object Misskey : Type
        object InApp : Type
        data class SummalyServer(val url: String) : Type
    }
}


sealed interface RememberVisibility {
    object None : RememberVisibility
    data class Remember(val visibility: Visibility, val accountId: Long) : RememberVisibility
}

data class Config(
    val isSimpleEditorEnabled: Boolean,
    val reactionPickerType: ReactionPickerType,
    val backgroundImagePath: String?,
    val isClassicUI: Boolean,
    val isUserNameDefault: Boolean,
    val isPostButtonAtTheBottom: Boolean,
    val urlPreviewConfig: UrlPreviewConfig,
    val noteExpandedHeightSize: Int,
    val theme: Theme
) {
    companion object
}

object DefaultConfig {
    val config = Config(
        isSimpleEditorEnabled = false,
        reactionPickerType = ReactionPickerType.LIST,
        backgroundImagePath = null,
        isClassicUI = false,
        isUserNameDefault = true,
        isPostButtonAtTheBottom = true,
        urlPreviewConfig = UrlPreviewConfig(UrlPreviewConfig.Type.Misskey),
        noteExpandedHeightSize = 300,
        theme = Theme.White
    )
}

enum class ReactionPickerType {
    LIST,
    SIMPLE
}