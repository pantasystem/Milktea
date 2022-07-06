package net.pantasystem.milktea.gallery

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import net.pantasystem.milktea.gallery.databinding.FragmentGalleryPostTabBinding
import net.pantasystem.milktea.gallery.viewmodel.GalleryPostActionViewModel
import net.pantasystem.milktea.model.account.page.Pageable

@AndroidEntryPoint
class GalleryPostTabFragment : Fragment(R.layout.fragment_gallery_post_tab){

    val binding: FragmentGalleryPostTabBinding by dataBinding()
    private val actionViewModel: GalleryPostActionViewModel by activityViewModels()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
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


        binding.fab.setOnClickListener {
            actionViewModel.showEditor()
        }

    }


}

@FlowPreview
@ExperimentalCoroutinesApi
@Suppress("DEPRECATION")
class GalleryPostTabPagerAdapter(
    val tabs: List<Pageable.Gallery>,
    private val pageTitles: List<String>,
    fragmentManager: FragmentManager
) : androidx.fragment.app.FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

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