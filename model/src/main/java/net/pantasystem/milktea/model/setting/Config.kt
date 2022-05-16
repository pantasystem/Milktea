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

sealed interface Theme {
    object White : Theme
    object Black : Theme
    object Dark : Theme
    object Bread : Theme
}

sealed interface RememberVisibility {
    object None : RememberVisibility
    data class Remember(val visibility: Visibility) : RememberVisibility
}
data class Config(
    val isSimpleEditorEnabled: Boolean = false,
    val reactionPickerType: ReactionPickerType = ReactionPickerType.LIST,
    val backgroundImagePath: String? = null,
    val isClassicUI: Boolean = false,
    val isUserNameDefault: Boolean = true,
    val isPostButtonAtTheBottom: Boolean = true,
    val urlPreviewConfig: UrlPreviewConfig,
    val noteExpandedHeightSize: Int = 300,
    val rememberVisibility: RememberVisibility = RememberVisibility.Remember(Visibility.Public(false)),
    val theme: Theme = Theme.White
)

enum class ReactionPickerType {
    LIST,
    SIMPLE
}