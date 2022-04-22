package net.pantasystem.milktea.model.drive

import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.model.account.Account

interface DriveDirectoryPagingStore {
    val state: Flow<PageableState<List<Directory>>>
    suspend fun loadPrevious()
    suspend fun clear()
    suspend fun setAccount(account: Account?)
    suspend fun setCurrentDirectory(directory: Directory?)
    fun onCreated(directory: Directory)
    fun onDeleted(directory: Directory)
}