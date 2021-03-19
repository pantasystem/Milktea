package jp.panta.misskeyandroidclient.view.notes.draft

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import jp.panta.misskeyandroidclient.MediaActivity
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.NoteEditorActivity
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.confirm.ConfirmCommand
import jp.panta.misskeyandroidclient.model.confirm.ResultType
import jp.panta.misskeyandroidclient.model.file.File
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNote
import jp.panta.misskeyandroidclient.view.confirm.ConfirmDialog
import jp.panta.misskeyandroidclient.viewmodel.confirm.ConfirmViewModel
import jp.panta.misskeyandroidclient.viewmodel.file.FileListener
import jp.panta.misskeyandroidclient.viewmodel.notes.draft.DraftNotesViewModel
import kotlinx.android.synthetic.main.fragment_draft_notes.*

class DraftNotesFragment : Fragment(R.layout.fragment_draft_notes), DraftNoteActionCallback, FileListener{

    companion object{
        private const val EV_DELETE_DRAFT_NOTE = "delete_draft_note"
    }

    private var mDraftNotesViewModel: DraftNotesViewModel? = null
    private var mConfirmViewModel: ConfirmViewModel? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val miApplication = view.context.applicationContext as MiApplication
        val viewModel = ViewModelProvider(this, DraftNotesViewModel.Factory(miApplication))[DraftNotesViewModel::class.java]
        mDraftNotesViewModel = viewModel

        val adapter = DraftNoteListAdapter(this, this, viewLifecycleOwner)
        draftNotesView.adapter = adapter
        draftNotesView.layoutManager = LinearLayoutManager(view.context)

        viewModel.draftNotes.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })

        draftNotesSwipeRefresh.setOnRefreshListener {
            viewModel.loadDraftNotes()
        }
        viewModel.isLoading.observe( viewLifecycleOwner, Observer {
            draftNotesSwipeRefresh.isRefreshing = it
        })

        val confirmViewModel = ViewModelProvider(requireActivity())[ConfirmViewModel::class.java]
        mConfirmViewModel = confirmViewModel

        confirmViewModel.confirmedEvent.observe(viewLifecycleOwner, Observer {
            if(it.eventType == EV_DELETE_DRAFT_NOTE && it.resultType == ResultType.POSITIVE){
                (it.args as? DraftNote)?.let{ dn ->
                    mDraftNotesViewModel?.deleteDraftNote(dn)
                }
            }
        })

        confirmViewModel.confirmEvent.observe(viewLifecycleOwner, Observer {
            ConfirmDialog().show(parentFragmentManager, "confirm")
        })
    }

    override fun onSelect(draftNote: DraftNote?) {
        val intent = NoteEditorActivity.newBundle(requireContext(), draftNote = draftNote)
        requireActivity().startActivityFromFragment(this, intent, 300)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mDraftNotesViewModel?.loadDraftNotes()

    }

    override fun onDetach(file: File?) {
        mDraftNotesViewModel?.detachFile(file)
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