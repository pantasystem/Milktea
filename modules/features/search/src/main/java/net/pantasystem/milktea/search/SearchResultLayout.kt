package net.pantasystem.milktea.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.user.compose.ItemSimpleUserCard

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SearchResultLayout(
    modifier: Modifier = Modifier,
    uiState: SearchUiState,
    onUserSelected: (User) -> Unit,
    onHashtagSelected: (String) -> Unit
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
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
            ItemSimpleUserCard(user = user, onSelected = onUserSelected)
        }
    }
}