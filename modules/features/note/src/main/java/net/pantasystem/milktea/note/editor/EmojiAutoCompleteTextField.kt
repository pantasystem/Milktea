package net.pantasystem.milktea.note.editor

import android.content.Context
import android.text.InputType
import android.view.inputmethod.InputMethodManager
import android.widget.MultiAutoCompleteTextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.addTextChangedListener
import kotlinx.coroutines.flow.SharedFlow
import net.pantasystem.milktea.common_android.ui.text.CustomEmojiTokenizer
import net.pantasystem.milktea.common.text.UrlPatternChecker
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.emoji.CustomEmojiRepository
import net.pantasystem.milktea.note.editor.viewmodel.TextWithCursorPos

/**
 * カスタム絵文字補完対応のテキスト入力フィールド。
 *
 * MultiAutoCompleteTextView を AndroidView でラップし、既存の CustomEmojiCompleteAdapter +
 * CustomEmojiTokenizer をそのまま活用する。
 *
 * NOTE: 将来的には BasicTextField + カスタムドロップダウンに置き換え可能だが、
 *       初期移行ではこの AndroidView ブリッジを使う。
 *
 * @param value 現在のテキスト（Compose 側の状態）
 * @param onValueChange テキストが変化したときのコールバック
 * @param onFocused このフィールドがフォーカスを得たときのコールバック
 * @param account 絵文字補完に使うアカウント。null の場合は補完なし
 * @param customEmojiRepository 絵文字補完用リポジトリ
 * @param hint プレースホルダーテキスト
 * @param minLines 最小行数
 * @param inputType android.text.InputType の値
 * @param textCursorPosFlow ViewModel から流れてくるカーソル位置更新イベント（絵文字挿入後など）
 * @param onUrlPasted URL が貼り付けられたときのコールバック。null の場合は検出しない
 * @param onCursorPositionChanged テキスト変化後のカーソル位置を通知するコールバック。
 *   絵文字ピッカーから挿入位置を決める際に使う
 * @param autoFocus true の場合、画面表示時にこのフィールドへフォーカスを当てて IME を表示する
 */
@Composable
fun EmojiAutoCompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onFocused: () -> Unit,
    account: Account?,
    customEmojiRepository: CustomEmojiRepository,
    modifier: Modifier = Modifier,
    hint: String = "",
    minLines: Int = 1,
    inputType: Int = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE,
    textCursorPosFlow: SharedFlow<TextWithCursorPos>? = null,
    onUrlPasted: ((text: String, start: Int, beforeText: String, count: Int) -> Unit)? = null,
    onCursorPositionChanged: (Int) -> Unit = {},
    autoFocus: Boolean = false,
) {
    // MultiAutoCompleteTextView への参照を保持して LaunchedEffect からアクセスできるようにする
    val viewRef = remember { mutableStateOf<MultiAutoCompleteTextView?>(null) }

    // アダプターの再セットが必要かどうかを判定するために前回のアカウント ID を記憶する
    val lastAdapterAccountId = remember { mutableStateOf<Long?>(null) }

    // onValueChange / onFocused は最新の参照を保持（クロージャのキャプチャ問題を防ぐ）
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val currentOnFocused by rememberUpdatedState(onFocused)
    val currentOnUrlPasted by rememberUpdatedState(onUrlPasted)
    val currentOnCursorPositionChanged by rememberUpdatedState(onCursorPositionChanged)

    // ViewModel から流れるカーソル位置更新を監視し、View に反映する
    LaunchedEffect(textCursorPosFlow) {
        textCursorPosFlow?.collect { cursorData ->
            val view = viewRef.value ?: return@collect
            try {
                val newText = cursorData.text ?: ""
                // text を更新しつつカーソル位置を設定する
                // テキスト変更リスナーが反応しないよう setText の前後で一時的に抑制はしない
                // （onValueChange のループ防止は update ブロック側で行う）
                view.setText(newText)
                val safePos = cursorData.cursorPos.coerceIn(0, newText.length)
                view.setSelection(safePos)
            } catch (_: Exception) {
                // テキストが空などの境界ケースでクラッシュしないよう握りつぶす
            }
        }
    }

    AndroidView(
        factory = { ctx ->
            MultiAutoCompleteTextView(ctx).apply {
                this.hint = hint
                this.inputType = inputType
                this.minLines = minLines
                // 背景を透明にして OutlinedTextField 的な枠線を出さない
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                // Material3 テーマのデフォルトスタイルが paddingTop ~16dp を持つため、
                // TYPE_TEXT_FLAG_MULTI_LINE 設定後に gravity が TOP になると
                // テキスト上部に余分な空白として現れる。
                // 外側の Compose modifier で padding を管理するため内部は 0 にリセットする。
                setPadding(0, 0, 0, 0)

                if (account != null) {
                    setAdapter(
                        CustomEmojiCompleteAdapter(account, ctx, customEmojiRepository)
                    )
                    setTokenizer(CustomEmojiTokenizer())
                }

                addTextChangedListener(
                    onTextChanged = { text, start, _, count ->
                        // テキスト変化後のカーソル位置を通知
                        currentOnCursorPositionChanged(start + count)

                        // URL 貼り付け検出（コールバックが設定されている場合のみ）
                        if (currentOnUrlPasted != null && text != null && count > 0) {
                            val inputText = try {
                                text.substring(start, start + count)
                            } catch (_: Exception) { "" }
                            if (UrlPatternChecker.isMatch(inputText)) {
                                currentOnUrlPasted?.invoke(
                                    text.toString(),
                                    start,
                                    text.removeRange(start, start + count).toString(),
                                    count,
                                )
                            }
                        }
                    },
                ) { editable ->
                    val newText = editable?.toString() ?: ""
                    currentOnValueChange(newText)
                }

                setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) currentOnFocused()
                }

                if (autoFocus) {
                    // 画面を開いた時点で本文フィールドへフォーカスを当て、IME を表示する。
                    // adjustNothing のため OS 側の自動表示は行われないので明示的に呼ぶ。
                    // View がアタッチされてから実行する必要があるため post で遅延させる。
                    post {
                        if (requestFocus()) {
                            // SHOW_IMPLICIT は端末状態により無視されることがあるため、
                            // edge-to-edge で推奨される WindowInsetsController を優先して使う。
                            val controller = ViewCompat.getWindowInsetsController(this)
                            if (controller != null) {
                                controller.show(WindowInsetsCompat.Type.ime())
                            } else {
                                val imm = ctx.getSystemService(Context.INPUT_METHOD_SERVICE)
                                        as? InputMethodManager
                                imm?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
                            }
                        }
                    }
                }
            }.also { viewRef.value = it }
        },
        update = { view ->
            // アカウントが変わったらアダプターを更新する
            if (account != null && account.accountId != lastAdapterAccountId.value) {
                lastAdapterAccountId.value = account.accountId
                view.setAdapter(
                    CustomEmojiCompleteAdapter(account, view.context, customEmojiRepository)
                )
                view.setTokenizer(CustomEmojiTokenizer())
            }

            // テキストを同期する（ループ防止のため内容が変わった場合のみ更新）
            if (view.text.toString() != value) {
                view.setText(value)
                // カーソルを末尾に移動（単純な setText 後のデフォルト動作）
                try { view.setSelection(value.length) } catch (_: Exception) {}
            }
        },
        modifier = modifier,
    )
}
