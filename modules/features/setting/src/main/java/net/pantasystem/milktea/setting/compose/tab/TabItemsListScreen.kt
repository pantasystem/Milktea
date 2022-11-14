package net.pantasystem.milktea.setting.compose.tab

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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

    var bottomSheetType: BottomSheetType by remember {
        mutableStateOf(BottomSheetType.None)
    }

    LaunchedEffect(bottomSheetType) {
        snapshotFlow {
            bottomSheetType
        }.collect { action ->

            when(action) {
                is BottomSheetType.None -> {
                    scaffoldState.hide()
                }
                is BottomSheetType.AddTab -> {
                    scaffoldState.show()
                }
                is BottomSheetType.TabOptionDialog -> {
                    scaffoldState.show()
                }
            }
        }
    }

    ModalBottomSheetLayout(
        sheetState = scaffoldState,
        sheetContent = {
            TabItemSelectionDialog(
                modifier = Modifier.fillMaxSize(),
                items = pageTypes,
                onClick = {
                    bottomSheetType = BottomSheetType.None
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
                        bottomSheetType = if (scaffoldState.isVisible) {
                            BottomSheetType.None
                        } else {
                            BottomSheetType.AddTab
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

sealed interface BottomSheetType {
    object AddTab : BottomSheetType
    data class TabOptionDialog(val page: Page) : BottomSheetType
    object None : BottomSheetType
}

