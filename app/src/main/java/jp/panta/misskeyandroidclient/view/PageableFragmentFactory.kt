package jp.panta.misskeyandroidclient.view

import androidx.fragment.app.Fragment
import jp.panta.misskeyandroidclient.model.account.page.Page
import jp.panta.misskeyandroidclient.model.account.page.Pageable
import jp.panta.misskeyandroidclient.view.gallery.GalleryPostsFragment
import jp.panta.misskeyandroidclient.view.notes.TimelineFragment
import jp.panta.misskeyandroidclient.view.notes.detail.NoteDetailFragment
import jp.panta.misskeyandroidclient.view.notification.NotificationFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

object PageableFragmentFactory {

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    fun create(page: Page): Fragment{
        return when(val pageable = page.pageable()){
            is Pageable.Show ->{
                NoteDetailFragment.newInstance(page)
            }
            is Pageable.Notification ->{
                NotificationFragment()
            }
            is Pageable.Gallery -> {
                return GalleryPostsFragment.newInstance(pageable, page.accountId)
            }
            else ->{
                TimelineFragment.newInstance(page)
            }
        }

    }
}