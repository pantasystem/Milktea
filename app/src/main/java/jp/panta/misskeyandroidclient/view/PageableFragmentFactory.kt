package jp.panta.misskeyandroidclient.view

import androidx.fragment.app.Fragment
import jp.panta.misskeyandroidclient.model.Page
import jp.panta.misskeyandroidclient.model.Pageable
import jp.panta.misskeyandroidclient.view.notes.TimelineFragment
import jp.panta.misskeyandroidclient.view.notes.detail.NoteDetailFragment
import jp.panta.misskeyandroidclient.view.notification.NotificationFragment
import java.lang.IllegalArgumentException

object PageableFragmentFactory {

    fun create(pageable: Pageable?): Fragment{
        return when(pageable){
            is Page.Timeline ->{
                TimelineFragment.newInstance(pageable)
            }
            is Page.Show ->{
                NoteDetailFragment.newInstance(pageable)
            }
            is Page.Notification ->{
                NotificationFragment()
            }
            else -> throw IllegalArgumentException("unknown type")
        }
    }
}