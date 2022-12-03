package net.pantasystem.milktea.note.reaction.history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.FragmentReactionHistoryListBinding
import net.pantasystem.milktea.note.reaction.viewmodel.ReactionHistoryViewModel
import net.pantasystem.milktea.note.reaction.viewmodel.provideViewModel
import javax.inject.Inject

@AndroidEntryPoint
class ReactionHistoryListFragment : Fragment() {

    companion object {
        private const val EXTRA_NOTE_ID = "NOTE_ID"
        private const val EXTRA_ACCOUNT_ID = "EXTRA_ACCOUNT_ID"
        private const val EXTRA_TYPE = "EXTRA_TYPE"

        fun newInstance(noteId: Note.Id, type: String? = null) : ReactionHistoryListFragment {
            return ReactionHistoryListFragment().also {
                it.arguments = Bundle().also { bundle ->
                    bundle.putString(EXTRA_NOTE_ID, noteId.noteId)
                    bundle.putLong(EXTRA_ACCOUNT_ID, noteId.accountId)
                    type?.let{
                        bundle.putString(EXTRA_TYPE, type)
                    }
                }
            }
        }
    }

    lateinit var binding: FragmentReactionHistoryListBinding
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_reaction_history_list, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val aId = requireArguments().getLong(EXTRA_ACCOUNT_ID, -1)
        val nId = requireArguments().getString(EXTRA_NOTE_ID)
        requireNotNull(nId)
        require(aId != -1L)
        viewModel.isLoading.observe(viewLifecycleOwner) {
            if(it == true && viewModel.histories.value.isNullOrEmpty()) {
                // 初期読み込み
                binding.progress.visibility = View.VISIBLE
                binding.historiesView.visibility = View.GONE
            }else{
                binding.progress.visibility = View.GONE
                binding.historiesView.visibility = View.VISIBLE
            }
        }
        val simpleUserListAdapter =
            net.pantasystem.milktea.common_android_ui.user.SimpleUserListAdapter(requireActivity())
        binding.historiesView.adapter = simpleUserListAdapter
        mLinearLayoutManager = LinearLayoutManager(requireContext())
        binding.historiesView.layoutManager = mLinearLayoutManager
        binding.historiesView.addOnScrollListener(mScrollListener)
        viewModel.histories.observe(viewLifecycleOwner) {
            it?.let {
                it.map { rh ->
                    rh.user
                }.let { users ->
                    simpleUserListAdapter.submitList(users)
                }
            }
        }
        viewModel.next()
    }

    private val mScrollListener = object : RecyclerView.OnScrollListener(){
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)

            val endVisibleItemPosition = mLinearLayoutManager.findLastVisibleItemPosition()
            val itemCount = mLinearLayoutManager.itemCount


            if(endVisibleItemPosition == (itemCount - 1)){
                Log.d("", "後ろ")
                viewModel.next()

            }

        }
    }
}