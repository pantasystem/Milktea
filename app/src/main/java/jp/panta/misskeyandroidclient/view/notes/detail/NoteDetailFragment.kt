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
import jp.panta.misskeyandroidclient.model.account.page.Page
import jp.panta.misskeyandroidclient.model.account.page.Pageable
import jp.panta.misskeyandroidclient.model.notes.Note

class NoteDetailFragment : Fragment(R.layout.fragment_note_detail){

    companion object{
        private const val EXTRA_NOTE_ID = "jp.panta.misskeyandroidclinet.view.notes.detail.EXTRA_NOTE_ID"
        private const val EXTRA_PAGE = "jp.panta.misskeyandroidclinet.view.notes.detail.EXTRA_PAGE"
        fun newInstance(noteId: String): NoteDetailFragment{
            return NoteDetailFragment().apply{
                arguments = Bundle().apply{
                    putString(EXTRA_NOTE_ID, noteId)
                }
            }
        }

        fun newInstance(
            page: Page
        ): NoteDetailFragment{
            page.pageable() as? Pageable.Show?: throw IllegalArgumentException("Not Pageable.Show")
            return NoteDetailFragment().apply{
                arguments = Bundle().apply{
                    putSerializable(EXTRA_PAGE,  page)
                }
            }
        }

        fun newInstance(noteId: Note.Id): NoteDetailFragment {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val page = (arguments?.getSerializable(EXTRA_PAGE) as? Page)?.pageable() as? Pageable.Show
            ?: Pageable.Show(arguments?.getString(EXTRA_NOTE_ID)!!)

        val miApplication = context?.applicationContext as MiApplication

        val notesViewModel = ViewModelProvider(requireActivity(), NotesViewModelFactory(miApplication))[NotesViewModel::class.java]

        miApplication.getCurrentAccount().observe(viewLifecycleOwner, Observer { ac ->
            val noteDetailViewModel = ViewModelProvider(this, NoteDetailViewModelFactory(ac, miApplication, page))[NoteDetailViewModel::class.java]

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