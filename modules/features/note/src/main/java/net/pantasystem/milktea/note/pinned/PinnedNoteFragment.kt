package net.pantasystem.milktea.note.pinned

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
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common_android_ui.UserPinnedNotesFragmentFactory
import net.pantasystem.milktea.common_navigation.AuthorizationArgs
import net.pantasystem.milktea.common_navigation.AuthorizationNavigation
import net.pantasystem.milktea.common_navigation.ChannelDetailNavigation
import net.pantasystem.milktea.common_navigation.UserDetailNavigation
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.FragmentPinnedNotesBinding
import net.pantasystem.milktea.note.timeline.TimelineListAdapter
import net.pantasystem.milktea.note.view.NoteCardActionHandler
import net.pantasystem.milktea.note.viewmodel.NotesViewModel
import javax.inject.Inject
import javax.inject.Singleton

@AndroidEntryPoint
class PinnedNoteFragment : Fragment(R.layout.fragment_pinned_notes) {

    companion object {
        fun newInstance(userId: User.Id): Fragment {
            return PinnedNoteFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(PinnedNotesViewModel.EXTRA_USER_ID, userId)
                }
            }
        }
    }

    @Inject
    lateinit var authorizationNavigation: AuthorizationNavigation

    @Inject
    lateinit var accountStore: AccountStore

    @Inject
    lateinit var settingStore: SettingStore

    @Inject
    lateinit var userDetailNavigation: UserDetailNavigation

    @Inject
    lateinit var channelDetailNavigation: ChannelDetailNavigation

    val notesViewModel: NotesViewModel by activityViewModels()

    val pinnedNotesViewModel: PinnedNotesViewModel by viewModels()

    val binding: FragmentPinnedNotesBinding by dataBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val adapter = TimelineListAdapter(
            viewLifecycleOwner,
            onRefreshAction = {

            },
            onReauthenticateAction = {
                startActivity(
                    authorizationNavigation.newIntent(
                        AuthorizationArgs.ReAuth(
                            accountStore.currentAccount
                        )
                    )
                )
            },
        ) {
            NoteCardActionHandler(
                requireActivity() as AppCompatActivity,
                notesViewModel,
                settingStore,
                userDetailNavigation,
                channelDetailNavigation
            ).onAction(it)
        }

        binding.listView.adapter = adapter
        binding.listView.layoutManager = LinearLayoutManager(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                pinnedNotesViewModel.notes.collect {
                    adapter.submitList(it)
                }
            }
        }
    }
}

@Singleton
class UserPinnedNotesFragmentFactoryImpl @Inject constructor(): UserPinnedNotesFragmentFactory {
    override fun create(userId: User.Id): Fragment {
        return PinnedNoteFragment.newInstance(userId)
    }
}