package net.pantasystem.milktea.note.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * 純粋 Compose のテキスト入力フィールド土台（補完なし）。
 *
 * `BasicTextField` を `TextFieldValue`（text + selection を一体で保持）で駆動する。
 * カスタム絵文字補完・URL 検出・auto-focus などの機能はこの土台の上位で組み立てる。
 *
 * @param value 現在のテキスト状態（text + selection）
 * @param onValueChange 入力・カーソル移動で変化した値のコールバック
 * @param hint 空文字時に表示するプレースホルダ
 * @param minLines 最小行数
 * @param singleLine 単一行にするか
 * @param keyboardOptions IME オプション
 * @param focusRequester フォーカス制御用（auto-focus 等で使用）
 * @param onFocusChanged フォーカス状態が変わったときのコールバック
 * @param textStyle テキストスタイル。色は指定が無ければ onSurface を使う
 */
@Composable
fun NoteEditorTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    hint: String = "",
    minLines: Int = 1,
    singleLine: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    focusRequester: FocusRequester? = null,
    onFocusChanged: (Boolean) -> Unit = {},
    textStyle: TextStyle = LocalTextStyle.current,
) {
    val colorScheme = MaterialTheme.colorScheme
    val mergedTextStyle = textStyle.copy(color = colorScheme.onSurface)

    val focusModifier = if (focusRequester != null) {
        Modifier.focusRequester(focusRequester)
    } else {
        Modifier
    }

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .then(focusModifier)
            .onFocusChanged { onFocusChanged(it.isFocused) },
        textStyle = mergedTextStyle,
        cursorBrush = SolidColor(colorScheme.primary),
        minLines = minLines,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        decorationBox = { innerTextField ->
            Box {
                if (value.text.isEmpty() && hint.isNotEmpty()) {
                    Text(
                        text = hint,
                        style = mergedTextStyle.copy(color = colorScheme.onSurfaceVariant),
                    )
                }
                innerTextField()
            }
        },
    )
}

@Preview
@Composable
private fun Preview_NoteEditorTextField() {
    MaterialTheme {
        var value by remember { mutableStateOf(TextFieldValue("")) }
        NoteEditorTextField(
            value = value,
            onValueChange = { value = it },
            hint = "本文を入力",
            minLines = 3,
        )
    }
}
