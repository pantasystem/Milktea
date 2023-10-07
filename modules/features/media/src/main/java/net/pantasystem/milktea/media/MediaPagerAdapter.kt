@file:Suppress("DEPRECATION")

package net.pantasystem.milktea.media

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

@Suppress("DEPRECATION")
class MediaPagerAdapter(
    fragmentManager: FragmentManager,
    private val list: List<Media>
) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): Fragment {
        return when (val item = list[position]) {
            is Media.FileMedia -> createFragment(position, item.file)
        }
    }

    private fun createFragment(index: Int, file: File): Fragment {

        return if (file.type?.contains("image") == true) {
            ImageFragment.newInstance(index, file)
        } else {
            PlayerFragment.newInstance(index, file.path!!)
        }
    }

}
