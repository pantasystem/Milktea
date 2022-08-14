package net.pantasystem.milktea.group

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
@Stable
fun GroupEditorDialog(
    isNew: Boolean,
    name: String,
    onAction: (GroupEditorDialogAction) -> Unit,
) {

    Dialog(onDismissRequest = { onAction(GroupEditorDialogAction.OnDismiss) }) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colors.surface,
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(if(isNew) "グループ作成" else "編集", fontSize = 24.sp)
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = name,
                    placeholder = {
                        Text("グループ名")
                    },
                    onValueChange = { text ->
                        onAction(GroupEditorDialogAction.OnNameChanged(text))
                    }
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {onAction(GroupEditorDialogAction.OnDismiss)}) {
                        Text("Cancel")
                    }
                    TextButton(
                        enabled = name.isNotBlank(),
                        onClick = {
                        onAction(GroupEditorDialogAction.OnSave)
                        }
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

sealed interface GroupEditorDialogAction {
    object OnDismiss : GroupEditorDialogAction
    data class OnNameChanged(val text: String) : GroupEditorDialogAction
    object OnSave : GroupEditorDialogAction
}