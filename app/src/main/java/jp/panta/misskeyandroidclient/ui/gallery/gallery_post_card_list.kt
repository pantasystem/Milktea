package jp.panta.misskeyandroidclient.ui.gallery

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import jp.panta.misskeyandroidclient.ui.gallery.viewmodel.GalleryPostsViewModel
import jp.panta.misskeyandroidclient.util.compose.isScrolledToTheEnd
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent

@Composable
fun GalleryPostCardList(viewModel: GalleryPostsViewModel) {
    val listViewState = rememberLazyListState()

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
        }
    }

    val state by viewModel.galleryPosts.collectAsState()

    val content = state.content
    if (content is StateContent.Exist) {

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(content.rawContent) { post ->
                GalleryPostCard(galleryState = post, onAction = {

                })
            }
            if (state is PageableState.Loading.Previous) {
                item {
                    CircularProgressIndicator()
                }
            }
        }
    } else {
        Box(Modifier.fillMaxSize()) {
            when(state) {

                is PageableState.Loading -> {
                    Box(Modifier.fillMaxSize()) {
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