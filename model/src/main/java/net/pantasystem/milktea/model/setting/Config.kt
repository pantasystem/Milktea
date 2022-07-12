package net.pantasystem.milktea.model.setting

import net.pantasystem.milktea.model.notes.Visibility

val urlPattern = Regex("""(https)(://)([-_.!~*'()\[\]a-zA-Z0-9;/?:@&=+${'$'},%#]+)""")

data class UrlPreviewConfig(
    val type: Type = Type.Misskey
) {
    sealed interface Type {
        object Misskey : Type
        object InApp : Type
        data class SummalyServer(val url: String) : Type

        companion object
    }
}


sealed interface RememberVisibility {
    object None : RememberVisibility
    data class Remember(val visibility: Visibility, val accountId: Long) : RememberVisibility
    sealed interface Keys {
        object IsRememberNoteVisibility : Keys
        data class NoteVisibility(val accountId: Long) : Keys
        data class IsLocalOnly(val accountId: Long) : Keys
    }
}

/**
 * @param isEnable クラシュリティクスによる情報収集が有効
 * @param isConfirmed 情報収集の確認済み(可否は関係ない)
 */
data class IsCrashlyticsCollectionEnabled(
    val isEnable: Boolean,
    val isConfirmed: Boolean
)

data class Config(
    val isSimpleEditorEnabled: Boolean,
    val reactionPickerType: ReactionPickerType,
    val backgroundImagePath: String?,
    val isClassicUI: Boolean,
    val isUserNameDefault: Boolean,
    val isPostButtonAtTheBottom: Boolean,
    val urlPreviewConfig: UrlPreviewConfig,
    val noteExpandedHeightSize: Int,
    val theme: Theme,
    val isIncludeMyRenotes: Boolean,
    val isIncludeRenotedMyNotes: Boolean,
    val isIncludeLocalRenotes: Boolean,
    val surfaceColorOpacity: Int,
    val isEnableTimelineScrollAnimation: Boolean,
    val isCrashlyticsCollectionEnabled: IsCrashlyticsCollectionEnabled,
) {
    companion object

    fun setCrashlyticsCollectionEnabled(enabled: Boolean): Config {
        return copy(
            isCrashlyticsCollectionEnabled = isCrashlyticsCollectionEnabled.copy(
                isEnable = enabled,
                isConfirmed = true
            )
        )
    }
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
        theme = Theme.White,
        isIncludeLocalRenotes = true,
        isIncludeMyRenotes = true,
        isIncludeRenotedMyNotes = true,
        surfaceColorOpacity = 0xff,
        isEnableTimelineScrollAnimation = false,
        isCrashlyticsCollectionEnabled = IsCrashlyticsCollectionEnabled(
            isConfirmed = false,
            isEnable = false,
        ),
    )

    fun getRememberVisibilityConfig(accountId: Long): RememberVisibility.Remember {
        return RememberVisibility.Remember(
            accountId = accountId,
            visibility = Visibility.Public(false)
        )
    }
}

enum class ReactionPickerType {
    LIST,
    SIMPLE
}