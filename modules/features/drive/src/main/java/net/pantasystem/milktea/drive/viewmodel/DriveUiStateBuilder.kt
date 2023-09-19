package net.pantasystem.milktea.drive.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.initialState
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.drive.Directory
import net.pantasystem.milktea.model.drive.FileProperty
import javax.inject.Inject

class DriveUiStateBuilder @Inject constructor() {

    operator fun invoke(
        currentAccountFlow: Flow<Account?>,
        currentDirectoryFlow: Flow<Directory?>,
        directoriesStateFlow: Flow<PageableState<List<Directory>>>,
        filesStateFlow: Flow<PageableState<List<FileProperty>>>,
        fileCardDropDownedFlow: Flow<FileProperty.Id?>,
        maxSelectableSizeFlow: Flow<Int?>,
        modesFlow: Flow<Modes>,
        selectedFileIdsFlow: Flow<List<FileProperty.Id>>,
        coroutineScope: CoroutineScope,
    ): Flow<DriveUiState> {
        val filesState = combine(
            filesStateFlow,
            selectedFileIdsFlow,
            fileCardDropDownedFlow,
            maxSelectableSizeFlow
        ) { files, selected, dropdown, maxSize ->
            files.convert { state ->
                state.map {
                    FileViewData(
                        fileProperty = it,
                        isSelected = selected.contains(it.id),
                        isDropdownMenuExpanded = dropdown == it.id,
                        isEnabled = (maxSize == null || selected.size < maxSize) || selected.contains(it.id),
                    )
                }
            }
        }.stateIn(
            coroutineScope,
            SharingStarted.WhileSubscribed(5_000),
            PageableState.initialState(),
        )

        val pagingStateFlow = combine(
            directoriesStateFlow,
            filesState,
        ) { dState, fState ->
            PagingState(
                directoriesState = dState,
                driveFilesState = fState,
            )
        }.stateIn(
            coroutineScope,
            SharingStarted.WhileSubscribed(5_000),
            PagingState(),
        )

        return combine(
            currentAccountFlow,
            currentDirectoryFlow,
            pagingStateFlow,
            modesFlow,
            selectedFileIdsFlow,
        ) { ac, dir, pagingState, mode, selected ->
            DriveUiState(
                currentAccount = ac,
                currentDirectory = dir,
                directoriesState = pagingState.directoriesState,
                driveFilesState = pagingState.driveFilesState,
                selectedFilePropertyIds = selected,
                actionMode = mode.actionMode,
                viewMode = mode.viewMode,
                isSelectMode = mode.isSelectMode || mode.maxSelectableSize != null && mode.maxSelectableSize > 0,
                maxSelectableSize = mode.maxSelectableSize,
            )
        }
    }
}