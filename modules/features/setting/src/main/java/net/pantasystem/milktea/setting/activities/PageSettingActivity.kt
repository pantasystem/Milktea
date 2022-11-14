package net.pantasystem.milktea.setting.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common_navigation.*
import net.pantasystem.milktea.common_navigation.SearchAndSelectUserNavigation.Companion.EXTRA_SELECTED_USER_CHANGED_DIFF
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.account.page.PageType
import net.pantasystem.milktea.setting.EditTabNameDialog
import net.pantasystem.milktea.setting.PageSettingActionDialog
import net.pantasystem.milktea.setting.R
import net.pantasystem.milktea.setting.SelectPageToAddDialog
import net.pantasystem.milktea.setting.viewmodel.page.PageSettingViewModel
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class PageSettingActivity : AppCompatActivity() {


    @Inject
    lateinit var applyTheme: ApplyTheme

    @Inject
    lateinit var searchAndSelectUserNavigation: SearchAndSelectUserNavigation

    @Inject
    lateinit var searchNavigation: SearchNavigation

    @Inject
    lateinit var antennaNavigation: AntennaNavigation

    @Inject
    lateinit var channelNavigation: ChannelNavigation

    @Inject
    lateinit var userListNavigation: UserListNavigation

    private val mPageSettingViewModel: PageSettingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()

        mPageSettingViewModel.pageOnActionEvent.observe(this) {
            PageSettingActionDialog().show(supportFragmentManager, "PSA")
        }

        mPageSettingViewModel.pageOnUpdateEvent.observe(this) {
            EditTabNameDialog().show(supportFragmentManager, "ETD")
        }
//
        mPageSettingViewModel.pageAddedEvent.observe(this) { pt ->
            when (pt) {
                PageType.SEARCH, PageType.SEARCH_HASH -> startActivity(
                    searchNavigation.newIntent(SearchNavType.SearchScreen())
                )
                PageType.USER -> {
                    val intent =
                        searchAndSelectUserNavigation.newIntent(SearchAndSelectUserNavigationArgs( selectableMaximumSize = 1))
                    launchSearchAndSelectUserForAddUserTimelineTab.launch(intent)
                }
                PageType.USER_LIST -> startActivity(userListNavigation.newIntent(UserListArgs()))
                PageType.DETAIL -> startActivity(searchNavigation.newIntent(SearchNavType.SearchScreen()))
                PageType.ANTENNA -> startActivity(antennaNavigation.newIntent(Unit))
                PageType.USERS_GALLERY_POSTS -> {
                    val intent =
                        searchAndSelectUserNavigation.newIntent(SearchAndSelectUserNavigationArgs( selectableMaximumSize = 1))
                    launchSearchAndSelectUserForAddGalleryTab.launch(intent)
                }
                PageType.CHANNEL_TIMELINE -> {
                    val intent = channelNavigation.newIntent(Unit)
                    startActivity(intent)
                }
                else -> {
                    // auto add
                }
            }
        }


        setContent {
            MdcTheme {
                val list by mPageSettingViewModel.selectedPages.collectAsState()

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
                                        finish()
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
                                SelectPageToAddDialog().show(supportFragmentManager, "Activity")
                            }
                        )
                    },
                    floatingActionButtonPosition = FabPosition.Center
                ) {
                    TabItemsList(modifier = Modifier
                        .fillMaxSize()
                        .padding(it),
                        list = list,
                        onMove = { from, to ->
                            val tmp = list[to]
                            val mutable = list.toMutableList()
                            mutable[to] = mutable[from]
                            mutable[from] = tmp
                            mPageSettingViewModel.setList(mutable)
                        },
                        onOptionButtonClicked = { page ->
                            mPageSettingViewModel.pageOnActionEvent.event = page
                        }
                    )
                }
            }
        }

    }

    override fun onPause() {
        super.onPause()
        mPageSettingViewModel.save()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }


    private val launchSearchAndSelectUserForAddGalleryTab =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK && it.data != null) {
                val changeDiff =
                    it.data!!.getSerializableExtra(EXTRA_SELECTED_USER_CHANGED_DIFF) as ChangedDiffResult
                mPageSettingViewModel.addUsersGalleryByIds(changeDiff.selected)
            }
        }

    private val launchSearchAndSelectUserForAddUserTimelineTab =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK && it.data != null) {
                val changeDiff =
                    it.data!!.getSerializableExtra(EXTRA_SELECTED_USER_CHANGED_DIFF) as ChangedDiffResult
                mPageSettingViewModel.addUserPageByIds(changeDiff.selected)
            }
        }


}


@Composable
fun TabItemsList(
    modifier: Modifier,
    list: List<Page>,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit,
    onOptionButtonClicked: (Page) -> Unit,

) {

    val scope = rememberCoroutineScope()

    val dragDropState = rememberDragDropListState(onMove = onMove, scope = scope)


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

                Divider(modifier = Modifier.fillMaxWidth())
            }
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
    private var draggedDistance by mutableStateOf<Float>(0f)

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