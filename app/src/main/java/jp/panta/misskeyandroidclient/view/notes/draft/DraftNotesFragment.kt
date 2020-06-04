package jp.panta.misskeyandroidclient.view.notes.draft

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.NoteEditorActivity
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.model.notes.draft.DraftNote
import jp.panta.misskeyandroidclient.viewmodel.notes.draft.DraftNotesViewModel
import kotlinx.android.synthetic.main.fragment_draft_notes.*

class DraftNotesFragment : Fragment(R.layout.fragment_draft_notes), DraftNoteActionCallback{

    private var mDraftNotesViewModel: DraftNotesViewModel? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val miApplication = view.context.applicationContext as MiApplication
        val viewModel = ViewModelProvider(this, DraftNotesViewModel.Factory(miApplication))[DraftNotesViewModel::class.java]
        mDraftNotesViewModel = viewModel

        val adapter = DraftNoteListAdapter(this, viewLifecycleOwner)
        draftNotesView.adapter = adapter
        draftNotesView.layoutManager = LinearLayoutManager(view.context)

        viewModel.draftNotes.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })

        draftNotesSwipeRefresh.setOnRefreshListener {
            viewModel.loadDraftNotes()
        }
    }

    override fun onSelect(draftNote: DraftNote?) {
        val intent = Intent(requireContext(), NoteEditorActivity::class.java)
        intent.putExtra(NoteEditorActivity.EXTRA_DRAFT_NOTE, draftNote)
        requireActivity().startActivityFromFragment(this, intent, 300)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            300 ->{
                if(resultCode == RESULT_OK){
                    mDraftNotesViewModel?.loadDraftNotes()
                }
            }
        }

    }

}