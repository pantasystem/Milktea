package net.pantasystem.milktea.account

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import net.pantasystem.milktea.account.databinding.FragmentAccountTabBinding
import net.pantasystem.milktea.common.ui.ToolbarSetter
import net.pantasystem.milktea.common_android_ui.PageableFragmentFactory
import net.pantasystem.milktea.common_android_ui.UserPinnedNotesFragmentFactory
import javax.inject.Inject

@AndroidEntryPoint
class AccountTabFragment : Fragment(R.layout.fragment_account_tab) {

    @Inject
    lateinit var pageableFragmentFactory: PageableFragmentFactory

    @Inject
    lateinit var userPinnedNotesFragmentFactory: UserPinnedNotesFragmentFactory

    private val viewModel by viewModels<AccountTabViewModel>()

    private var _binding: FragmentAccountTabBinding? = null
    val binding: FragmentAccountTabBinding
        get() = requireNotNull(_binding)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAccountTabBinding.bind(view)
        _binding = binding

        val adapter = AccountTabPagerAdapter(
            pageableFragmentFactory,
            userPinnedNotesFragmentFactory,
            this,
        )
        binding.viewPager.adapter = adapter
        binding.tabLayout.tabMode = TabLayout.MODE_SCROLLABLE

        TabLayoutMediator(
            binding.tabLayout,
            binding.viewPager
        ) { tab, position ->
            tab.text = getString(adapter.tabs[position].title)
        }.attach()


        viewModel.tabs.onEach {
            adapter.submitList(it)
        }.flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED).launchIn(lifecycleScope)

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()

        (requireActivity() as? ToolbarSetter?)?.apply {
            setTitle(R.string.account)
            setToolbar(binding.toolbar)
        }
    }
}