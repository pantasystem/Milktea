package jp.panta.misskeyandroidclient.view.notes.reaction.history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentReactionHistoryListBinding
import jp.panta.misskeyandroidclient.model.notes.Note
import jp.panta.misskeyandroidclient.view.users.SimpleUserListAdapter
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.viewmodel.notes.reaction.ReactionHistoryViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

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
    lateinit var mViewModel: ReactionHistoryViewModel

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
        val type = requireArguments().getString(EXTRA_TYPE)
        requireNotNull(nId)
        require(aId != -1L)
        val noteId = Note.Id(aId, nId)
        val miCore = context?.applicationContext as MiCore
        val viewModel = ViewModelProvider(this, ReactionHistoryViewModel.Factory(noteId, type, miCore))[ReactionHistoryViewModel::class.java]
        mViewModel = viewModel
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
        val simpleUserListAdapter = SimpleUserListAdapter(requireActivity())
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
                mViewModel.next()

            }

        }
    }
}