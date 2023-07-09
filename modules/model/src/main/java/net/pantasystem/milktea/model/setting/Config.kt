package net.pantasystem.milktea.model.setting

import net.pantasystem.milktea.model.notes.Visibility




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

data class IsAnalyticsCollectionEnabled(
    val isEnabled: Boolean,
    val isConfirmed: Boolean,
)

/**
 * @param isSimpleEditorEnabled シンプルエディターを使用するのか
 * @param reactionPickerType リアクションピッカーの種別　
 * @param backgroundImagePath 背景画像のパス
 * @param isClassicUI BottomNavを使用しないタイプのUI
 * @param isUserNameDefault User@usernameを主体とする表示
 * @param isPostButtonAtTheBottom ノート編集画面の投稿ボタンを下に持ってくる
 * @param noteExpandedHeightSize ノートを強制的に折り畳むサイズ
 * @param theme テーマカラー
 * @param isCrashlyticsCollectionEnabled クラシュリティクスの許可状態
 * @param isAnalyticsCollectionEnabled アナリティクスの許可状態
 * @param isConfirmedPostNotification プッシュ通知の顕現許可の状態
 * @param isEnableInstanceTicker Instance Tickerの有無
 * @param isDriveUsingGridView 一覧表示時にグリッド表示を行うのか
 * @param isEnableNotificationSound アプリ内通知の通知音の有無
 * @param isStopNoteCaptureWhenBackground バックグラウンド移行時にノートのキャプチャーを停止する
 * @param isStopStreamingApiWhenBackground バックグルアンド移行時にストリーミングを中止する
 * @param isEnableStreamingAPIAndNoteCapture 自動更新のON/OFF
 * @Param isEnableNoteDivider ノートの区切り線の有無 trueで有り falseで無し
 * @param isVisibleInstanceUrlInToolbar toolbar内にインスタンス情報を表示するか？
 * @param isHideMediaWhenMobileNetwork モバイルネットワークの時はメディアを表示しない
 * @param noteHeaderFontSize ノートのヘッダー部分のテキストサイズ
 * @param noteContentFontSize ノートのコンテンツ部分のテキストサイズ
 * @param noteReactionCounterFontSize ノートのリアクションカウンターのカスタム絵文字、絵文字と件数表示のフォントサイズ
 * @param emojiPickerEmojiDisplaySize 絵文字ピッカーの絵文字の表示サイズ
 */
data class Config(
    val isSimpleEditorEnabled: Boolean,
    val reactionPickerType: ReactionPickerType,
    val backgroundImagePath: String?,
    val isClassicUI: Boolean,
    val isUserNameDefault: Boolean,
    val isPostButtonAtTheBottom: Boolean,
    val noteExpandedHeightSize: Int,
    val theme: Theme,
    val isIncludeMyRenotes: Boolean,
    val isIncludeRenotedMyNotes: Boolean,
    val isIncludeLocalRenotes: Boolean,
    val surfaceColorOpacity: Int,
    val isEnableTimelineScrollAnimation: Boolean,
    val isCrashlyticsCollectionEnabled: IsCrashlyticsCollectionEnabled,
    val isAnalyticsCollectionEnabled: IsAnalyticsCollectionEnabled,
    val isConfirmedPostNotification: Boolean,
    val isEnableInstanceTicker: Boolean,
    val isDriveUsingGridView: Boolean,
    val isEnableNotificationSound: Boolean,
    val isStopStreamingApiWhenBackground: Boolean,
    val isStopNoteCaptureWhenBackground: Boolean,
    val isEnableStreamingAPIAndNoteCapture: Boolean,
    val isEnableNoteDivider: Boolean,
    val isVisibleInstanceUrlInToolbar: Boolean,
    val isHideMediaWhenMobileNetwork: Boolean,
    val noteHeaderFontSize: Float,
    val noteContentFontSize: Float,
    val isDisplayTimestampsAsAbsoluteDates: Boolean,
    val noteReactionCounterFontSize: Float,
    val noteCustomEmojiScaleSizeInText: Float,
    val emojiPickerEmojiDisplaySize: Int,
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

    fun setAnalyticsCollectionEnabled(enabled: Boolean): Config {
        return copy(
            isAnalyticsCollectionEnabled = isAnalyticsCollectionEnabled.copy(
                isEnabled = enabled,
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
        isAnalyticsCollectionEnabled = IsAnalyticsCollectionEnabled(
            isConfirmed = false,
            isEnabled = false,
        ),
        isConfirmedPostNotification = false,
        isEnableInstanceTicker = true,
        isDriveUsingGridView = false,
        isEnableNotificationSound = true,
        isStopNoteCaptureWhenBackground = true,
        isStopStreamingApiWhenBackground = true,
        isEnableStreamingAPIAndNoteCapture = true,
        isEnableNoteDivider = true,
        isVisibleInstanceUrlInToolbar = true,
        isHideMediaWhenMobileNetwork = false,
        noteContentFontSize = 15f,
        noteHeaderFontSize = 15f,
        isDisplayTimestampsAsAbsoluteDates = false,
        noteReactionCounterFontSize = 15f,
        noteCustomEmojiScaleSizeInText = 1.2f,
        emojiPickerEmojiDisplaySize = 28,
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