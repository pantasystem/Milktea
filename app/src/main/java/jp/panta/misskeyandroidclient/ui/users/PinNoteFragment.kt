package jp.panta.misskeyandroidclient.ui.users

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentPinNoteBinding
import net.pantasystem.milktea.model.user.User
import jp.panta.misskeyandroidclient.ui.notes.view.TimelineListAdapter
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.NotesViewModel
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.PlaneNoteViewData
import jp.panta.misskeyandroidclient.ui.users.viewmodel.UserDetailViewModel
import jp.panta.misskeyandroidclient.ui.users.viewmodel.provideFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class PinNoteFragment : Fragment(R.layout.fragment_pin_note) {

    val mBinding: FragmentPinNoteBinding by dataBinding()

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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val notesViewModel = ViewModelProvider(requireActivity())[NotesViewModel::class.java]
        val adapter = TimelineListAdapter(object : DiffUtil.ItemCallback<PlaneNoteViewData>() {
            override fun areContentsTheSame(
                oldItem: PlaneNoteViewData,
                newItem: PlaneNoteViewData
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areItemsTheSame(
                oldItem: PlaneNoteViewData,
                newItem: PlaneNoteViewData
            ): Boolean {
                return oldItem.id == newItem.id
            }
        }, viewLifecycleOwner, notesViewModel)
        mBinding.pinNotesView.adapter = adapter
        mBinding.pinNotesView.layoutManager = LinearLayoutManager(this.context)
        userViewModel.pinNotes.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })
    }

}