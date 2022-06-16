package jp.panta.misskeyandroidclient.ui.notes.view.draft

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.NoteEditorActivity
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentDraftNotesBinding
import jp.panta.misskeyandroidclient.ui.confirm.ConfirmDialog
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.draft.DraftNotesViewModel
import jp.panta.misskeyandroidclient.viewmodel.confirm.ConfirmViewModel
import jp.panta.misskeyandroidclient.viewmodel.file.FileListener
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.data.infrastructure.confirm.ConfirmCommand
import net.pantasystem.milktea.data.infrastructure.confirm.ResultType
import net.pantasystem.milktea.media.MediaActivity
import net.pantasystem.milktea.model.file.File
import net.pantasystem.milktea.model.notes.draft.DraftNote

/**
 * NOTE: 直接的なコードによる参照はないが、activity_draft_notesから参照されているので削除しないこと。
 */
@AndroidEntryPoint
class DraftNotesFragment : Fragment(R.layout.fragment_draft_notes), DraftNoteActionCallback, FileListener{

    companion object{
        private const val EV_DELETE_DRAFT_NOTE = "delete_draft_note"
    }

    private var mDraftNotesViewModel: DraftNotesViewModel? = null
    private var mConfirmViewModel: ConfirmViewModel? = null

    private val binding: FragmentDraftNotesBinding by dataBinding()

    val viewModel: DraftNotesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // TODO: 実装する

        mDraftNotesViewModel = viewModel

        val adapter = DraftNoteListAdapter(this, this, viewLifecycleOwner)
        binding.draftNotesView.adapter = adapter
        binding.draftNotesView.layoutManager = LinearLayoutManager(view.context)

        lifecycleScope.launchWhenResumed {
            viewModel.uiState.collect {
                binding.draftNotesSwipeRefresh.isRefreshing = it.draftNotes is ResultState.Loading
                when(val content = it.draftNoteUiStateList.content) {
                    is StateContent.NotExist -> {
                        adapter.submitList(emptyList())
                    }
                    is StateContent.Exist -> {
                        adapter.submitList(content.rawContent)
                    }
                }
            }
        }


        val confirmViewModel = ViewModelProvider(requireActivity())[ConfirmViewModel::class.java]
        mConfirmViewModel = confirmViewModel

        confirmViewModel.confirmedEvent.observe(viewLifecycleOwner) {
            if (it.eventType == EV_DELETE_DRAFT_NOTE && it.resultType == ResultType.POSITIVE) {
                (it.args as? DraftNote)?.let { dn ->
                    mDraftNotesViewModel?.deleteDraftNote(dn)
                }
            }
        }

        confirmViewModel.confirmEvent.observe(viewLifecycleOwner) {
            ConfirmDialog().show(parentFragmentManager, "confirm")
        }
    }

    override fun onSelect(draftNote: DraftNote?) {
        val intent = NoteEditorActivity.newBundle(requireContext(), draftNoteId = draftNote?.draftNoteId)
        requireActivity().startActivityFromFragment(this, intent, 300)
    }


    override fun onDetach(file: File?) {
        // TODO: 実装する
//        mDraftNotesViewModel?.detachFile(file)
    }

    override fun onSelect(file: File?) {
        file?.let{
            val intent = Intent(requireContext(), MediaActivity::class.java)
            intent.putExtra(MediaActivity.EXTRA_FILE, file)
            startActivity(intent)
        }

    }

    override fun onDelete(draftNote: DraftNote?) {
        draftNote?.let{
            mConfirmViewModel?.confirmEvent?.event = ConfirmCommand(
                title = null,
                message = getString(R.string.delete_draft_note),
                args = draftNote,
                eventType = EV_DELETE_DRAFT_NOTE

            )
        }

    }

}