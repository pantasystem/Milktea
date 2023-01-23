package jp.panta.misskeyandroidclient.ui

import androidx.fragment.app.Fragment
import net.pantasystem.milktea.common_android_ui.PageableFragmentFactory
import net.pantasystem.milktea.gallery.GalleryPostsFragment
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.note.detail.NoteDetailFragment
import net.pantasystem.milktea.note.timeline.TimelineFragment
import net.pantasystem.milktea.notification.NotificationFragment
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

    override fun create(accountId: Long?, pageable: Pageable): Fragment {
        if (accountId == null) {
            return create(pageable)
        }
        return when(pageable){
            is Pageable.Show ->{
                NoteDetailFragment.newInstance(pageable.noteId, accountId)
            }
            is Pageable.Notification ->{
                NotificationFragment()
            }
            is Pageable.Gallery -> {
                return GalleryPostsFragment.newInstance(pageable, null)
            }
            else ->{
                TimelineFragment.newInstance(pageable, accountId)
            }
        }
    }
}