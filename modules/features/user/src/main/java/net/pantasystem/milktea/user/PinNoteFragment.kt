package net.pantasystem.milktea.user

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import net.pantasystem.milktea.app_store.setting.SettingStore
import net.pantasystem.milktea.common_navigation.AuthorizationArgs
import net.pantasystem.milktea.common_navigation.AuthorizationNavigation
import net.pantasystem.milktea.common_navigation.UserDetailNavigation
import net.pantasystem.milktea.model.user.User
import net.pantasystem.milktea.note.timeline.TimelineListAdapter
import net.pantasystem.milktea.note.timeline.viewmodel.TimelineListItem
import net.pantasystem.milktea.note.view.NoteCardActionHandler
import net.pantasystem.milktea.note.viewmodel.NotesViewModel
import net.pantasystem.milktea.user.databinding.FragmentPinNoteBinding
import net.pantasystem.milktea.user.viewmodel.UserDetailViewModel
import net.pantasystem.milktea.user.viewmodel.provideFactory
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class PinNoteFragment : Fragment(R.layout.fragment_pin_note) {


    companion object {

        fun newInstance(userId: User.Id?, fqcnUserName: String?): PinNoteFragment {
            require(!(userId == null && fqcnUserName == null)) {
                "userId, fqcnUserNameどちらか一つは必須です。"
            }
            return PinNoteFragment().also {
                it.arguments = Bundle().also { bundle ->
                    if (userId != null) {
                        bundle.putString("USER_ID", userId.id)
                        bundle.putLong("ACCOUNT_ID", userId.accountId)
                    }
                    if (fqcnUserName != null) {
                        bundle.putString("FQCN_USER_NAME", fqcnUserName)
                    }

                }

            }
        }
    }

    @Inject
    lateinit var assistedFactory: UserDetailViewModel.ViewModelAssistedFactory

    @Inject
    internal lateinit var settingStore: SettingStore

    @Inject
    lateinit var userDetailNavigation: UserDetailNavigation

    @Inject
    lateinit var authorizationNavigation: AuthorizationNavigation

    @ExperimentalCoroutinesApi
    val userViewModel: UserDetailViewModel by activityViewModels {

        val accountId: Long = requireArguments().getLong("ACCOUNT_ID", -1)
        val remoteUserId: String? = requireArguments().getString("USER_ID")
        if (!(remoteUserId == null || accountId == -1L)) {
            val userId = User.Id(accountId, remoteUserId)
            return@activityViewModels UserDetailViewModel.provideFactory(assistedFactory, userId)
        }
        val userName = requireArguments().getString("FQCN_USER_NAME")
        return@activityViewModels UserDetailViewModel.provideFactory(assistedFactory, userName!!)
    }

    val binding: FragmentPinNoteBinding by dataBinding()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val notesViewModel = ViewModelProvider(requireActivity())[NotesViewModel::class.java]
        val adapter = TimelineListAdapter(viewLifecycleOwner, {
            userViewModel.sync()
        }, {
            authorizationNavigation.newIntent(AuthorizationArgs.New)
        }) {
            NoteCardActionHandler(
                requireActivity() as AppCompatActivity,
                notesViewModel,
                settingStore,
                userDetailNavigation
            ).onAction(
                it
            )
        }

        binding.pinNotesView.adapter = adapter
        binding.pinNotesView.layoutManager = LinearLayoutManager(this.context)

        userViewModel.pinNotes.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list.map {
                TimelineListItem.Note(it)
            })
        }
    }

}