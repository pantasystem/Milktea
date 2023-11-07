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
    isRenotedByMe: Boolean,
    onToggleAddAccount: (Long) -> Unit,
    onRenoteButtonClicked: () -> Unit,
    onQuoteRenoteButtonClicked: () -> Unit,
    onRenoteInChannelButtonClicked: () -> Unit,
    onQuoteInChannelRenoteButtonClicked: () -> Unit,
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
                onClick = onToggleAddAccount
            )

            if (uiState.isRenoteButtonVisible) {
                NormalBottomSheetDialogSelectionLayout(
                    onClick = onRenoteButtonClicked,
                    icon = Icons.Default.Repeat,
                    text = stringResource(id = R.string.renote)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            if (isRenotedByMe) {
                NormalBottomSheetDialogSelectionLayout(
                    onClick = onDeleteRenoteButtonCLicked,
                    icon = Icons.Default.FormatQuote,
                    text = stringResource(id = R.string.unrenote)
                )
                Spacer(modifier = Modifier.height(8.dp))

            }

            if (uiState.isRenoteButtonVisible && uiState.canQuote) {
                NormalBottomSheetDialogSelectionLayout(
                    onClick = onQuoteRenoteButtonClicked,
                    icon = Icons.Default.FormatQuote,
                    text = stringResource(id = R.string.quote_renote)
                )
            }

            if (uiState.isChannelRenoteButtonVisible) {
                Spacer(modifier = Modifier.height(8.dp))
                NormalBottomSheetDialogSelectionLayout(
                    onClick = onRenoteInChannelButtonClicked,
                    icon = Icons.Default.Repeat,
                    text = stringResource(id = R.string.renote_in_channel)
                )

                if (uiState.canQuote) {
                    Spacer(modifier = Modifier.height(8.dp))
                    NormalBottomSheetDialogSelectionLayout(
                        onClick = onQuoteInChannelRenoteButtonClicked,
                        icon = Icons.Default.FormatQuote,
                        text = stringResource(id = R.string.quote_renote_in_channel)
                    )
                }
            }
        }
    }
}