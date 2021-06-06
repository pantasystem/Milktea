package jp.panta.misskeyandroidclient.view.gallery

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentGalleryPostTabBinding
import jp.panta.misskeyandroidclient.model.account.page.Pageable

class GalleryPostTabFragment : Fragment(R.layout.fragment_gallery_post_tab){

    val binding: FragmentGalleryPostTabBinding by dataBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val appCompatActivity = (requireActivity() as AppCompatActivity)
        appCompatActivity.setSupportActionBar(binding.toolbar)
        appCompatActivity.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.galleryTab.setupWithViewPager(binding.viewPager)
        val adapter = GalleryPostTabPagerAdapter(
            listOf(
                Pageable.Gallery.Posts,
                Pageable.Gallery.ILikedPosts,
                Pageable.Gallery.MyPosts
            ),
            listOf(
                getString(R.string.gallery),
                getString(R.string.my_liking),
                getString(R.string.my_posts),
            ),
            childFragmentManager
        )
        binding.viewPager.adapter = adapter


    }


}

class GalleryPostTabPagerAdapter(
    val tabs: List<Pageable.Gallery>,
    val pageTitles: List<String>,
    fragmentManager: FragmentManager
) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    init {
        assert(tabs.size == pageTitles.size) {
            "与えられたタブ数(${tabs.size})と、タイトルの数(${pageTitles.size})が一致しません"
        }
    }

    override fun getCount(): Int {
        return tabs.size
    }

    override fun getItem(position: Int): Fragment {
        return GalleryPostsFragment.newInstance(tabs[position], null)
    }

    override fun getPageTitle(position: Int): CharSequence {
        return pageTitles[position]
    }
}