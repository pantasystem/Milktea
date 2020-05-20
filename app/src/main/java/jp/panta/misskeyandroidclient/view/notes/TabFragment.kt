package jp.panta.misskeyandroidclient.view.notes

import android.content.Context
import android.os.Bundle
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import jp.panta.misskeyandroidclient.KeyStore
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.Page
import jp.panta.misskeyandroidclient.util.getPreferenceName
import jp.panta.misskeyandroidclient.view.PageableFragmentFactory
import jp.panta.misskeyandroidclient.view.ScrollableTop
import jp.panta.misskeyandroidclient.view.notes.detail.NoteDetailFragment
import jp.panta.misskeyandroidclient.view.notification.NotificationFragment
import jp.panta.misskeyandroidclient.view.settings.page.PageTypeNameMap
import jp.panta.misskeyandroidclient.viewmodel.setting.page.PageableTemplate
import kotlinx.android.synthetic.main.fragment_tab.*

class TabFragment : Fragment(R.layout.fragment_tab), ScrollableTop{



    private var mPagerAdapter: TimelinePagerAdapter? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val miApp = context?.applicationContext as MiApplication

        val sharedPreferences = requireContext().getSharedPreferences(requireContext().getPreferenceName(), Context.MODE_PRIVATE)
        val includeMyRenotes = sharedPreferences.getBoolean(KeyStore.BooleanKey.INCLUDE_MY_RENOTES.name, true)
        val includeRenotedMyNotes = sharedPreferences.getBoolean(KeyStore.BooleanKey.INCLUDE_RENOTED_MY_NOTES.name, true)
        val includeLocalRenotes = sharedPreferences.getBoolean(KeyStore.BooleanKey.INCLUDE_LOCAL_RENOTES.name, true)

        Log.d("TabFragment", "設定:$includeLocalRenotes, $includeRenotedMyNotes, $includeMyRenotes")
        miApp.currentAccount.observe(viewLifecycleOwner, Observer { accountRelation ->
            var pages = accountRelation.pages
            pages = if(pages.isNullOrEmpty()){
                PageableTemplate.makeDefaultPages(PageTypeNameMap(requireContext())).apply{
                    forEach {
                        it.accountId = accountRelation.account.id
                    }
                }
            }else pages


            Log.d("TabFragment", "pages:$pages")

            mPagerAdapter = viewPager.adapter as? TimelinePagerAdapter
            if(mPagerAdapter == null){
                mPagerAdapter = TimelinePagerAdapter(this, emptyList())
                viewPager.adapter = mPagerAdapter
            }
            mPagerAdapter?.setList(pages.sortedBy {
                it.pageNumber
            })
            //mPagerAdapter?.notifyDataSetChanged()

            val mediator = TabLayoutMediator(tabLayout, viewPager){ tab: TabLayout.Tab, position: Int ->
                tab.text = mPagerAdapter?.getPageTitle(position)
            }
            mediator.attach()


            if(pages.size <= 1){
                tabLayout.visibility = View.GONE
                elevationView.visibility = View.VISIBLE
            }else{
                tabLayout.visibility = View.VISIBLE
                elevationView.visibility = View.GONE
                tabLayout.elevation
            }
        })

    }


    class TimelinePagerAdapter(fragment: Fragment, list: List<Page>) : FragmentStateAdapter(fragment){
        var requestBaseList: List<Page> = list
            private set
        private var oldRequestBaseSetting = requestBaseList

        val scrollableTopFragments = ArrayList<ScrollableTop>()

        override fun createFragment(position: Int): Fragment {
            Log.d("getItem", "$position, ${requestBaseList[position].pageable()?.javaClass}")
            val item = requestBaseList[position]
            val fragment = PageableFragmentFactory.create(item.pageable())

            if(fragment is ScrollableTop){
                scrollableTopFragments.add(fragment)
            }
            return fragment
        }

        fun getPageTitle(position: Int): String{
            val page = requestBaseList[position]
            return page.title
        }

        override fun getItemCount(): Int = requestBaseList.size

        private val diffCallback = object : DiffUtil.Callback(){
            override fun areContentsTheSame(
                oldItemPosition: Int,
                newItemPosition: Int
            ): Boolean {
                return oldRequestBaseSetting[oldItemPosition] == requestBaseList[newItemPosition]
            }

            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return oldRequestBaseSetting[oldItemPosition].id == requestBaseList[newItemPosition].id
            }

            override fun getNewListSize(): Int {
                return requestBaseList.size
            }

            override fun getOldListSize(): Int {
                return oldRequestBaseSetting.size
            }
        }

        override fun getItemId(position: Int): Long {
            val item = requestBaseList[position]
            return requestBaseList[position].id?: item.hashCode().toLong() + (item.accountId?.hashCode()?: 0)
        }

        fun setList(list: List<Page>){
            oldRequestBaseSetting = requestBaseList
            requestBaseList = list

            val result = DiffUtil.calculateDiff(diffCallback)
            result.dispatchUpdatesTo(this)
        }



    }

    override fun showTop() {
        showTopCurrentFragment()
    }

    private fun showTopCurrentFragment(){
        try{
            mPagerAdapter?.scrollableTopFragments?.forEach{
                it.showTop()
            }
        }catch(e: UninitializedPropertyAccessException){

        }

    }


}