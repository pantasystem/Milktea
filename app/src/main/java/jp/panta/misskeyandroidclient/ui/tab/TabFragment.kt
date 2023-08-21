package jp.panta.misskeyandroidclient.ui.tab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.tabs.TabLayout
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentTabBinding
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.common.Logger
import net.pantasystem.milktea.common.ui.AvatarIconView.Companion.setShape
import net.pantasystem.milktea.common.ui.ToolbarSetter
import net.pantasystem.milktea.common_android_ui.PageableFragmentFactory
import net.pantasystem.milktea.model.account.page.Page
import javax.inject.Inject

@AndroidEntryPoint
class TabFragment : Fragment(R.layout.fragment_tab) {

    companion object {
        private const val PAGES = "pages"
    }


    private lateinit var mPagerAdapter: TimelinePagerAdapter

    private val binding: FragmentTabBinding by dataBinding()

    @Inject
    lateinit var accountStore: AccountStore

    @Inject
    lateinit var pageableFragmentFactory: PageableFragmentFactory

    @Inject
    lateinit var loggerFactory: Logger.Factory

    private val mTabViewModel by viewModels<TabViewModel>()

    private var mPages: List<Page>? = null

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            mPages = savedInstanceState.getParcelableArrayList(PAGES)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mPagerAdapter = TimelinePagerAdapter(
            this.childFragmentManager,
            pageableFragmentFactory,
            mPages ?: emptyList(),
            loggerFactory,
        )

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.viewPager.adapter = mPagerAdapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                mTabViewModel.pages.collect { pages ->
                    mPages = pages
                    mPagerAdapter.setList(
                        pages.sortedBy {
                            it.weight
                        })

                    if (pages.size <= 1) {
                        binding.tabLayout.visibility = View.GONE
                        binding.elevationView.visibility = View.VISIBLE
                    } else {
                        binding.tabLayout.visibility = View.VISIBLE
                        binding.elevationView.visibility = View.GONE
                        binding.tabLayout.tabMode = if (pages.size > 5) {
                            TabLayout.MODE_SCROLLABLE
                        } else {
                            TabLayout.MODE_FIXED
                        }
                    }
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                mTabViewModel.currentUser.filterNotNull().collect {
                    binding.currentAccountView.setImageUrl(it.avatarUrl)
                }
            }
        }

        mTabViewModel.visibleInstanceInfo.onEach {
            when(it) {
                CurrentAccountInstanceInfoUrl.Invisible -> {
                    binding.currentInstanceHostView.visibility = View.GONE
                }
                is CurrentAccountInstanceInfoUrl.Visible -> {
                    binding.currentInstanceHostView.visibility = View.VISIBLE
                    binding.currentInstanceHostView.text = it.host
                }
            }
        }.flowWithLifecycle(
            viewLifecycleOwner.lifecycle,
            Lifecycle.State.RESUMED
        ).launchIn(viewLifecycleOwner.lifecycleScope)

        mTabViewModel.avatarIconShapeType.onEach {
            binding.currentAccountView.setShape(it.value)
        }.flowWithLifecycle(
            viewLifecycleOwner.lifecycle,
            Lifecycle.State.RESUMED
        ).launchIn(viewLifecycleOwner.lifecycleScope)

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        if (mPages != null) {
            outState.putParcelableArrayList(PAGES, ArrayList(mPages!!))
        }
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as? ToolbarSetter?)?.apply {
            setToolbar(binding.toolbar)
            setTitle(R.string.menu_home)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mPagerAdapter.onDestroy()
    }
}
