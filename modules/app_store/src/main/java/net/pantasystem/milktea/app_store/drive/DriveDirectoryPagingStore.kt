package net.pantasystem.milktea.app_store.drive

import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.drive.Directory

interface DriveDirectoryPagingStore {
    val state: Flow<PageableState<List<Directory>>>
    suspend fun loadPrevious(): Result<Int>
    suspend fun clear()
    suspend fun setAccount(account: Account?)
    suspend fun setCurrentDirectory(directory: Directory?)
    fun onCreated(directory: Directory)
    fun onDeleted(directory: Directory)
}