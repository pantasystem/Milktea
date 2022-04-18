package jp.panta.misskeyandroidclient.ui.notes.view.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentNoteDetailBinding
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.NotesViewModel
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.detail.NoteDetailViewModel
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.detail.NoteDetailViewModelFactory
import net.pantasystem.milktea.model.account.page.Page
import net.pantasystem.milktea.model.account.page.Pageable
import net.pantasystem.milktea.model.notes.Note
import jp.panta.misskeyandroidclient.viewmodel.timeline.CurrentPageableTimelineViewModel
import kotlinx.coroutines.*

@FlowPreview
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class NoteDetailFragment : Fragment(R.layout.fragment_note_detail) {

    companion object {
        private const val EXTRA_NOTE_ID =
            "jp.panta.misskeyandroidclinet.view.notes.detail.EXTRA_NOTE_ID"
        private const val EXTRA_PAGE = "jp.panta.misskeyandroidclinet.view.notes.detail.EXTRA_PAGE"
        private const val EXTRA_ACCOUNT_ID =
            "jp.panta.misskeyandroidclient.view.notes.detail.EXTRA_ACCOUNT_ID"

        fun newInstance(noteId: String, accountId: Long? = null): NoteDetailFragment {
            return NoteDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(EXTRA_NOTE_ID, noteId)
                    putLong(EXTRA_ACCOUNT_ID, accountId ?: -1)
                }
            }
        }

        fun newInstance(
            page: net.pantasystem.milktea.model.account.page.Page
        ): NoteDetailFragment {
            page.pageable() as? net.pantasystem.milktea.model.account.page.Pageable.Show ?: throw IllegalArgumentException("Not Pageable.Show")
            return NoteDetailFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(EXTRA_PAGE, page)
                    putLong(EXTRA_ACCOUNT_ID, page.accountId)
                }
            }
        }

        fun newInstance(noteId: net.pantasystem.milktea.model.notes.Note.Id): NoteDetailFragment {
            return newInstance(noteId.noteId, noteId.accountId)
        }
    }

    private val binding: FragmentNoteDetailBinding by dataBinding()


    val notesViewModel: NotesViewModel by activityViewModels()

    private val currentPageableTimelineViewModel: CurrentPageableTimelineViewModel by activityViewModels()

    val page: net.pantasystem.milktea.model.account.page.Pageable.Show by lazy {
        (arguments?.getSerializable(EXTRA_PAGE) as? net.pantasystem.milktea.model.account.page.Page)?.pageable() as? net.pantasystem.milktea.model.account.page.Pageable.Show
            ?: net.pantasystem.milktea.model.account.page.Pageable.Show(arguments?.getString(EXTRA_NOTE_ID)!!)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val accountId = arguments?.getLong(EXTRA_ACCOUNT_ID, -1)?.let {
            if (it == -1L) null else it
        }

        val miApplication = context?.applicationContext as MiApplication


        val noteDetailViewModel = ViewModelProvider(
            this,
            NoteDetailViewModelFactory(page, accountId = accountId, miApplication = miApplication)
        )[NoteDetailViewModel::class.java]

        noteDetailViewModel.loadDetail()
        val adapter = NoteDetailAdapter(
            noteDetailViewModel = noteDetailViewModel,
            notesViewModel = notesViewModel,
            viewLifecycleOwner = viewLifecycleOwner
        )
        noteDetailViewModel.notes.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }


        binding.notesView.adapter = adapter
        binding.notesView.layoutManager = LinearLayoutManager(context)

        binding.showInBrowser.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                val url = noteDetailViewModel.getUrl()
                withContext(Dispatchers.Main) {
                    showShareLink(url)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        currentPageableTimelineViewModel.setCurrentPageable(page)
    }

    @MainThread
    private fun showShareLink(url: String) {
        val uri = Uri.parse(url)
        startActivity(
            Intent(Intent.ACTION_VIEW, uri)
        )
    }
}