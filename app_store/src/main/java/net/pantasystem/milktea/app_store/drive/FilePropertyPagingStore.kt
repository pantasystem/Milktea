package net.pantasystem.milktea.app_store.drive

import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.drive.Directory
import net.pantasystem.milktea.model.drive.FileProperty

interface FilePropertyPagingStore {
    val state: Flow<PageableState<List<FileProperty.Id>>>
    val isLoading: Boolean

    suspend fun loadPrevious()
    suspend fun clear()
    suspend fun setCurrentDirectory(directory: Directory?)
    suspend fun setCurrentAccount(account: Account?)
    fun onCreated(id: FileProperty.Id)
}