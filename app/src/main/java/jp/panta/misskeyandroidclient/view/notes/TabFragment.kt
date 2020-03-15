package jp.panta.misskeyandroidclient.view.notes

import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.Observer
import jp.panta.misskeyandroidclient.KeyStore
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.NoteType
import jp.panta.misskeyandroidclient.view.ScrollableTop
import jp.panta.misskeyandroidclient.view.notes.detail.NoteDetailFragment
import kotlinx.android.synthetic.main.fragment_tab.*

class TabFragment : Fragment(), ScrollableTop{


    private val defaultTabType = listOf(NoteType.HOME, NoteType.SOCIAL, NoteType.GLOBAL)

    private lateinit var mPagerAdapter: TimelinePagerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //return super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val miApp = context?.applicationContext as MiApplication
        //val connectionInstance = miApp.currentConnectionInstanceLiveData.value
        //val i = connectionInstance?.getI()

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context)
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

            mPagerAdapter = TimelinePagerAdapter(activity?.supportFragmentManager, settings)
            viewPager.adapter = mPagerAdapter
            tabLayout.setupWithViewPager(viewPager)

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

    class TimelinePagerAdapter(supportFragmentManager: FragmentManager?, val requestBaseList: List<NoteRequest.Setting>) : FragmentPagerAdapter(supportFragmentManager!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){

        val scrollableTopFragments = ArrayList<ScrollableTop>()

        override fun getCount(): Int {
            return requestBaseList.size
        }

        override fun getItem(p0: Int): Fragment {
            Log.d("getItem", "$p0, ${requestBaseList[p0].type}")
            val item = requestBaseList[p0]
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

        override fun getPageTitle(position: Int): CharSequence? {
            val requestBase = requestBaseList[position]
            return requestBase.title
        }

    }

    override fun showTop() {
        showTopCurrentFragment()
    }

    private fun showTopCurrentFragment(){
        mPagerAdapter.scrollableTopFragments.forEach{
            it.showTop()
        }
    }


}