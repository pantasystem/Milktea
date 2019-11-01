package jp.panta.misskeyandroidclient.view.drive

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import jp.panta.misskeyandroidclient.R
import kotlinx.android.synthetic.main.fragment_drive.*

class DriveFragment : Fragment(R.layout.fragment_drive){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        drive_view_pager.adapter = DrivePagerAdapter(context!!, activity?.supportFragmentManager!!)
        drive_tab.setupWithViewPager(drive_view_pager)
    }

    class DrivePagerAdapter(context: Context, fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){
        val titleList = listOf(context.getString(R.string.file), context.getString(R.string.folder))
        override fun getCount(): Int {
            return titleList.size
        }

        override fun getItem(position: Int): Fragment {
            return when(titleList[position]){
                titleList[0] -> FileFragment.newInstance(3)
                titleList[1] -> FolderFragment()
                else -> FileFragment()
            }
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titleList[position]
        }
    }
}