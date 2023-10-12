package net.pantasystem.milktea.setting.compose.tab

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import net.pantasystem.milktea.model.account.page.Page


@Composable
internal fun TabItemsList(
    dragDropState: DragAndDropState,
    modifier: Modifier,
    list: List<Page>,
    onOptionButtonClicked: (Page) -> Unit
) {


    LazyColumn(modifier = modifier.dragAndDrop(dragDropState), state = dragDropState.listState) {
        itemsIndexed(list) { index, item ->
            Column(
                modifier = Modifier
                    .graphicsLayer {
                        translationY = dragDropState.targetElementTranslateY
                            .takeIf {
                                index == dragDropState.currentIndexOfDraggedItem
                            } ?: 0f
                    }
                    .fillMaxWidth()
                    .background(MaterialTheme.colors.surface)
                    .zIndex(if (index == dragDropState.currentIndexOfDraggedItem) 1f else 0f)
            ) {
                TabItem(
                    dragDropState = dragDropState,
                    index = index,
                    item = item,
                    onOptionButtonClicked = onOptionButtonClicked
                )
                Divider(modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun TabItem(
    dragDropState: DragAndDropState,
    index: Int,
    item: Page,
    onOptionButtonClicked: (Page) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row {
            Icon(
                Icons.Default.Menu,
                contentDescription = null,
                modifier = Modifier.pointerInput(Unit) {

                    detectDragGestures(
                        onDrag = dragDropState::onDrag,
                        onDragStart = {
                            dragDropState.listState.getVisibleItemInfoFor(index)?.run {
                                dragDropState.startDrag(index, this)
                            }
                        },
                        onDragEnd = dragDropState::onDragEnd,
                        onDragCancel = dragDropState::onDragCancel
                    )
                },
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                item.title,
                fontSize = 18.sp,
                color = MaterialTheme.colors.contentColorFor(MaterialTheme.colors.surface)
            )
        }

        IconButton(onClick = {
            onOptionButtonClicked(item)
        }) {
            Icon(Icons.Default.MoreVert, contentDescription = null)
        }

    }
}
