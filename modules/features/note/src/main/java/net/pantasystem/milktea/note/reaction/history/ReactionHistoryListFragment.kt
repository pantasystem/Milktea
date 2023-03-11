package net.pantasystem.milktea.note.reaction.history

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.FragmentReactionHistoryListBinding
import javax.inject.Inject

@AndroidEntryPoint
class ReactionHistoryListFragment : Fragment(R.layout.fragment_reaction_history_list) {

    companion object {
        private const val EXTRA_NOTE_ID = "NOTE_ID"
        private const val EXTRA_ACCOUNT_ID = "EXTRA_ACCOUNT_ID"
        private const val EXTRA_TYPE = "EXTRA_TYPE"

        fun newInstance(noteId: Note.Id, type: String? = null): ReactionHistoryListFragment {
            return ReactionHistoryListFragment().also {
                it.arguments = Bundle().also { bundle ->
                    bundle.putString(EXTRA_NOTE_ID, noteId.noteId)
                    bundle.putLong(EXTRA_ACCOUNT_ID, noteId.accountId)
                    type?.let {
                        bundle.putString(EXTRA_TYPE, type)
                    }
                }
            }
        }
    }

    private val binding: FragmentReactionHistoryListBinding by dataBinding()

    lateinit var mLinearLayoutManager: LinearLayoutManager

    @Inject
    lateinit var assistedFactory: ReactionHistoryViewModel.ViewModelAssistedFactory
    private val viewModel: ReactionHistoryViewModel by viewModels {
        val aId = requireArguments().getLong(EXTRA_ACCOUNT_ID, -1)
        val nId = requireArguments().getString(EXTRA_NOTE_ID)
        val type = requireArguments().getString(EXTRA_TYPE)
        requireNotNull(nId)
        require(aId != -1L)
        val noteId = Note.Id(aId, nId)
        ReactionHistoryViewModel.provideViewModel(assistedFactory, noteId, type)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listAdapter = ReactionHistoryListAdapter(requireActivity())
        binding.historiesView.adapter = listAdapter
        mLinearLayoutManager = LinearLayoutManager(requireContext())
        binding.historiesView.layoutManager = mLinearLayoutManager
        binding.historiesView.addOnScrollListener(mScrollListener)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.uiState.collect { uiState ->
                    listAdapter.submitList(uiState.items)
                }
            }
        }
        viewModel.next()
    }

    private val mScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            val endVisibleItemPosition = mLinearLayoutManager.findLastVisibleItemPosition()
            val itemCount = mLinearLayoutManager.itemCount

            if (endVisibleItemPosition == (itemCount - 1)) {
                viewModel.next()
            }

        }
    }
}