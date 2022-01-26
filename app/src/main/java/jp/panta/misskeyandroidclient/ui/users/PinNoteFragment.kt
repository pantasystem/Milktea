package jp.panta.misskeyandroidclient.ui.users

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentPinNoteBinding
import jp.panta.misskeyandroidclient.model.users.User
import jp.panta.misskeyandroidclient.ui.notes.TimelineListAdapter
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData
import jp.panta.misskeyandroidclient.viewmodel.users.UserDetailViewModel
import jp.panta.misskeyandroidclient.viewmodel.users.UserDetailViewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class PinNoteFragment : Fragment(R.layout.fragment_pin_note){

    val mBinding: FragmentPinNoteBinding by dataBinding()

    companion object {

        fun newInstance(userId: User.Id?, fqcnUserName: String?) : PinNoteFragment{
            require(!(userId == null && fqcnUserName == null)) {
                "userId, fqcnUserNameどちらか一つは必須です。"
            }
            return PinNoteFragment().also {
                it.arguments = Bundle().also { bundle ->
                    if(userId != null) {
                        bundle.putString("USER_ID", userId.id)
                        bundle.putLong("ACCOUNT_ID", userId.accountId)
                    }
                    if(fqcnUserName != null) {
                        bundle.putString("FQCN_USER_NAME", fqcnUserName)
                    }

                }

            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userId = arguments?.getString("USER_ID")
        val accountId = arguments?.getLong("ACCOUNT_ID")
        val fqcnUserName = arguments?.getString("FQCN_USER_NAME")
        val miApplication = requireContext().applicationContext as MiApplication
        val userViewModel = ViewModelProvider(
            requireActivity(),
            UserDetailViewModelFactory(
                miApplication,
                userId = userId?.let {
                    User.Id(accountId!!, userId)
                },
                fqcnUserName = fqcnUserName
            )
        )[UserDetailViewModel::class.java]
        val notesViewModel = ViewModelProvider(requireActivity())[NotesViewModel::class.java]
        val adapter = TimelineListAdapter(object : DiffUtil.ItemCallback<PlaneNoteViewData>(){
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