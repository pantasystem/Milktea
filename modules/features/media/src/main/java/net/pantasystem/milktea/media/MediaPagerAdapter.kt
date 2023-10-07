@file:Suppress("DEPRECATION")

package net.pantasystem.milktea.media

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

@Suppress("DEPRECATION")
class MediaPagerAdapter(
    fragmentManager: FragmentManager,
) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private var _files = emptyList<Media>()

    override fun getCount(): Int {
        return _files.size
    }

    override fun getItem(position: Int): Fragment {
        return when (val item = _files[position]) {
            is Media.FileMedia -> createFragment(position, item.file)
        }
    }

    fun setFiles(files: List<Media>) {
        if (_files == files) return
        _files = files
        notifyDataSetChanged()
    }

    private fun createFragment(index: Int, file: File): Fragment {

        return if (file.type?.contains("image") == true) {
            ImageFragment.newInstance(index, file)
        } else {
            PlayerFragment.newInstance(index, file.path!!)
        }
    }

}
