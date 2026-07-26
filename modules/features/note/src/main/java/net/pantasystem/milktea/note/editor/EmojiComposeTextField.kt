package net.pantasystem.milktea.note.editor

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import net.pantasystem.milktea.common.text.UrlPatternChecker
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.emoji.CustomEmoji
import net.pantasystem.milktea.model.emoji.CustomEmojiRepository
import net.pantasystem.milktea.note.editor.emoji.EmojiToken
import net.pantasystem.milktea.note.editor.emoji.EmojiTokenScanner
import net.pantasystem.milktea.note.editor.viewmodel.TextWithCursorPos

private const val SEARCH_DEBOUNCE_MS = 150L
private const val MAX_SUGGESTIONS = 20

/**
 * カスタム絵文字補完に対応した純粋 Compose のテキスト入力フィールド。
 *
 * 旧 EmojiAutoCompleteTextField（`MultiAutoCompleteTextView` の `AndroidView` ラッパー）の
 * 置き換えとして実装したもの。`TextFieldValue` で text と selection を一体管理し、
 * カーソル追跡・カーソル逆流・二重管理といった AndroidView 由来のハックを排する。
 *
 * まだ [NoteEditorTextInputSection] には接続していない（段階移行のため）。
 *
 * @param value 現在のテキスト（外部状態）
 * @param onValueChange テキスト変化時のコールバック
 * @param account 補完に使うアカウント。null の場合は補完なし
 * @param customEmojiRepository 補完候補の検索に使うリポジトリ
 * @param hint プレースホルダ
 * @param minLines 最小行数
 * @param onFocused フォーカスを得たときのコールバック
 * @param onCursorPositionChanged カーソル位置が変わったときのコールバック
 * @param onUrlPasted URL 貼り付けを検出したときのコールバック。null の場合は検出しない
 * @param textCursorPosFlow ViewModel から流れる「テキスト＋カーソル位置」の更新イベント。
 *   絵文字ピッカー・メンション挿入後にテキストを置き換えつつカーソルを挿入位置直後へ移動する
 * @param dismissSignal 値が変化するたびに補完ドロップダウンを一時的に閉じる（外側タップ等の合図）。
 *   フォーカス・IME は保持したままドロップダウンだけを隠す。次にトークンが変化すると再表示される
 * @param autoFocus true の場合、初回表示時にフォーカスを当てて IME を表示する
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EmojiComposeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    account: Account?,
    customEmojiRepository: CustomEmojiRepository,
    modifier: Modifier = Modifier,
    hint: String = "",
    minLines: Int = 1,
    singleLine: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onFocused: () -> Unit = {},
    onCursorPositionChanged: (Int) -> Unit = {},
    onUrlPasted: ((text: String, start: Int, beforeText: String, count: Int) -> Unit)? = null,
    textCursorPosFlow: SharedFlow<TextWithCursorPos>? = null,
    dismissSignal: Int = 0,
    autoFocus: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
) {
    // text + selection を一体で保持する内部状態
    var textFieldValue by remember { mutableStateOf(TextFieldValue(value, TextRange(value.length))) }

    // クロージャの最新参照を保持
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val currentOnUrlPasted by rememberUpdatedState(onUrlPasted)
    val currentOnCursorPositionChanged by rememberUpdatedState(onCursorPositionChanged)

    // 外部 value が内部 text と食い違った場合のみ同期する（外部からのテキスト差し替え）。
    // 自分の onValueChange 経由で戻ってくる value は既に一致しているためリセットされない。
    // NOTE: value 更新（カーソル末尾）と下の textCursorPosFlow（正しいカーソル位置）は
    //       どちらが先に走っても、この「text が異なる場合のみ」ガードにより最終的に
    //       正しいカーソル位置へ収束する（旧 AndroidView 実装の update ブロックと同じ仕組み）。
    LaunchedEffect(value) {
        if (value != textFieldValue.text) {
            textFieldValue = TextFieldValue(value, TextRange(value.length))
        }
    }

    // ViewModel からの「テキスト＋カーソル位置」更新（絵文字ピッカー・メンション挿入）を反映する。
    LaunchedEffect(textCursorPosFlow) {
        textCursorPosFlow?.collect { data ->
            val newText = data.text ?: ""
            val pos = data.cursorPos.coerceIn(0, newText.length)
            textFieldValue = TextFieldValue(newText, TextRange(pos))
            currentOnCursorPositionChanged(pos)
        }
    }

    // 補完候補
    var suggestions by remember { mutableStateOf<List<CustomEmoji>>(emptyList()) }

    // フィールドがフォーカスを持っているか。候補ドロップダウンの表示条件に使う。
    // フォーカスを失ったら（別フィールドへ移動）候補を隠す。
    // フォーカス状態は入力中に切り替わらないためフリッカーしない。
    var isFocused by remember { mutableStateOf(false) }

    // 外側タップ等の合図で一時的にドロップダウンを閉じる。フォーカス・IME は保持したまま隠す。
    // 次にトークン（query）が変化したら解除して再表示できるようにする。
    var suppressed by remember { mutableStateOf(false) }

    // カーソル直前のトークン（選択範囲が無く、query が空でないときのみ有効）
    val currentToken: EmojiToken? = remember(textFieldValue) {
        if (!textFieldValue.selection.collapsed) {
            null
        } else {
            EmojiTokenScanner.findCurrentToken(textFieldValue.text, textFieldValue.selection.end)
                ?.takeIf { it.query.isNotEmpty() }
        }
    }

    // トークンに応じて候補を非同期検索（LaunchedEffect のキャンセルで debounce + 最新優先）
    LaunchedEffect(currentToken?.query, account?.accountId) {
        val query = currentToken?.query
        if (query.isNullOrEmpty() || account == null) {
            suggestions = emptyList()
            return@LaunchedEffect
        }
        delay(SEARCH_DEBOUNCE_MS)
        suggestions = customEmojiRepository.search(account.getHost(), query)
            .getOrElse { emptyList() }
            .take(MAX_SUGGESTIONS)
    }

    // 外側タップ等の合図でドロップダウンを一時的に閉じる（初期値 0 のときは何もしない）
    LaunchedEffect(dismissSignal) {
        if (dismissSignal != 0) suppressed = true
    }
    // トークン（query）が変化したら抑制を解除して再表示できるようにする
    LaunchedEffect(currentToken?.query) {
        suppressed = false
    }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // 初回表示時の auto-focus + IME 表示
    LaunchedEffect(autoFocus) {
        if (autoFocus) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    val handleChange: (TextFieldValue) -> Unit = handleChange@{ new ->
        val old = textFieldValue
        if (new.text != old.text) {
            currentOnValueChange(new.text)
            // URL 貼り付け検出：挿入された領域を新旧テキストの差分から求める
            val onUrlPastedCb = currentOnUrlPasted
            if (onUrlPastedCb != null) {
                val inserted = computeInsertedRegion(old.text, new.text)
                if (inserted != null && UrlPatternChecker.isMatch(inserted.text)) {
                    onUrlPastedCb(new.text, inserted.start, old.text, inserted.text.length)
                }
            }
        }
        textFieldValue = new
        currentOnCursorPositionChanged(new.selection.end)
    }

    val applySuggestion: (CustomEmoji) -> Unit = { emoji ->
        val token = currentToken
        if (token != null) {
            val replacement = ":${emoji.name}:"
            val text = textFieldValue.text
            val newText = text.substring(0, token.start) + replacement + text.substring(token.end)
            val newCursor = token.start + replacement.length
            textFieldValue = TextFieldValue(newText, TextRange(newCursor))
            currentOnValueChange(newText)
            currentOnCursorPositionChanged(newCursor)
            suggestions = emptyList()
        }
    }

    Box(modifier = modifier) {
        NoteEditorTextField(
            value = textFieldValue,
            onValueChange = handleChange,
            modifier = Modifier.fillMaxWidth(),
            hint = hint,
            minLines = minLines,
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            focusRequester = focusRequester,
            onFocusChanged = { focused ->
                isFocused = focused
                if (focused) onFocused()
            },
            textStyle = textStyle,
        )

        if (isFocused && !suppressed && suggestions.isNotEmpty()) {
            EmojiSuggestionPopup(
                suggestions = suggestions,
                accountHost = account?.getHost(),
                onSelected = applySuggestion,
            )
        }
    }
}

/**
 * テキストフィールドの下に候補リストを表示する Popup。
 * フォーカスを奪わない（IME・入力欄のフォーカスを維持する）。
 */
@Composable
private fun EmojiSuggestionPopup(
    suggestions: List<CustomEmoji>,
    accountHost: String?,
    onSelected: (CustomEmoji) -> Unit,
) {
    val positionProvider = remember {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize,
            ): IntOffset {
                val x = anchorBounds.left
                // 下に十分な余白があれば下、無ければフィールドの上に出す
                val y = if (anchorBounds.bottom + popupContentSize.height <= windowSize.height) {
                    anchorBounds.bottom
                } else {
                    (anchorBounds.top - popupContentSize.height).coerceAtLeast(0)
                }
                return IntOffset(x, y)
            }
        }
    }

    Popup(
        popupPositionProvider = positionProvider,
        properties = PopupProperties(focusable = false),
    ) {
        Surface(
            tonalElevation = 3.dp,
            shadowElevation = 3.dp,
            color = MaterialTheme.colorScheme.surface,
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 220.dp),
            ) {
                items(suggestions) { emoji ->
                    EmojiSuggestionRow(
                        emoji = emoji,
                        accountHost = accountHost,
                        onClick = { onSelected(emoji) },
                    )
                }
            }
        }
    }
}

@Composable
private fun EmojiSuggestionRow(
    emoji: CustomEmoji,
    accountHost: String?,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = emoji.url ?: emoji.uri),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = ":${emoji.name}:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * 旧テキストと新テキストを比較し、挿入された領域（開始位置と挿入文字列）を返す。
 * 新テキストが旧より長い場合のみ有効。共通の接頭辞・接尾辞を除いた中間を挿入分とみなす。
 */
internal data class InsertedRegion(val start: Int, val text: String)

internal fun computeInsertedRegion(old: String, new: String): InsertedRegion? {
    if (new.length <= old.length) return null

    var start = 0
    val minLen = minOf(old.length, new.length)
    while (start < minLen && old[start] == new[start]) {
        start++
    }

    var oldEnd = old.length
    var newEnd = new.length
    while (oldEnd > start && newEnd > start && old[oldEnd - 1] == new[newEnd - 1]) {
        oldEnd--
        newEnd--
    }

    if (newEnd <= start) return null
    return InsertedRegion(start = start, text = new.substring(start, newEnd))
}
