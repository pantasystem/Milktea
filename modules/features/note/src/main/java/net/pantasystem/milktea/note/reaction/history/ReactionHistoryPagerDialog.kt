package net.pantasystem.milktea.note.reaction.history

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.children
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.common_android.ui.text.CustomEmojiDecorator
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.reaction.Reaction
import net.pantasystem.milktea.model.notes.reaction.ReactionHistoryDataSource
import net.pantasystem.milktea.model.notes.reaction.ReactionHistoryRequest
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.DialogReactionHistoryPagerBinding
import net.pantasystem.milktea.note.reaction.viewmodel.ReactionHistoryPagerUiState
import net.pantasystem.milktea.note.reaction.viewmodel.ReactionHistoryPagerViewModel
import javax.inject.Inject

@AndroidEntryPoint
class ReactionHistoryPagerDialog : BottomSheetDialogFragment() {

    companion object {
        private const val EXTRA_NOTE_ID = "EXTRA_NOTE_ID"
        private const val EXTRA_ACCOUNT_ID = "EXTRA_ACCOUNT_ID"
        private const val EXTRA_SHOW_REACTION_TYPE = "EXTRA_SHOW_REACTION_TYPE"

        fun newInstance(noteId: Note.Id, showReaction: String? = null): ReactionHistoryPagerDialog {
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

    private var _binding: DialogReactionHistoryPagerBinding? = null
    val binding: DialogReactionHistoryPagerBinding
        get() = requireNotNull(_binding)

    private val pagerViewModel by viewModels<ReactionHistoryPagerViewModel>()

    @Inject
    internal lateinit var reactionHistoryDataSource: ReactionHistoryDataSource

    private val aId: Long by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().getLong(EXTRA_ACCOUNT_ID, -1).apply {
            require(this != -1L)
        }
    }

    private val nId: String by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments().getString(EXTRA_NOTE_ID)!!
    }

    private val noteId by lazy {
        Note.Id(aId, nId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DataBindingUtil.inflate(
            inflater,
            R.layout.dialog_reaction_history_pager,
            container,
            false
        )
        return binding.root
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pagerViewModel.setNoteId(noteId)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val showCurrentReaction = requireArguments().getString(EXTRA_SHOW_REACTION_TYPE)


        binding.reactionHistoryTab.setupWithViewPager(binding.reactionHistoryPager)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                pagerViewModel.uiState.collect { uiState ->
                    val list = uiState.types
                    val types = list.toMutableList().also {
                        it.add(
                            0,
                            ReactionHistoryRequest(
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
                        setCustomEmojiSpanIntoTabs(uiState)
                    }
                }
            }

        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setCustomEmojiSpanIntoTabs(uiState: ReactionHistoryPagerUiState) {

        if (uiState.note != null) {
            val types = uiState.types
            for (i in 1 until types.size) {
                val tab = binding.reactionHistoryTab.getTabAt(i + 1)!!
                val textView = tab.view.children.firstOrNull {
                    it is TextView && it.id == -1
                } as? TextView

                if (textView != null) {
                    val spanned = CustomEmojiDecorator().decorate(
                        accountHost = uiState.account?.getHost(),
                        sourceHost = uiState.noteAuthor?.host,
                        uiState.note.emojis,
                        types[i].type ?: "",
                        textView,
                    )

                    tab.text = SpannableStringBuilder(spanned).apply {
                        if (Reaction(types[i].type ?: "").isCustomEmojiFormat()) {
                            append(types[i].type ?: "")
                        }
                    }
                }

            }
        }
    }

    private fun showPager(noteId: Note.Id, types: List<ReactionHistoryRequest>) {
        val adapter = ReactionHistoryPagerAdapter(childFragmentManager, types, noteId)
        binding.reactionHistoryPager.adapter = adapter
    }

    override fun onPause() {
        super.onPause()
        dismissAllowingStateLoss()
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().lifecycleScope.launch(Dispatchers.IO) {
            reactionHistoryDataSource.clear(noteId)
        }
    }
}