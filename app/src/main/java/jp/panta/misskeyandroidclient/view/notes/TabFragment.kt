package jp.panta.misskeyandroidclient.view.notes

import android.content.Context
import android.os.Bundle
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DiffUtil
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import jp.panta.misskeyandroidclient.KeyStore
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.NoteType
import jp.panta.misskeyandroidclient.util.getPreferenceName
import jp.panta.misskeyandroidclient.view.ScrollableTop
import jp.panta.misskeyandroidclient.view.notes.detail.NoteDetailFragment
import kotlinx.android.synthetic.main.fragment_tab.*

class TabFragment : Fragment(), ScrollableTop{


    private val defaultTabType = listOf(NoteType.HOME, NoteType.SOCIAL, NoteType.GLOBAL)

    private var mPagerAdapter: TimelinePagerAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //return super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val miApp = context?.applicationContext as MiApplication

        val sharedPreferences = requireContext().getSharedPreferences(requireContext().getPreferenceName(), Context.MODE_PRIVATE)
        val includeMyRenotes = sharedPreferences.getBoolean(KeyStore.BooleanKey.INCLUDE_MY_RENOTES.name, true)
        val includeRenotedMyNotes = sharedPreferences.getBoolean(KeyStore.BooleanKey.INCLUDE_RENOTED_MY_NOTES.name, true)
        val includeLocalRenotes = sharedPreferences.getBoolean(KeyStore.BooleanKey.INCLUDE_LOCAL_RENOTES.name, true)

        Log.d("TabFragment", "設定:$includeLocalRenotes, $includeRenotedMyNotes, $includeMyRenotes")
        miApp.currentAccount.observe(viewLifecycleOwner, Observer { accountRelation ->
            var settings = accountRelation.pages
            settings = if(settings.isNullOrEmpty()){
                makeDefaultNoteSetting(defaultTabType)
            }else settings

            settings.forEach{setting ->
                setting.includeLocalRenotes = includeLocalRenotes
                setting.includeMyRenotes = includeMyRenotes
                setting.includeRenotedMyNotes = includeRenotedMyNotes
            }

            if(mPagerAdapter == null){
                mPagerAdapter = TimelinePagerAdapter(this, emptyList())
                viewPager.adapter = mPagerAdapter
            }
            mPagerAdapter?.setList(settings.sortedBy {
                it.weight
            })
            //mPagerAdapter?.notifyDataSetChanged()

            val mediator = TabLayoutMediator(tabLayout, viewPager){ tab: TabLayout.Tab, position: Int ->
                tab.text = mPagerAdapter?.getPageTitle(position)
            }
            mediator.attach()


            if(settings.size <= 1){
                tabLayout.visibility = View.GONE
                elevationView.visibility = View.VISIBLE
            }else{
                tabLayout.visibility = View.VISIBLE
                elevationView.visibility = View.GONE
            }
        })

    }

    private fun makeDefaultNoteSetting(list: List<NoteType>): List<NoteRequest.Setting>{
        return list.map{
            NoteRequest.Setting(type = it)
        }
    }

    class TimelinePagerAdapter(fragment: Fragment, list: List<NoteRequest.Setting>) : FragmentStateAdapter(fragment){
        var requestBaseList: List<NoteRequest.Setting> = list
            private set
        private var oldRequestBaseSetting = requestBaseList

        val scrollableTopFragments = ArrayList<ScrollableTop>()

        override fun createFragment(position: Int): Fragment {
            Log.d("getItem", "$position, ${requestBaseList[position].type}")
            val item = requestBaseList[position]
            val noteId = item.noteId
            val fragment =  if(item.type == NoteType.DETAIL && noteId != null){
                NoteDetailFragment.newInstance(noteId)
            }else{
                TimelineFragment.newInstance(item)
            }
            if(fragment is ScrollableTop){
                scrollableTopFragments.add(fragment)
            }
            return fragment
        }

        fun getPageTitle(position: Int): String{
            val requestBase = requestBaseList[position]
            return requestBase.title
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

        fun setList(list: List<NoteRequest.Setting>){
            oldRequestBaseSetting = requestBaseList
            requestBaseList = list

            val result = DiffUtil.calculateDiff(diffCallback)
            result.dispatchUpdatesTo(this)
        }

        override fun getItemId(position: Int): Long {
            return requestBaseList[position].id?: super.getItemId(position)
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