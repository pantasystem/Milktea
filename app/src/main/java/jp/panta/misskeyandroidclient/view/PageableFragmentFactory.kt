package jp.panta.misskeyandroidclient.view

import androidx.fragment.app.Fragment
import jp.panta.misskeyandroidclient.model.account.page.Page
import jp.panta.misskeyandroidclient.model.account.page.Pageable

import jp.panta.misskeyandroidclient.model.core.Account
import jp.panta.misskeyandroidclient.view.notes.TimelineFragment
import jp.panta.misskeyandroidclient.view.notes.detail.NoteDetailFragment
import jp.panta.misskeyandroidclient.view.notification.NotificationFragment
import java.lang.IllegalArgumentException

object PageableFragmentFactory {

    fun create(page: Page): Fragment{
        return when(page.pageable()){
            is Pageable.Show ->{
                NoteDetailFragment.newInstance(page)
            }
            is Pageable.Notification ->{
                NotificationFragment()
            }
            else ->{
                TimelineFragment.newInstance(page)
            }
        }

    }
}