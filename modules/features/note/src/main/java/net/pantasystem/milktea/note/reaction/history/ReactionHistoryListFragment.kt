package net.pantasystem.milktea.note.reaction.history

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.FragmentReactionHistoryListBinding
import net.pantasystem.milktea.note.reaction.viewmodel.ReactionHistoryViewModel

@AndroidEntryPoint
class ReactionHistoryListFragment : Fragment(R.layout.fragment_reaction_history_list) {

    companion object {
        fun newInstance(noteId: Note.Id, type: String? = null) : ReactionHistoryListFragment {
            return ReactionHistoryListFragment().also {
                it.arguments = Bundle().also { bundle ->
                    bundle.putSerializable(ReactionHistoryViewModel.EXTRA_NOTE_ID, noteId)
                    type?.let{
                        bundle.putString(ReactionHistoryViewModel.EXTRA_TYPE, type)
                    }
                }
            }
        }
    }

    val binding: FragmentReactionHistoryListBinding by dataBinding()
    lateinit var mLinearLayoutManager: LinearLayoutManager

    private val viewModel: ReactionHistoryViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


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