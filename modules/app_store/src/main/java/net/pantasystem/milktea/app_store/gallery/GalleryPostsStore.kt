package net.pantasystem.milktea.app_store.gallery

import kotlinx.coroutines.flow.Flow
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.paginator.StateLocker
import net.pantasystem.milktea.model.account.Account
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.gallery.GalleryPost

interface GalleryPostsStore : StateLocker {
    interface Factory {
        fun create(pageable: Pageable.Gallery,
                   getAccount: suspend () -> Account,): GalleryPostsStore
    }
    val state: Flow<PageableState<List<GalleryPost.Id>>>

    suspend fun loadPrevious(): Result<Int>
    suspend fun loadFuture(): Result<Int>
    suspend fun clear()
}