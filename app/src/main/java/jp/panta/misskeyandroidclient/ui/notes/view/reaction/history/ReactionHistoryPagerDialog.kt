package jp.panta.misskeyandroidclient.ui.notes.view.reaction.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.DialogReactionHistoryPagerBinding
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.reaction.ReactionHistoryRequest
import jp.panta.misskeyandroidclient.viewmodel.MiCore
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.reaction.ReactionHistoryPagerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ReactionHistoryPagerDialog : BottomSheetDialogFragment(){

    companion object {
        private const val EXTRA_NOTE_ID = "EXTRA_NOTE_ID"
        private const val EXTRA_ACCOUNT_ID = "EXTRA_ACCOUNT_ID"
        private const val EXTRA_SHOW_REACTION_TYPE = "EXTRA_SHOW_REACTION_TYPE"

        fun newInstance(noteId: net.pantasystem.milktea.model.notes.Note.Id, showReaction: String? = null): ReactionHistoryPagerDialog {
            return ReactionHistoryPagerDialog().also {
                it.arguments = Bundle().also { bundle ->
                    bundle.putString(EXTRA_NOTE_ID, noteId.noteId)
                    bundle.putLong(EXTRA_ACCOUNT_ID, noteId.accountId)
                    showReaction?.let { type ->
                        bundle.putString(EXTRA_SHOW_REACTION_TYPE, type)
                    }
                }
            }
        }
    }

    lateinit var binding: DialogReactionHistoryPagerBinding
    lateinit var noteId: net.pantasystem.milktea.model.notes.Note.Id


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_reaction_history_pager, container, false)
        return binding.root
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val aId = requireArguments().getLong(EXTRA_ACCOUNT_ID, - 1)
        val nId = requireArguments().getString(EXTRA_NOTE_ID)
        require(aId != -1L)
        requireNotNull(nId)
        val showCurrentReaction = requireArguments().getString(EXTRA_SHOW_REACTION_TYPE)

        val noteId = net.pantasystem.milktea.model.notes.Note.Id(aId, nId)
        this.noteId = noteId

        val miCore = requireContext().applicationContext as MiCore
        binding.reactionHistoryTab.setupWithViewPager(binding.reactionHistoryPager)
        val pagerViewModel = ViewModelProvider(this, ReactionHistoryPagerViewModel.Factory(noteId, miCore))[ReactionHistoryPagerViewModel::class.java]

        lifecycleScope.launchWhenCreated {
            pagerViewModel.types.collect { list ->
                val types = list.toMutableList().also {
                    it.add(0,
                        net.pantasystem.milktea.model.notes.reaction.ReactionHistoryRequest(
                            noteId,
                            null
                        )
                    )
                }
                val index = showCurrentReaction.let { type ->

                    types.indexOfFirst {
                        it.type == type
                    }
                }
                withContext(Dispatchers.Main) {
                    showPager(noteId, types)
                    binding.reactionHistoryPager.currentItem = index
                }
            }
        }


    }

    private fun showPager(noteId: net.pantasystem.milktea.model.notes.Note.Id, types: List<net.pantasystem.milktea.model.notes.reaction.ReactionHistoryRequest>) {
        val adapter = ReactionHistoryPagerAdapter(childFragmentManager, types, noteId)
        binding.reactionHistoryPager.adapter = adapter
    }

    override fun onPause() {
        super.onPause()
        dismissAllowingStateLoss()
    }
    override fun onDestroy() {
        super.onDestroy()
        val miCore = requireContext().applicationContext as MiCore
        requireActivity().lifecycleScope.launch(Dispatchers.IO) {
            miCore.getReactionHistoryDataSource().clear(noteId)
        }
    }
}