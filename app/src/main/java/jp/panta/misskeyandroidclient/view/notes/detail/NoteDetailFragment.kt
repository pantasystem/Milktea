package jp.panta.misskeyandroidclient.view.notes.detail

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModelFactory
import jp.panta.misskeyandroidclient.viewmodel.notes.detail.NoteDetailViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.detail.NoteDetailViewModelFactory
import kotlinx.android.synthetic.main.fragment_note_detail.*
import java.lang.IllegalArgumentException

class NoteDetailFragment : Fragment(R.layout.fragment_note_detail){

    companion object{
        private const val EXTRA_NOTE_ID = "jp.panta.misskeyandroidclinet.view.notes.detail.EXTRA_NOTE_ID"
        fun newInstance(noteId: String): NoteDetailFragment{
            return NoteDetailFragment().apply{
                arguments = Bundle().apply{
                    putString(EXTRA_NOTE_ID, noteId)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val noteId = arguments?.getString(EXTRA_NOTE_ID)
        noteId?: throw IllegalArgumentException("noteId must not null")

        val miApplication = context?.applicationContext as MiApplication
        miApplication.currentAccount.observe(viewLifecycleOwner, Observer {ar ->
            val notesViewModel = ViewModelProvider(activity!!, NotesViewModelFactory(ar, miApplication))[NotesViewModel::class.java]
            val noteDetailViewModel = ViewModelProvider(this, NoteDetailViewModelFactory(ar, miApplication, noteId))[NoteDetailViewModel::class.java]

            noteDetailViewModel.loadDetail()
            val adapter = NoteDetailAdapter(
                noteDetailViewModel = noteDetailViewModel,
                notesViewModel = notesViewModel,
                viewLifecycleOwner = viewLifecycleOwner
            )
            noteDetailViewModel.notes.observe(viewLifecycleOwner, Observer {
                adapter.submitList(it)
            })

            noteDetailViewModel.loadDetail()

            notes_view.adapter = adapter
            notes_view.layoutManager = LinearLayoutManager(context)
        })
    }
}