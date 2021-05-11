package jp.panta.misskeyandroidclient.view.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentPinNoteBinding
import jp.panta.misskeyandroidclient.view.notes.TimelineListAdapter
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData
import jp.panta.misskeyandroidclient.viewmodel.users.UserDetailViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

class PinNoteFragment : Fragment(){

    lateinit var mBinding: FragmentPinNoteBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_pin_note, container, false)
        return mBinding.root
    }

    @ExperimentalCoroutinesApi
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val userViewModel = ViewModelProvider(requireActivity())[UserDetailViewModel::class.java]
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