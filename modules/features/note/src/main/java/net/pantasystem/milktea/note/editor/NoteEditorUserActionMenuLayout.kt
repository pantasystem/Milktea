package net.pantasystem.milktea.note.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.pantasystem.milktea.note.R

@Composable
fun NoteEditorUserActionMenuLayout(
    modifier: Modifier = Modifier,
    isEnableDrive: Boolean,
    onPickFileFromDriveButtonClicked: () -> Unit,
    onPickFileFromLocalButtonCLicked: () -> Unit,
    onPickImageFromLocalButtonClicked: () -> Unit,
    onTogglePollButtonClicked: () -> Unit,
    onSelectMentionUsersButtonClicked: () -> Unit,
    onSelectEmojiButtonClicked: () -> Unit,
    onToggleCwButtonClicked: () -> Unit
) {
    var isShowFilePickerDropDownMenu: Boolean by remember {
        mutableStateOf(false)
    }
    Surface(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        ) {
            MenuItemLayout {
                DropdownMenu(
                    expanded = isShowFilePickerDropDownMenu,
                    onDismissRequest = { isShowFilePickerDropDownMenu = true }
                ) {
                    DropdownMenuItem(onClick = onPickImageFromLocalButtonClicked) {
                        Icon(Icons.Default.Image, contentDescription = null)
                        Text("画像を選択")
                    }
                    DropdownMenuItem(onClick = onPickFileFromLocalButtonCLicked) {
                        Icon(Icons.Default.UploadFile, contentDescription = null)
                        Text("ファイルを選択")
                    }
                    if (isEnableDrive) {
                        DropdownMenuItem(onClick = onPickFileFromDriveButtonClicked) {
                            Icon(Icons.Default.Cloud, contentDescription = null)
                            Text("ドライブから")
                        }
                    }
                }
                IconButton(
                    onClick = {
                        isShowFilePickerDropDownMenu = true
                    }
                ) {
                    Icon(Icons.Default.AddToPhotos, contentDescription = null)
                }
            }
            MenuItemLayout {
                IconButton(onClick = onTogglePollButtonClicked) {
                    Icon(Icons.Default.Poll, contentDescription = null)
                }
            }
            MenuItemLayout {
                IconButton(onClick = onToggleCwButtonClicked) {
                    Icon(Icons.Default.VisibilityOff, contentDescription = null)
                }
            }
            MenuItemLayout {
                IconButton(onClick = onSelectMentionUsersButtonClicked) {
                    Icon(
                        painterResource(id = R.drawable.ic_mention),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            MenuItemLayout {
                IconButton(onClick = onSelectEmojiButtonClicked) {
                    Icon(Icons.Default.EmojiEmotions, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun MenuItemLayout(
    modifier: Modifier = Modifier,
    children: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier,
        contentAlignment = Alignment.Center
    ) {
        children()
    }
}

@Preview
@Composable
fun Preview_NoteEditorUserActionMenuLayout() {
    MaterialTheme {
        NoteEditorUserActionMenuLayout(
            modifier = Modifier.height(56.dp),
            isEnableDrive = true,
            onPickFileFromDriveButtonClicked = {},
            onPickFileFromLocalButtonCLicked = {},
            onPickImageFromLocalButtonClicked = {},
            onTogglePollButtonClicked = {},
            onSelectMentionUsersButtonClicked = {},
            onSelectEmojiButtonClicked = {},
            onToggleCwButtonClicked = {}
        )
    }
}