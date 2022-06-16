package jp.panta.misskeyandroidclient.ui.notes.view.draft

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import jp.panta.misskeyandroidclient.R

@Composable
fun DraftNotesPage() {
    Scaffold(
        topBar = {
            TopAppBar() {
                Text(text = stringResource(id = R.string.draft_notes))
            }
        }
    ) {
        LazyColumn(Modifier.padding(it)) {

        }
    }
}