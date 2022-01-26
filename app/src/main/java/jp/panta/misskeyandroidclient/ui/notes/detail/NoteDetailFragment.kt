package jp.panta.misskeyandroidclient.ui.notes.detail

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.wada811.databinding.dataBinding
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentNoteDetailBinding
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModelFactory
import jp.panta.misskeyandroidclient.viewmodel.notes.detail.NoteDetailViewModel
import jp.panta.misskeyandroidclient.viewmodel.notes.detail.NoteDetailViewModelFactory
import jp.panta.misskeyandroidclient.model.account.page.Page
import jp.panta.misskeyandroidclient.model.account.page.Pageable
import jp.panta.misskeyandroidclient.model.notes.Note
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class NoteDetailFragment : Fragment(R.layout.fragment_note_detail){

    companion object{
        private const val EXTRA_NOTE_ID = "jp.panta.misskeyandroidclinet.view.notes.detail.EXTRA_NOTE_ID"
        private const val EXTRA_PAGE = "jp.panta.misskeyandroidclinet.view.notes.detail.EXTRA_PAGE"
        private const val EXTRA_ACCOUNT_ID = "jp.panta.misskeyandroidclient.view.notes.detail.EXTRA_ACCOUNT_ID"

        fun newInstance(noteId: String, accountId: Long? = null): NoteDetailFragment {
            return NoteDetailFragment().apply{
                arguments = Bundle().apply{
                    putString(EXTRA_NOTE_ID, noteId)
                    putLong(EXTRA_ACCOUNT_ID, accountId ?: -1)
                }
            }
        }

        fun newInstance(
            page: Page
        ): NoteDetailFragment {
            page.pageable() as? Pageable.Show?: throw IllegalArgumentException("Not Pageable.Show")
            return NoteDetailFragment().apply{
                arguments = Bundle().apply{
                    putSerializable(EXTRA_PAGE,  page)
                    putLong(EXTRA_ACCOUNT_ID, page.accountId)
                }
            }
        }

        fun newInstance(noteId: Note.Id): NoteDetailFragment {
            return newInstance(noteId.noteId, noteId.accountId)
        }
    }

    private val binding: FragmentNoteDetailBinding by dataBinding()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val page = (arguments?.getSerializable(EXTRA_PAGE) as? Page)?.pageable() as? Pageable.Show
            ?: Pageable.Show(arguments?.getString(EXTRA_NOTE_ID)!!)
        val accountId = arguments?.getLong(EXTRA_ACCOUNT_ID, -1 )?.let{
            if(it == -1L) null else it
        }

        val miApplication = context?.applicationContext as MiApplication

        val notesViewModel = ViewModelProvider(requireActivity(), NotesViewModelFactory(miApplication))[NotesViewModel::class.java]

        val noteDetailViewModel = ViewModelProvider(this, NoteDetailViewModelFactory(page, accountId = accountId, miApplication = miApplication))[NoteDetailViewModel::class.java]

        noteDetailViewModel.loadDetail()
        val adapter = NoteDetailAdapter(
            noteDetailViewModel = noteDetailViewModel,
            notesViewModel = notesViewModel,
            viewLifecycleOwner = viewLifecycleOwner
        )
        noteDetailViewModel.notes.observe(viewLifecycleOwner, {
            adapter.submitList(it)
        })


        binding.notesView.adapter = adapter
        binding.notesView.layoutManager = LinearLayoutManager(context)
    }
}