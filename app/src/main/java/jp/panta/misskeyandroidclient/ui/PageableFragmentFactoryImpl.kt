package jp.panta.misskeyandroidclient.ui

import androidx.fragment.app.Fragment
import jp.panta.misskeyandroidclient.ui.notification.NotificationFragment
import net.pantasystem.milktea.common_android_ui.PageableFragmentFactory
import net.pantasystem.milktea.gallery.GalleryPostsFragment
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.note.view.TimelineFragment
import net.pantasystem.milktea.note.view.detail.NoteDetailFragment
import javax.inject.Inject


class PageableFragmentFactoryImpl @Inject constructor(): PageableFragmentFactory {

    override fun create(page: Page): Fragment {
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

    override fun create(pageable: Pageable): Fragment {
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