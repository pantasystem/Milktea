package net.pantasystem.milktea.common.ui

import androidx.compose.foundation.lazy.LazyListState

fun LazyListState.isScrolledToTheEnd() = layoutInfo.visibleItemsInfo.lastOrNull()?.index == layoutInfo.totalItemsCount - 1