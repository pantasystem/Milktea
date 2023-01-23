package net.pantasystem.milktea.common_compose.drive

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.pantasystem.milktea.common_compose.R

@Composable
fun EditCaptionDialogLayout(
    value: String,
    onCancelButtonClicked: () -> Unit,
    onTextChanged: (String) -> Unit,
    onSaveButtonClicked: () -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colors.surface,
        elevation = 24.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                stringResource(R.string.edit_caption),
                fontSize = 24.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = value,
                placeholder = {
                    Text(stringResource(R.string.input_caption))
                },
                onValueChange = onTextChanged
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onCancelButtonClicked) {
                    Text(stringResource(R.string.cancel))
                }
                TextButton(onClick = onSaveButtonClicked) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }
}
