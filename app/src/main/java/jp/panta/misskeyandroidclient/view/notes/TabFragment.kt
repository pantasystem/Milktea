package jp.panta.misskeyandroidclient.view.notes

import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
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
            }
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //return super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val testData1 = NoteRequest.Setting(i = SecretConstant.i(), type = NoteType.HOME)
        val testData2 = NoteRequest.Setting(i = SecretConstant.i(), type = NoteType.SOCIAL)
        val testData3 = NoteRequest.Setting(i = SecretConstant.i(), type = NoteType.GLOBAL)
        val testData4 = NoteRequest.Setting(i = SecretConstant.i(), type = NoteType.SEARCH, query = "おはよう")
        val testDataList = listOf(testData1, testData2, testData3, testData4)
        if(testDataList.size <= 1){
            tabLayout.visibility = View.GONE
        }
        val adapter = TimelinePagerAdapter(activity?.supportFragmentManager, testDataList)
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)


    }

    class TimelinePagerAdapter(supportFragmentManager: FragmentManager?, val requestBaseList: List<NoteRequest.Setting>) : FragmentPagerAdapter(supportFragmentManager!!){
        override fun getCount(): Int {
            return requestBaseList.size
        }

        override fun getItem(p0: Int): Fragment {
            return TimelineFragment.newInstance(requestBaseList[p0])
        }

        override fun getPageTitle(position: Int): CharSequence? {
            val requestBase = requestBaseList[position]
            return localizationTitle(requestBase)
        }

    }
}