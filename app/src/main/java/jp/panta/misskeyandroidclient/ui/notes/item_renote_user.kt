package jp.panta.misskeyandroidclient.ui.notes

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import jp.panta.misskeyandroidclient.model.notes.NoteRelation
import jp.panta.misskeyandroidclient.ui.components.CustomEmojiText

@Composable
fun ItemRenoteUser(note: NoteRelation, isUserNameDefault: Boolean = false) {

    Card(
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier.padding(0.5.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            Image(
                painter = rememberImagePainter(note.user.avatarUrl),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                if(isUserNameDefault){
                    CustomEmojiText(text = note.user.getDisplayUserName(), emojis = note.user.emojis)
                }else{
                    CustomEmojiText(text = note.user.getDisplayName(), emojis = note.user.emojis)
                }
                if(isUserNameDefault){
                    Text(text = note.user.getDisplayName())
                }else{
                    Text(text = note.user.getDisplayUserName())
                }
            }

        }
    }
}


