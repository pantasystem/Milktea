package jp.panta.misskeyandroidclient.view.drive

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentDriveBinding
import jp.panta.misskeyandroidclient.viewmodel.drive.DriveSelectableMode

class DriveFragment : Fragment(R.layout.fragment_drive){

    private val binding: FragmentDriveBinding by dataBinding()

    companion object {
        const val EXTRA_MODE = "mode"
        fun newInstance(mode: DriveSelectableMode?) : Fragment{
            return DriveFragment().also {
                it.arguments = Bundle().also { bundle ->
                    bundle.putSerializable(EXTRA_MODE, mode)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mode = arguments?.getSerializable(EXTRA_MODE) as DriveSelectableMode?

        binding.driveViewPager.adapter = DrivePagerAdapter(requireContext(), childFragmentManager, mode)
        binding.driveTab.setupWithViewPager(binding.driveViewPager)
    }

    class DrivePagerAdapter(context: Context, fragmentManager: FragmentManager, val mode: DriveSelectableMode?) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){
        val titleList = listOf(context.getString(R.string.file), context.getString(R.string.folder))
        override fun getCount(): Int {
            return titleList.size
        }

        override fun getItem(position: Int): Fragment {
            return when(titleList[position]){
                titleList[0] -> FileFragment.newInstance(mode)
                titleList[1] -> FolderFragment.newInstance(mode)
                else -> FileFragment()
            }
        }

        override fun getPageTitle(position: Int): CharSequence {
            return titleList[position]
        }
    }
}