package net.pantasystem.milktea.note.editor.visibility

import android.app.Dialog
import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.unit.dp
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.channel.Channel
import net.pantasystem.milktea.model.instance.FeatureType
import net.pantasystem.milktea.model.notes.CanLocalOnly
import net.pantasystem.milktea.model.notes.ReactionAcceptanceType
import net.pantasystem.milktea.model.notes.Visibility
import net.pantasystem.milktea.model.notes.isLocalOnly
import net.pantasystem.milktea.note.editor.viewmodel.NoteEditorViewModel

@AndroidEntryPoint
class VisibilitySelectionDialogV2 : BottomSheetDialogFragment() {

    val viewModel by activityViewModels<NoteEditorViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            val view = ComposeView(requireContext()).apply {
                setContent {
                    MdcTheme {
                        VisibilitySelectionDialogContent(viewModel = viewModel)
                    }
                }
            }
            setContentView(view)
            val behavior = (this as BottomSheetDialog).behavior
            behavior.peekHeight = (resources.displayMetrics.density * 350).toInt()
        }
    }


}

@Composable
fun VisibilitySelectionDialogContent(viewModel: NoteEditorViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val channelsState by viewModel.channels.collectAsState()
    val enableFeatures by viewModel.enableFeatures.collectAsState()
    val reactionAcceptanceType = uiState.sendToState.reactionAcceptanceType
    VisibilitySelectionDialogLayout(
        visibility = uiState.sendToState.visibility,
        channelId = uiState.sendToState.channelId,
        currentAccountInstanceType = uiState.currentAccount?.instanceType,
        channelsState = channelsState,
        onVisibilityChanged = viewModel::setVisibility,
        enableFeatures = enableFeatures,
        reactionAcceptanceType = reactionAcceptanceType,
        onChannelSelected = {
            viewModel.setChannelId(it.id)
        },
        onReactionAcceptanceSelected = {
            viewModel.onReactionAcceptanceSelected(it)
        }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun VisibilitySelectionDialogLayout(
    visibility: Visibility,
    channelsState: ResultState<List<Channel>>,
    enableFeatures: Set<FeatureType>,
    onVisibilityChanged: (visibility: Visibility) -> Unit,
    onChannelSelected: (channel: Channel) -> Unit,
    onReactionAcceptanceSelected: (ReactionAcceptanceType?) -> Unit,
    channelId: Channel.Id? = null,
    currentAccountInstanceType: Account.InstanceType? = null,
    reactionAcceptanceType: ReactionAcceptanceType? = null,
) {
    val channels =
        (channelsState.content as? StateContent.Exist)?.rawContent ?: emptyList()
    Surface(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .nestedScroll(rememberNestedScrollInteropConnection())
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
                        isSelected = visibility is Visibility.Public && channelId == null,
                        onClick = onVisibilityChanged
                    )

                    VisibilitySelectionTile(
                        item = Visibility.Home(visibility.isLocalOnly()),
                        isSelected = visibility is Visibility.Home && channelId == null,
                        onClick = onVisibilityChanged
                    )
                    VisibilitySelectionTile(
                        item = Visibility.Followers(
                            visibility.isLocalOnly()
                        ),
                        isSelected = visibility is Visibility.Followers && channelId == null,
                        onClick = onVisibilityChanged
                    )
                    VisibilitySelectionTile(
                        item = Visibility.Specified(
                            emptyList()
                        ),
                        isSelected = visibility is Visibility.Specified && channelId == null,
                        onClick = onVisibilityChanged
                    )


                    if (currentAccountInstanceType == Account.InstanceType.MISSKEY
                        || currentAccountInstanceType == Account.InstanceType.FIREFISH
                    ) {
                        VisibilityLocalOnlySwitch(
                            checked = visibility.isLocalOnly(),
                            enabled = visibility is CanLocalOnly && channelId == null,
                            onChanged = { result ->
                                (visibility as? CanLocalOnly)?.changeLocalOnly(
                                    result
                                )?.also {
                                    onVisibilityChanged(it as Visibility)
                                }
                            },
                        )
                    }
                }

                if (enableFeatures.contains(FeatureType.ReactionAcceptance)) {
                    item {
                        ReactionAcceptanceTitle()
                    }
                    items(listOf<ReactionAcceptanceType?>(null) + ReactionAcceptanceType.values()) { type ->
                        ReactionAcceptanceSelection(
                            type = type,
                            isSelected = type == reactionAcceptanceType,
                            onSelected = onReactionAcceptanceSelected
                        )
                    }
                }

                if (enableFeatures.contains(FeatureType.Channel)) {
                    item {
                        VisibilityChannelTitle()
                    }
                    items(channels) { channel ->
                        VisibilityChannelSelection(
                            item = channel,
                            isSelected = channel.id == channelId,
                            onClick = {
                                onChannelSelected(it)
                            }
                        )
                    }
                }
            }
        }

    }
}


