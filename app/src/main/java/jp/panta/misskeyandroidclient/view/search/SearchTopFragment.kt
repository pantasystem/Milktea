package jp.panta.misskeyandroidclient.view.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.PagerAdapter
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.SearchActivity
import jp.panta.misskeyandroidclient.model.Page
import jp.panta.misskeyandroidclient.model.account.page.Pageable
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.setMenuTint
import jp.panta.misskeyandroidclient.view.explore.ExploreFragment
import jp.panta.misskeyandroidclient.view.notes.TimelineFragment
import kotlinx.android.synthetic.main.fragment_search_top.*
import java.lang.IllegalArgumentException

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
        search_view_pager.adapter = SearchPagerAdapter(activity.supportFragmentManager, requireContext())
        search_tab_layout.setupWithViewPager(search_view_pager)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.search_top_menu, menu)
        context?.setMenuTint(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            //R.id.search ->
            R.id.search ->{
                activity?.startActivity(Intent(this.context, SearchActivity::class.java))
                activity?.overridePendingTransition(0, 0)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setHasOptionsMenu(true)
    }
    class SearchPagerAdapter(supportFragmentManager: FragmentManager, context: Context) : FragmentPagerAdapter(supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){
        val tabList = listOf(context.getString(R.string.title_featured), context.getString(R.string.explore))
        override fun getCount(): Int {
            return tabList.size
        }

        override fun getItem(position: Int): Fragment {
            return when(position){
                0 -> TimelineFragment.newInstance(Pageable.Featured(null))
                1 -> ExploreFragment()
                else -> throw IllegalArgumentException("range 0..1, list:$tabList")
            }
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return tabList[position]
        }
    }
}