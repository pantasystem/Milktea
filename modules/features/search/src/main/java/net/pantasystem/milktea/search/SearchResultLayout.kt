package net.pantasystem.milktea.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.pantasystem.milktea.model.search.SearchHistory
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.user.compose.ItemSimpleUserCard

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SearchSuggestionsLayout(
    modifier: Modifier = Modifier,
    uiState: SearchUiState,
    onUserSelected: (User) -> Unit,
    onHashtagSelected: (String) -> Unit,
    onDeleteSearchHistory: (Long) -> Unit,
    onSearchHistoryClicked: (SearchHistory) -> Unit
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        if (uiState.hashtags.isEmpty() && uiState.users.isEmpty()) {
            items(uiState.history) { history ->
                SearchHistoryCard(history = history, onDelete = {
                    onDeleteSearchHistory(history.id)
                }, onClick = {
                    onSearchHistoryClicked(history)
                })
            }
        }
        items(uiState.hashtags) { hashtag ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onHashtagSelected(hashtag)
                }
            ) {
                Box(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
                    Text(text = "#$hashtag", fontSize = 20.sp)
                }
            }
        }
        items(uiState.users) { user ->
            ItemSimpleUserCard(user = user, onSelected = onUserSelected, accountHost = uiState.accountHost)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SearchHistoryCard(history: SearchHistory, onDelete: () -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(history.keyword, fontSize = 20.sp, modifier = Modifier.weight(1f))
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = stringResource(id = R.string.remove)
                )
            }
        }
    }
}