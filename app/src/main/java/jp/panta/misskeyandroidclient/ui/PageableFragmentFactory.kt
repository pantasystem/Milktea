package jp.panta.misskeyandroidclient.ui

import androidx.fragment.app.Fragment
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.account.page.Pageable
import jp.panta.misskeyandroidclient.ui.gallery.GalleryPostsFragment
import jp.panta.misskeyandroidclient.ui.notes.view.TimelineFragment
import jp.panta.misskeyandroidclient.ui.notes.view.detail.NoteDetailFragment
import jp.panta.misskeyandroidclient.ui.notification.NotificationFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

object PageableFragmentFactory {

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    fun create(page: net.pantasystem.milktea.model.account.page.Page): Fragment{
        return when(val pageable = page.pageable()){
            is net.pantasystem.milktea.model.account.page.Pageable.Show ->{
                NoteDetailFragment.newInstance(page)
            }
            is net.pantasystem.milktea.model.account.page.Pageable.Notification ->{
                NotificationFragment()
            }
            is net.pantasystem.milktea.model.account.page.Pageable.Gallery -> {
                return GalleryPostsFragment.newInstance(pageable, page.accountId)
            }
            else ->{
                TimelineFragment.newInstance(page)
            }
        }

    }
}