package jp.panta.misskeyandroidclient.view.notes

import android.content.SharedPreferences
import android.content.pm.PackageManager
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
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.SecretConstant
import jp.panta.misskeyandroidclient.model.notes.NoteRequest
import jp.panta.misskeyandroidclient.model.notes.NoteType
import kotlinx.android.synthetic.main.fragment_tab.*

class TabFragment : Fragment(){

    companion object{

        fun localizationTitle(requestBase: NoteRequest.Setting): String?{
            return when(requestBase.type){
                NoteType.HOME -> "Home"
                NoteType.LOCAL -> "Local"
                NoteType.SOCIAL -> "Social"
                NoteType.GLOBAL -> "Global"
                NoteType.SEARCH, NoteType.SEARCH_HASH -> requestBase.query
                NoteType.USER -> requestBase.userId
                NoteType.FAVORITE -> "Favorite"
                NoteType.FEATURED -> "Featured"
                NoteType.DETAIL -> "Detail"
            }
        }

    }
    private val defaultTabType = listOf(NoteType.HOME, NoteType.SOCIAL, NoteType.GLOBAL)


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
        val includeMyRenotes = sharedPreferences.getBoolean("includeMyRenotes", true)
        val includeRenotedMyNotes = sharedPreferences.getBoolean("includeRenotedMyNotes", true)
        val includeLocalRenotes = sharedPreferences.getBoolean("includeLocalRenotes", true)

        Log.d("TabFragment", "設定:$includeLocalRenotes, $includeRenotedMyNotes, $includeMyRenotes")
        miApp.noteRequestSettingDao?.findAll()?.observe(viewLifecycleOwner, Observer {settingList ->
            val settings = if(settingList.isNullOrEmpty()){
                makeDefaultNoteSetting(defaultTabType)
            }else settingList

            settings.forEach{setting ->
                setting.includeLocalRenotes = includeLocalRenotes
                setting.includeMyRenotes = includeMyRenotes
                setting.includeRenotedMyNotes = includeRenotedMyNotes
            }

            val adapter = TimelinePagerAdapter(activity?.supportFragmentManager, settings)
            viewPager.adapter = adapter
            tabLayout.setupWithViewPager(viewPager)

            if(settings.size <= 1){
                tabLayout.visibility = View.GONE
                elevationView.visibility = View.VISIBLE
            }
        })
    }

    private fun makeDefaultNoteSetting(list: List<NoteType>): List<NoteRequest.Setting>{
        return list.map{
            NoteRequest.Setting(type = it)
        }
    }

    class TimelinePagerAdapter(supportFragmentManager: FragmentManager?, val requestBaseList: List<NoteRequest.Setting>) : FragmentPagerAdapter(supportFragmentManager!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){
        override fun getCount(): Int {
            return requestBaseList.size
        }

        override fun getItem(p0: Int): Fragment {
            Log.d("getItem", "$p0, ${requestBaseList[p0].type}")
            return TimelineFragment.newInstance(requestBaseList[p0])
        }

        override fun getPageTitle(position: Int): CharSequence? {
            val requestBase = requestBaseList[position]
            return localizationTitle(requestBase)
        }

    }
}