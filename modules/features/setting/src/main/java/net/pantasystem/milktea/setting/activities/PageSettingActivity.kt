package net.pantasystem.milktea.setting.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.*
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.composethemeadapter.MdcTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import net.pantasystem.milktea.common.ui.ApplyTheme
import net.pantasystem.milktea.common_navigation.*
import net.pantasystem.milktea.common_navigation.SearchAndSelectUserNavigation.Companion.EXTRA_SELECTED_USER_CHANGED_DIFF
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
//        val binding = DataBindingUtil.setContentView<ActivityPageSettingBinding>(this,
//            R.layout.activity_page_setting
//        )
//        binding.lifecycleOwner = this
//        setSupportActionBar(binding.pageSettingToolbar)
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)
//
//
//
//        val touchHelper = ItemTouchHelper(ItemTouchCallback())
//        touchHelper.attachToRecyclerView(binding.pagesView)
//        binding.pagesView.addItemDecoration(touchHelper)
//
//        val pagesAdapter = PagesAdapter(mPageSettingViewModel)
//        binding.pagesView.adapter = pagesAdapter
//        binding.pagesView.layoutManager = LinearLayoutManager(this)
//
//        mPageSettingViewModel.selectedPages.observe(this) {
//            Log.d("PageSettingActivity", "選択済みページが更新された")
//            pagesAdapter.submitList(it)
//        }
//
//        binding.addPageButton.setOnClickListener {
//            SelectPageToAddDialog().show(supportFragmentManager, "Activity")
//        }
//
//        mPageSettingViewModel.pageOnActionEvent.observe(this) {
//            PageSettingActionDialog().show(supportFragmentManager, "PSA")
//        }
//
//        mPageSettingViewModel.pageOnUpdateEvent.observe(this) {
//            EditTabNameDialog().show(supportFragmentManager, "ETD")
//        }
//
//        mPageSettingViewModel.pageAddedEvent.observe(this) { pt ->
//            when (pt) {
//                PageType.SEARCH, PageType.SEARCH_HASH -> startActivity(
//                    searchNavigation.newIntent(SearchNavType.SearchScreen())
//                )
//                PageType.USER -> {
//                    val intent =
//                        searchAndSelectUserNavigation.newIntent(SearchAndSelectUserNavigationArgs( selectableMaximumSize = 1))
//                    launchSearchAndSelectUserForAddUserTimelineTab.launch(intent)
//                }
//                PageType.USER_LIST -> startActivity(userListNavigation.newIntent(UserListArgs()))
//                PageType.DETAIL -> startActivity(searchNavigation.newIntent(SearchNavType.SearchScreen()))
//                PageType.ANTENNA -> startActivity(antennaNavigation.newIntent(Unit))
//                PageType.USERS_GALLERY_POSTS -> {
//                    val intent =
//                        searchAndSelectUserNavigation.newIntent(SearchAndSelectUserNavigationArgs( selectableMaximumSize = 1))
//                    launchSearchAndSelectUserForAddGalleryTab.launch(intent)
//                }
//                PageType.CHANNEL_TIMELINE -> {
//                    val intent = channelNavigation.newIntent(Unit)
//                    startActivity(intent)
//                }
//                else -> {
//                    // auto add
//                }
//            }
//        }


        setContent {
            MdcTheme {
                var list by remember {
                    mutableStateOf((0 until 100).map { it.toString() })
                }
                Scaffold() {
                    ReorderList(modifier = Modifier
                        .fillMaxSize()
                        .padding(it),
                        list = list,
                        onMove = { from, to ->
                            val tmp = list[to]
                            val mutable = list.toMutableList()
                            mutable[to] = mutable[from]
                            mutable[from] = tmp
                            list = mutable
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

    inner class ItemTouchCallback : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN,
        ItemTouchHelper.ACTION_STATE_IDLE
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val from = viewHolder.absoluteAdapterPosition
            val to = target.absoluteAdapterPosition
            val exList = ArrayList(mPageSettingViewModel.selectedPages.value ?: emptyList())
            val d = exList.removeAt(from)
            exList.add(to, d)
            mPageSettingViewModel.setList(exList)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) = Unit
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
fun ReorderList(
    modifier: Modifier,
    list: List<String>,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit
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
            Box(modifier = Modifier.graphicsLayer {
                translationY = dragDropState.targetElementTranslateY
                    .takeIf {
                        index == dragDropState.currentIndexOfDraggedItem
                    }?: 0f
            }) {
                Text(item)
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
            ?.let { item -> (initiallyDraggedElement?.offset ?: 0f).toFloat() + draggedDistance - item.offset }


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