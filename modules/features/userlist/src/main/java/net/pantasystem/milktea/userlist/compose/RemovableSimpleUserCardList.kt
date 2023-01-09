package net.pantasystem.milktea.userlist.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.pantasystem.milktea.model.user.User

@Composable
fun RemovableSimpleUserCardList(
    modifier: Modifier,
    users: List<User>,
    accountHost: String?,
    onSelectUser: (User) -> Unit,
    onDeleteButtonClicked: (User) -> Unit,
) {
    LazyColumn(modifier.fillMaxSize()) {
        items(users) { user ->
            RemovableSimpleUserCard(
                user = user,
                accountHost = accountHost,
                onSelected = { u ->
                    onSelectUser(u)
                },
                onDeleteButtonClicked = { u ->
                    onDeleteButtonClicked(u)
                }
            )
        }
    }

}