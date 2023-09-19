package net.pantasystem.milktea.note.renote

import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.view.NormalBottomSheetDialogSelectionLayout

@Composable
fun RenoteDialogContent(
    uiState: RenoteViewModelUiState,
    accountIdToIconUrlMap: Map<Long, String>,
    isRenotedByMe: Boolean,
    onToggleAddAccount: (Long) -> Unit,
    onRenoteButtonClicked: () -> Unit,
    onQuoteRenoteButtonClicked: () -> Unit,
    onDeleteRenoteButtonCLicked: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            RenoteTargetAccountRowList(
                accounts = uiState.accounts,
                accountIdToIconUrlMap = accountIdToIconUrlMap,
                onClick = onToggleAddAccount
            )
            NormalBottomSheetDialogSelectionLayout(
                onClick = onRenoteButtonClicked,
                icon = Icons.Default.Repeat,
                text = stringResource(id = R.string.renote)
            )

            Spacer(modifier = Modifier.height(8.dp))
            if (isRenotedByMe) {
                NormalBottomSheetDialogSelectionLayout(
                    onClick = onDeleteRenoteButtonCLicked,
                    icon = Icons.Default.FormatQuote,
                    text = stringResource(id = R.string.unrenote)
                )
                Spacer(modifier = Modifier.height(8.dp))

            }


            if (uiState.canQuote) {
                NormalBottomSheetDialogSelectionLayout(
                    onClick = onQuoteRenoteButtonClicked,
                    icon = Icons.Default.FormatQuote,
                    text = stringResource(id = R.string.quote_renote)
                )
            }
        }
    }
}