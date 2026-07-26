package net.pantasystem.milktea.note.editor

import android.text.InputType
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.SharedFlow
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.emoji.CustomEmojiRepository
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.editor.viewmodel.NoteEditorFocusEditTextType
import net.pantasystem.milktea.note.editor.viewmodel.TextWithCursorPos

/**
 * テキスト入力エリア（CW フィールド + 本文フィールド + 文字数カウント）
 *
 * 内部で EmojiAutoCompleteTextField（AndroidView ラッパー）を使用する。
 *
 * @param onFocusChanged どちらのフィールドがフォーカスされたかを通知するコールバック。
 *   NoteEditorScreen で focusedField 状態の更新に使う。
 * @param textCursorPosFlow ViewModel から流れるカーソル位置更新イベント。本文フィールドのみに渡す。
 * @param onUrlPasted 本文フィールドへの URL 貼り付けを検出したときのコールバック。
 */
@Composable
fun NoteEditorTextInputSection(
    text: String,
    cw: String?,
    hasCw: Boolean,
    account: Account?,
    customEmojiRepository: CustomEmojiRepository,
    textCursorPosFlow: SharedFlow<TextWithCursorPos>,
    onTextChanged: (String) -> Unit,
    onCwChanged: (String) -> Unit,
    onFocusChanged: (NoteEditorFocusEditTextType) -> Unit,
    onUrlPasted: (text: String, start: Int, beforeText: String, count: Int) -> Unit,
    onTextCursorPositionChanged: (Int) -> Unit = {},
    onCwCursorPositionChanged: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (hasCw) {
            // CW フィールドは純粋 Compose 版へ移行済み（段階移行 Phase 3）
            EmojiComposeTextField(
                value = cw ?: "",
                onValueChange = onCwChanged,
                onFocused = { onFocusChanged(NoteEditorFocusEditTextType.Cw) },
                account = account,
                customEmojiRepository = customEmojiRepository,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                hint = stringResource(id = R.string.cw_hint),
                singleLine = true,
                minLines = 1,
                onCursorPositionChanged = onCwCursorPositionChanged,
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
        }

        EmojiAutoCompleteTextField(
            value = text,
            onValueChange = onTextChanged,
            onFocused = { onFocusChanged(NoteEditorFocusEditTextType.Text) },
            account = account,
            customEmojiRepository = customEmojiRepository,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            hint = stringResource(id = R.string.please_speak),
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE,
            minLines = 1,
            textCursorPosFlow = textCursorPosFlow,
            onUrlPasted = onUrlPasted,
            onCursorPositionChanged = onTextCursorPositionChanged,
            autoFocus = true,
        )
    }
}

@Preview
@Composable
private fun Preview_NoteEditorTextInputSection() {
    MaterialTheme {
        Surface {
            Text(
                text = "EmojiAutoCompleteTextField preview requires Android context",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
