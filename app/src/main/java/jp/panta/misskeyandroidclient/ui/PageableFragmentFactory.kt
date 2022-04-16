package jp.panta.misskeyandroidclient.ui

import androidx.fragment.app.Fragment
import net.pantasystem.milktea.data.model.account.page.Page
import net.pantasystem.milktea.data.model.account.page.Pageable
import jp.panta.misskeyandroidclient.ui.gallery.GalleryPostsFragment
import jp.panta.misskeyandroidclient.ui.notes.view.TimelineFragment
import jp.panta.misskeyandroidclient.ui.notes.view.detail.NoteDetailFragment
import jp.panta.misskeyandroidclient.ui.notification.NotificationFragment
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