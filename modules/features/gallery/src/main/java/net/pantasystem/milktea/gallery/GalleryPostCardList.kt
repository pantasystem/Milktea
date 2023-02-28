package net.pantasystem.milktea.gallery

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common.ui.isScrolledToTheEnd
import net.pantasystem.milktea.gallery.viewmodel.GalleryPostsViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GalleryPostCardList(
    viewModel: GalleryPostsViewModel,
    onAction: (GalleryPostCardAction) -> Unit
) {
    val listViewState = rememberLazyListState()
    val visibleFileIds by viewModel.visibleFileIds.collectAsState()

    LaunchedEffect(key1 = null) {
        viewModel.loadInit()
    }

    LaunchedEffect(key1 = null) {
        snapshotFlow {
            listViewState.isScrolledToTheEnd() && listViewState.layoutInfo.totalItemsCount != listViewState.layoutInfo.visibleItemsInfo.size
        }.distinctUntilChanged().onEach {
            if (it) {
                viewModel.loadPrevious()
            }
        }.launchIn(this)
    }

    val state by viewModel.galleryPosts.collectAsState()

    val content = state.content
    SwipeRefresh(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(rememberNestedScrollInteropConnection()),
        state = rememberSwipeRefreshState(isRefreshing = state is PageableState.Loading.Init),
        onRefresh = {
            viewModel.loadInit()
        }
    ) {
        if (content is StateContent.Exist) {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                state = listViewState,
            ) {
                items(content.rawContent) { post ->
                    GalleryPostCard(
                        galleryState = post,
                        onAction = {
                            if (
                                it is GalleryPostCardAction.OnThumbnailClicked
                                && (!visibleFileIds.contains(it.fileProperty.id) && it.fileProperty.isSensitive)
                            ) {
                                viewModel.toggleFileVisibleState(it.fileProperty.id)
                            } else {
                                onAction.invoke(it)
                            }
                        },
                        visibleFileIds = visibleFileIds
                    )
                }
                if (state is PageableState.Loading.Previous) {
                    item {
                        Box(
                            Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        } else {
            Box(Modifier.fillMaxSize()) {
                when (state) {

                    is PageableState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is PageableState.Error -> {
                        Text("Load error")
                    }
                    is PageableState.Fixed -> {
                        Text("No contents")
                    }
                }
            }
        }
    }


}