package jp.panta.misskeyandroidclient.ui.users

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.ui.users.viewmodel.search.SearchUserViewModel
import jp.panta.misskeyandroidclient.ui.users.viewmodel.selectable.SelectedUserViewModel

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun SearchAndSelectUserScreen(
    searchUserViewModel: SearchUserViewModel,
    selectedUserViewModel: SelectedUserViewModel,
    onNavigateUp: ()-> Unit,
) {



    val users by searchUserViewModel.users.collectAsState()
    val selectedUserIds by selectedUserViewModel.selectedUserIds.observeAsState()

    val userName by searchUserViewModel.userName.observeAsState()
    val host by searchUserViewModel.host.observeAsState()

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
                }
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
                selectedUserIds = selectedUserIds?: emptySet(),
                onSelected = {
                    selectedUserViewModel.toggleSelectUser(it)
                },
            )
        }
    }
}