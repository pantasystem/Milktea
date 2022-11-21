package net.pantasystem.milktea.user.reaction

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common.PageableState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common_navigation.UserDetailNavigation
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.note.view.NoteCardActionHandler
import net.pantasystem.milktea.note.view.NoteCardActionListenerAdapter
import net.pantasystem.milktea.note.viewmodel.NotesViewModel
import net.pantasystem.milktea.user.R
import net.pantasystem.milktea.user.databinding.FragmentUserReactionsBinding
import javax.inject.Inject

@AndroidEntryPoint
class UserReactionsFragment : Fragment(R.layout.fragment_user_reactions) {

    companion object {
        fun newInstance(userId: User.Id): Fragment {
            return UserReactionsFragment().apply {
                arguments = Bundle().apply {
                    putLong(UserReactionsViewModel.ACCOUNT_ID, userId.accountId)
                    putString(UserReactionsViewModel.USER_ID, userId.id)
                }
            }
        }
    }

    @Inject
    lateinit var settingStore: SettingStore

    @Inject
    lateinit var userDetailNavigation: UserDetailNavigation

    private val binding: FragmentUserReactionsBinding by dataBinding()
    private val viewModel by viewModels<UserReactionsViewModel>()
    private val notesViewModel by activityViewModels<NotesViewModel>()

    private lateinit var layoutManager: LinearLayoutManager


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = UserReactionsListAdapter(
            lifecycleOwner = viewLifecycleOwner,
            noteCardActionHandler = NoteCardActionListenerAdapter {
                NoteCardActionHandler(
                    requireActivity() as AppCompatActivity,
                    notesViewModel,
                    settingStore,
                    userDetailNavigation
                ).onAction(it)
            }
        )
        binding.userReactionsListView.adapter = adapter
        layoutManager = LinearLayoutManager(requireContext())
        binding.userReactionsListView.layoutManager = layoutManager

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.state.collect {
                    val list = (it.content as? StateContent.Exist)?.rawContent ?: emptyList()
                    adapter.submitList(list)

                    binding.swipeRefreshLayout.isRefreshing = it is PageableState.Loading
                }
            }
        }

        binding.userReactionsListView.addOnScrollListener(_scrollListener)

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.clearAndLoadPrevious()
        }

    }

    private val _scrollListener = object : RecyclerView.OnScrollListener() {


        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            val endVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
            val itemCount = layoutManager.itemCount

            if (endVisibleItemPosition == (itemCount - 1)) {
                viewModel.loadPrevious()
            }

        }
    }
}