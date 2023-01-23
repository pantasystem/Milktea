package net.pantasystem.milktea.common_android_ui.account

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.pantasystem.milktea.common_android_ui.R
import net.pantasystem.milktea.common_android_ui.account.viewmodel.AccountInfo
import net.pantasystem.milktea.common_android_ui.account.viewmodel.AccountViewModelUiState

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AccountSwitchingDialogLayout(
    uiState: AccountViewModelUiState,
    onSettingButtonClicked: () -> Unit,
    onAvatarIconClicked: (AccountInfo) -> Unit,
    onAccountClicked: (AccountInfo) -> Unit,
    onAddAccountButtonClicked: () -> Unit,
) {
    Surface(Modifier.fillMaxWidth(), shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.width(56.dp))
                Text(
                    stringResource(id = R.string.account),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = onSettingButtonClicked) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                }
            }
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .nestedScroll(
                        rememberNestedScrollInteropConnection()
                    )
            ) {
                items(uiState.accounts) { account ->
                    AccountTile(account = account, onClick = onAccountClicked, onAvatarClick = onAvatarIconClicked)
                }
            }

            Button(
                onClick = onAddAccountButtonClicked,
                shape = RoundedCornerShape(32.dp),
                contentPadding = PaddingValues(horizontal = 32.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(id = R.string.add_account))
            }
        }
    }
}