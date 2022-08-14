package net.pantasystem.milktea.group

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.pantasystem.milktea.model.group.GroupWithMember

@Composable
@Stable
fun GroupCardListPage(uiState: GroupListUiState, onAction: (GroupCardListAction) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {
                        onAction.invoke(GroupCardListAction.OnNavigateUp)
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                title = {
                    Text(stringResource(R.string.groups))
                },
                backgroundColor = MaterialTheme.colors.surface
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                onAction.invoke(GroupCardListAction.OnFabClick)
            }) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) {
        LazyColumn(
            Modifier.padding(it)
        ) {
           items(uiState.joinedGroups.size) { index ->
               GroupCard(group = uiState.joinedGroups[index], onClick = {
                   onAction.invoke(GroupCardListAction.OnClick(uiState.joinedGroups[index]))
               })
           }
        }
    }

}

sealed interface GroupCardListAction {
    data class OnClick(val group: GroupWithMember) : GroupCardListAction
    object OnNavigateUp : GroupCardListAction
    object OnFabClick : GroupCardListAction
}