package jp.panta.misskeyandroidclient.view.search

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.PagerAdapter
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.NoteType
import jp.panta.misskeyandroidclient.view.notes.TimelineFragment
import kotlinx.android.synthetic.main.fragment_search_top.*

class SearchTopFragment : Fragment(){
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search_top, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = activity ?: return
        search_view_pager.adapter = SearchPagerAdapter(activity.supportFragmentManager, context!!)
        search_tab_layout.setupWithViewPager(search_view_pager)
    }

    class SearchPagerAdapter(supportFragmentManager: FragmentManager, private val context: Context) : FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){
        val tabList = listOf(context.getString(R.string.title_featured))
        override fun getCount(): Int {
            return tabList.size
        }

        override fun getItem(position: Int): Fragment {
            return TimelineFragment.newInstance(NoteRequest.Setting(type = NoteType.FEATURED))
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return tabList[position]
        }
    }
}