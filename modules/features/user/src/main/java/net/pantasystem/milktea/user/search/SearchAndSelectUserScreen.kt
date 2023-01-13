package net.pantasystem.milktea.user.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import net.pantasystem.milktea.user.R
import net.pantasystem.milktea.user.compose.SimpleUserListView
import net.pantasystem.milktea.user.viewmodel.SelectedUserViewModel

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun SearchAndSelectUserScreen(
    searchUserViewModel: SearchUserViewModel,
    selectedUserViewModel: SelectedUserViewModel,
    onNavigateUp: () -> Unit,
) {

    val uiState by searchUserViewModel.uiState.collectAsState()

    val users by searchUserViewModel.users.collectAsState()
    val selectedUserIds by selectedUserViewModel.selectedUserIds.collectAsState()
    val selectedUsers by selectedUserViewModel.selectedUserList.collectAsState()


    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            SimpleUserListView(
                users = selectedUsers.toList(),
                onSelected = { selectedUserViewModel.toggleSelectUser(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                accountHost = uiState.account?.getHost()
            )
        }
    ) {

        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                onNavigateUp.invoke()
                            },
                        ) {
                            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    title = {
                        Text(stringResource(R.string.select_user))
                    },
                    backgroundColor = MaterialTheme.colors.surface
                )
            },
            bottomBar = {
                if (!sheetState.isVisible) {
                    Surface {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                                .clickable {
                                    scope.launch {
                                        if (sheetState.isVisible) {
                                            sheetState.hide()
                                        } else {
                                            sheetState.show()
                                        }
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_person_add_black_24dp),
                                contentDescription = null,
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${stringResource(R.string.select_user)}(${selectedUserIds.size})",
                                fontSize = 24.sp
                            )
                        }
                    }
                }

            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("@")
                    TextField(
                        value = uiState.query.word,
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(stringResource(id = R.string.user_name))
                        },
                        onValueChange = { text ->
                            searchUserViewModel.setUserName(text)
                        },
                    )
                    Text("@")
                    TextField(
                        value = uiState.query.sourceHost ?: "",
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(stringResource(id = R.string.host))
                        },
                        onValueChange = { text ->
                            searchUserViewModel.setHost(text)
                        }
                    )
                }
                SimpleUserListView(
                    users = users,
                    selectedUserIds = selectedUserIds,
                    onSelected = {
                        selectedUserViewModel.toggleSelectUser(it)
                    },
                    modifier = Modifier.weight(1f),
                    accountHost = uiState.account?.getHost(),
                )


            }


        }
    }

}

