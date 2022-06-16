package jp.panta.misskeyandroidclient.ui.notes.view.draft

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.pantasystem.milktea.model.drive.DriveFileRepository
import net.pantasystem.milktea.model.drive.FilePropertyDataSource
import net.pantasystem.milktea.model.notes.draft.DraftNote

@Composable
fun DraftNoteCard(
    draftNote: DraftNote,
    visibleContentDraftNoteIds: Set<Long>,
    filePropertyDataSource: FilePropertyDataSource,
    driveFileRepository: DriveFileRepository,
) {

    Card(
        elevation = 4.dp,
        modifier = Modifier.padding(8.dp),
        shape = RoundedCornerShape(8.dp),
    ) {

        Column() {
            if (!draftNote.cw.isNullOrBlank()) {
                Text(text = draftNote.cw ?: "")

            }
            AnimatedVisibility(
                visible = draftNote.cw.isNullOrBlank() || visibleContentDraftNoteIds.contains(
                    draftNote.draftNoteId
                )
            ) {
                if (draftNote.text != null) {
                    Text(draftNote.text!!)
                }

            }
            // TODO: 実装する

//            if (!draftNote.files.isNullOrEmpty()) {
//                HorizontalFilePreviewList(
//                    files = draftNote.appFiles,
//                    repository = driveFileRepository,
//                    dataSource = filePropertyDataSource,
//                    onAction = {
//
//                    },
//                )
//            }
        }

    }
}

@Composable
fun DraftNotePollChoice(text: String) {
    Text(
        modifier = Modifier.padding(4.dp),
        
        text = text
    )
}