package jp.panta.misskeyandroidclient.ui

import androidx.fragment.app.Fragment
import jp.panta.misskeyandroidclient.ui.notes.view.TimelineFragment
import jp.panta.misskeyandroidclient.ui.notes.view.detail.NoteDetailFragment
import jp.panta.misskeyandroidclient.ui.notification.NotificationFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import net.pantasystem.milktea.gallery.GalleryPostsFragment
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.account.page.Pageable

object PageableFragmentFactory {

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

    fun create(pageable: Pageable): Fragment {
        return when(pageable){
            is Pageable.Show ->{
                NoteDetailFragment.newInstance(pageable.noteId)
            }
            is Pageable.Notification ->{
                NotificationFragment()
            }
            is Pageable.Gallery -> {
                return GalleryPostsFragment.newInstance(pageable, null)
            }
            else ->{
                TimelineFragment.newInstance(pageable)
            }
        }
    }
}