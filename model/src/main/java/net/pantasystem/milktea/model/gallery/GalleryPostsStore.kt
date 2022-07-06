package net.pantasystem.milktea.model.gallery

import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.paginator.StateLocker
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.Pageable

interface GalleryPostsStore : StateLocker {
    interface Factory {
        fun create(pageable: Pageable.Gallery,
                   getAccount: suspend () -> Account,): GalleryPostsStore
    }
    val state: Flow<PageableState<List<GalleryPost.Id>>>

    suspend fun loadPrevious()
    suspend fun loadFuture()
    suspend fun clear()
}