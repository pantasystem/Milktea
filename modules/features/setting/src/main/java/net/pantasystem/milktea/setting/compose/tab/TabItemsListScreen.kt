package net.pantasystem.milktea.setting.compose.tab

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common_android_ui.account.page.PageTypeHelper
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.account.page.PageType
import net.pantasystem.milktea.setting.R


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TabItemsListScreen(
    dragDropState: DragAndDropState,
    pageTypes: List<PageType>,
    list: List<Page>,
    onSelectPage: (PageType) -> Unit,
    onOptionButtonClicked: (Page) -> Unit,
    onNavigateUp: () -> Unit,

    ) {
    val scaffoldState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()
//    val pageTypes by mPageSettingViewModel.pageTypes.collectAsState()


    ModalBottomSheetLayout(
        sheetState = scaffoldState,
        sheetContent = {
            TabItemSelectionDialog(
                modifier = Modifier.fillMaxSize(),
                items = pageTypes,
                onClick = {
                    scope.launch {
                        scaffoldState.hide()
                    }
                    onSelectPage(it)
                }
            )
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(stringResource(R.string.add_to_tab))
                    },
                    backgroundColor = MaterialTheme.colors.surface,
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                onNavigateUp()
                            }
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                        }
                    }
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    text = {
                        Text(stringResource(R.string.add_tab))
                    },
                    icon = {
                        Icon(Icons.Default.BookmarkAdd, contentDescription = null)
                    },
                    onClick = {
                        scope.launch {
                            if (scaffoldState.isVisible) {
                                scaffoldState.hide()
                            } else {
                                scaffoldState.show()
                            }
                        }
                    }
                )
            },
            floatingActionButtonPosition = FabPosition.Center,
        ) {
            TabItemsList(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
                list = list,
                onOptionButtonClicked = { page ->
                    onOptionButtonClicked(page)
                },
                dragDropState = dragDropState
            )
        }

    }
}


@Composable
fun TabItemsList(
    dragDropState: DragAndDropState,
    modifier: Modifier,
    list: List<Page>,
    onOptionButtonClicked: (Page) -> Unit
) {


    LazyColumn(modifier = modifier.pointerInput(Unit) {
        detectDragGesturesAfterLongPress(
            onDrag = dragDropState::onDrag,
            onDragStart = dragDropState::onDragStart,
            onDragEnd = dragDropState::onDragEnd,
            onDragCancel = dragDropState::onDragCancel
        )
    }, state = dragDropState.listState) {


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

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun TabItemSelectionDialog(
    modifier: Modifier = Modifier,
    items: List<PageType>,
    onClick: (PageType) -> Unit,
) {
    LazyColumn(
        modifier
    ) {
        items(items) { pageType ->
            Surface(
                onClick = {
                    onClick(pageType)
                },
            ) {
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                        .padding(horizontal = 32.dp, vertical = 16.dp)
                        .fillMaxWidth(),
                ) {
                    Text(
                        PageTypeHelper.nameByPageType(LocalContext.current, pageType),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
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

fun LazyListState.getVisibleItemInfoFor(absoluteIndex: Int): LazyListItemInfo? {
    return this
        .layoutInfo
        .visibleItemsInfo
        .getOrNull(absoluteIndex - this.layoutInfo.visibleItemsInfo.first().index)
}

@Composable
fun rememberDragDropListState(
    lazyListState: LazyListState = rememberLazyListState(),
    scope: CoroutineScope,
    onMove: (Int, Int) -> Unit
): DragAndDropState {
    return remember { DragAndDropState(listState = lazyListState, scope = scope, onMove = onMove) }
}


class DragAndDropState(
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