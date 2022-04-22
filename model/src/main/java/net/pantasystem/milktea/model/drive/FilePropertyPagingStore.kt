package net.pantasystem.milktea.model.drive

import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.model.account.Account

interface FilePropertyPagingStore {
    val state: Flow<PageableState<List<FileProperty.Id>>>
    val isLoading: Boolean

    suspend fun loadPrevious()
    suspend fun clear()
    suspend fun setCurrentDirectory(directory: Directory?)
    suspend fun setCurrentAccount(account: Account?)
    fun onCreated(id: FileProperty.Id)
}