package jp.panta.misskeyandroidclient.ui.users

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.ui.users.viewmodel.search.SearchUserViewModel
import jp.panta.misskeyandroidclient.ui.users.viewmodel.selectable.SelectedUserViewModel
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun SearchAndSelectUserScreen(
    searchUserViewModel: SearchUserViewModel,
    selectedUserViewModel: SelectedUserViewModel,
    onNavigateUp: () -> Unit,
) {


    val users by searchUserViewModel.users.collectAsState()
    val selectedUserIds by selectedUserViewModel.selectedUserIds.observeAsState()

    val userName by searchUserViewModel.userName.observeAsState()
    val host by searchUserViewModel.host.observeAsState()

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = {
            SimpleUserListView(
                users = users,
                onSelected = { selectedUserViewModel.toggleSelectUser(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
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
            }
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("@")
                    TextField(
                        value = userName ?: "",
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(stringResource(id = R.string.user_name))
                        },
                        onValueChange = { text ->
                            searchUserViewModel.userName.value = text
                        },
                    )
                    Text("@")
                    TextField(
                        value = host ?: "",
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(stringResource(id = R.string.host))
                        },
                        onValueChange = { text ->
                            searchUserViewModel.host.value = text
                        }
                    )
                }
                SimpleUserListView(
                    users = users,
                    selectedUserIds = selectedUserIds ?: emptySet(),
                    onSelected = {
                        selectedUserViewModel.toggleSelectUser(it)
                    },
                    modifier = Modifier.weight(1f)
                )

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
                        text = "${stringResource(R.string.select_user)}(${selectedUserIds?.size ?: 0})",
                        fontSize = 24.sp
                    )
                }
            }


        }
    }

}

