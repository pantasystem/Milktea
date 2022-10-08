package net.pantasystem.milktea.note.editor

import android.app.Dialog
import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.model.notes.CanLocalOnly
import net.pantasystem.milktea.model.notes.Visibility
import net.pantasystem.milktea.model.notes.isLocalOnly
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.editor.viewmodel.NoteEditorViewModel

@AndroidEntryPoint
class VisibilitySelectionDialogV2 : BottomSheetDialogFragment() {

    val viewModel by activityViewModels<NoteEditorViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            setContentView(ComposeView(requireContext()).apply {
                setContent {
                    val visibility by viewModel.visibility.collectAsState()
                    val channelsState by viewModel.channels.collectAsState()
                    val channels = (channelsState.content as? StateContent.Exist)?.rawContent ?: emptyList()

                    MdcTheme {
                        Surface(
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        ) {
                            Column(
                                Modifier.padding(top = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Box(
                                    Modifier
                                        .width(32.dp)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color.Gray),
                                    content = {}
                                )
                                Spacer(Modifier.height(4.dp))

                                LazyColumn(
                                    Modifier.fillMaxWidth()
                                ) {
                                    item {
                                        VisibilitySelectionTile(
                                            item = Visibility.Public(visibility.isLocalOnly()),
                                            isSelected = visibility is Visibility.Public,
                                            onClick = viewModel::setVisibility
                                        )

                                        VisibilitySelectionTile(
                                            item = Visibility.Home(visibility.isLocalOnly()),
                                            isSelected = visibility is Visibility.Home,
                                            onClick = viewModel::setVisibility
                                        )
                                        VisibilitySelectionTile(
                                            item = Visibility.Followers(
                                                visibility.isLocalOnly()
                                            ), isSelected = visibility is Visibility.Followers,
                                            onClick = viewModel::setVisibility
                                        )
                                        VisibilitySelectionTile(
                                            item = Visibility.Specified(
                                                emptyList()
                                            ), isSelected = visibility is Visibility.Specified,
                                            onClick = viewModel::setVisibility
                                        )

                                        VisibilityLocalOnlySwitch(
                                            checked = visibility.isLocalOnly(),
                                            enabled = visibility is CanLocalOnly,
                                            onChanged = { result ->
                                                (visibility as? CanLocalOnly)?.changeLocalOnly(
                                                    result
                                                )?.also {
                                                    viewModel.setVisibility(it as Visibility)
                                                }
                                            },
                                        )

                                        VisibilityChannelTitle()
                                    }

                                    items(channels) { channel ->
                                        Text(channel.name)
                                    }
                                }
                            }

                        }
                    }

                }
            })
        }
    }
}

@Composable
fun VisibilitySelectionTile(
    item: Visibility,
    isSelected: Boolean,
    onClick: (item: Visibility) -> Unit,
) {

    val title = when (item) {
        is Visibility.Followers -> stringResource(id = R.string.visibility_follower)
        is Visibility.Home -> stringResource(id = R.string.visibility_home)
        is Visibility.Public -> stringResource(id = R.string.visibility_public)
        is Visibility.Specified -> stringResource(id = R.string.visibility_specified)
    }

    val iconDrawable = when (item) {
        is Visibility.Followers -> R.drawable.ic_lock_black_24dp
        is Visibility.Home -> R.drawable.ic_home_black_24dp
        is Visibility.Public -> R.drawable.ic_language_black_24dp
        is Visibility.Specified -> R.drawable.ic_email_black_24dp
    }

    Surface(
        Modifier.clickable {
            onClick(item)
        },
        color = if (isSelected) MaterialTheme.colors.primary else MaterialTheme.colors.surface,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painterResource(iconDrawable), contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text(title)
        }
    }
}

@Composable
fun VisibilityLocalOnlySwitch(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onChanged: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .clickable {
                onChanged.invoke(!checked)
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.local_only))
        }

        Switch(checked = checked, onCheckedChange = onChanged, enabled = enabled)
    }
}

@Composable
fun VisibilityChannelTitle() {
    Text(
        stringResource(R.string.channel),
        fontSize = 20.sp,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
        fontWeight = FontWeight.ExtraBold
    )
}

