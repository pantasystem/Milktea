package net.pantasystem.milktea.setting.compose.tab

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.account.page.PageType
import net.pantasystem.milktea.setting.R


@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun TabItemsListScreen(
    dragDropState: DragAndDropState,
    pageTypes: List<PageType>,
    list: List<Page>,
    onSelectPage: (PageType) -> Unit,
    onOptionButtonClicked: (Page) -> Unit,
    onNavigateUp: () -> Unit

    ) {
    val scaffoldState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    val scope = rememberCoroutineScope()


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


