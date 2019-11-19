package jp.panta.misskeyandroidclient.view.users

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.view.notes.TimelineListAdapter
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.PlaneNoteViewData
import jp.panta.misskeyandroidclient.viewmodel.users.UserDetailViewModel
import kotlinx.android.synthetic.main.fragment_pin_note.*

class PinNoteFragment : Fragment(R.layout.fragment_pin_note){

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userViewModel = ViewModelProvider(activity!!)[UserDetailViewModel::class.java]
        val notesViewModel = ViewModelProvider(activity!!)[NotesViewModel::class.java]
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
        pin_notes_view.adapter = adapter
        pin_notes_view.layoutManager = LinearLayoutManager(this.context)
        userViewModel.pinNotes.observe(viewLifecycleOwner, Observer{
            adapter.submitList(it)
        })
    }
}