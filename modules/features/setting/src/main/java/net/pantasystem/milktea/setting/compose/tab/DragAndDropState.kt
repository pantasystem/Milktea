package net.pantasystem.milktea.setting.compose.tab

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


internal fun LazyListState.getVisibleItemInfoFor(absoluteIndex: Int): LazyListItemInfo? {
    return this
        .layoutInfo
        .visibleItemsInfo
        .getOrNull(absoluteIndex - this.layoutInfo.visibleItemsInfo.first().index)
}

@Composable
internal fun rememberDragDropListState(
    lazyListState: LazyListState = rememberLazyListState(),
    scope: CoroutineScope,
    onMove: (Int, Int) -> Unit
): DragAndDropState {
    return remember { DragAndDropState(listState = lazyListState, scope = scope, onMove = onMove) }
}

internal fun Modifier.dragAndDrop(dragDropState: DragAndDropState): Modifier {
    return then(
        Modifier.pointerInput(
            Unit
        ) {
            detectDragGesturesAfterLongPress(
                onDrag = dragDropState::onDrag,
                onDragStart = dragDropState::onDragStart,
                onDragEnd = dragDropState::onDragEnd,
                onDragCancel = dragDropState::onDragCancel
            )
        }
    )
}


internal class DragAndDropState(
    val listState: LazyListState,
    val scope: CoroutineScope,
    val onMove: (fromIndex: Int, toIndex: Int) -> Unit,
) {
    private var initiallyDraggedElement by mutableStateOf<LazyListItemInfo?>(null)
    var currentIndexOfDraggedItem by mutableStateOf<Int?>(null)
        private set
    private var draggedDistance by mutableStateOf(0f)

    private var overscrollJob: Job? = null

    val targetElementTranslateY
        get() = currentIndexOfDraggedItem
            ?.let {
                listState.getVisibleItemInfoFor(it)
            }
            ?.let { item ->
                (initiallyDraggedElement?.offset ?: 0f).toFloat() + draggedDistance - item.offset
            }


    private val currentElementItemInfo
        get() = currentIndexOfDraggedItem?.let {
            listState.getVisibleItemInfoFor(it)
        }


    fun onDrag(change: PointerInputChange, offset: Offset) {
        change.consume()
        draggedDistance += offset.y


        initiallyDraggedElement?.let {
            val startOffset = it.offset + draggedDistance
            val endOffset = it.offset + it.size + draggedDistance

            currentElementItemInfo?.let { hovered ->
                listState.layoutInfo.visibleItemsInfo.filterNot { item ->
                    (item.offset + item.size) < startOffset || item.offset > endOffset
                }.firstOrNull { item ->
                    val delta = startOffset - hovered.offset
                    when {
                        delta > 0 -> (endOffset > (item.offset + item.size))
                        else -> (startOffset < item.offset)
                    }
                }?.also { item ->
                    currentIndexOfDraggedItem?.let { current ->
                        onMove(current, item.index)
                    }
                    currentIndexOfDraggedItem = item.index
                }
            }
        }
        if (overscrollJob?.isActive == true) {
            return
        }
        checkForOverScroll().takeIf { o -> o != 0f }
            ?.let {
                overscrollJob = scope.launch {
                    listState.scrollBy(it)
                }
            } ?: run { overscrollJob?.cancel() }
    }

    fun onDragStart(offset: Offset) {
        listState.layoutInfo.visibleItemsInfo
            .firstOrNull { item ->
                offset.y.toInt() in item.offset..(item.offset + item.size)
            }?.also {
                currentIndexOfDraggedItem = it.index
                initiallyDraggedElement = it
            }
    }

    fun startDrag(currentIndexOfDraggedItem: Int, initiallyDraggedElement: LazyListItemInfo) {
        this.currentIndexOfDraggedItem = currentIndexOfDraggedItem
        this.initiallyDraggedElement = initiallyDraggedElement
    }

    fun onDragEnd() {
        draggedDistance = 0f
        currentIndexOfDraggedItem = null
        initiallyDraggedElement = null
        overscrollJob?.cancel()
    }

    fun onDragCancel() {
        draggedDistance = 0f
        currentIndexOfDraggedItem = null
        initiallyDraggedElement = null
        overscrollJob?.cancel()
    }

    private fun checkForOverScroll(): Float {
        return initiallyDraggedElement?.let {
            val startOffset = it.offset + draggedDistance
            val endOffset = (it.offset + it.size) + draggedDistance
            val viewPortStart = listState.layoutInfo.viewportStartOffset
            val viewPortEnd = listState.layoutInfo.viewportEndOffset

            when {
                draggedDistance > 0 -> (endOffset - viewPortEnd).takeIf { diff -> diff > 0 }
                draggedDistance < 0 -> (startOffset - viewPortStart).takeIf { diff -> diff < 0 }
                else -> null
            }
        } ?: 0f
    }
}