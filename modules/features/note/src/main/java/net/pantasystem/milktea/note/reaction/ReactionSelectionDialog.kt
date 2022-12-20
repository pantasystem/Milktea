@file:Suppress("DEPRECATION")

package net.pantasystem.milktea.note.reaction

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.reaction.ReactionSelection
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.DialogSelectReactionBinding
import net.pantasystem.milktea.note.reaction.choices.EmojiChoicesAdapter
import net.pantasystem.milktea.note.reaction.choices.ReactionChoicesFragment
import net.pantasystem.milktea.note.reaction.viewmodel.ReactionSelectionDialogViewModel
import net.pantasystem.milktea.note.reaction.viewmodel.TabType
import net.pantasystem.milktea.note.reaction.viewmodel.toTextReaction
import net.pantasystem.milktea.note.viewmodel.NotesViewModel
import javax.inject.Inject

@AndroidEntryPoint
class ReactionSelectionDialog : BottomSheetDialogFragment(),
    ReactionSelection {

    companion object {
        fun newInstance(noteId: Note.Id): ReactionSelectionDialog {
            return ReactionSelectionDialog().apply {
                arguments = Bundle().apply {
                    putLong("ACCOUNT_ID", noteId.accountId)
                    putString("NOTE_ID", noteId.noteId)
                }
            }
        }
    }

    private var mNoteViewModel: NotesViewModel? = null
    val notesViewModel by activityViewModels<NotesViewModel>()


    @Inject
    lateinit var accountStore: AccountStore

    val viewModel: ReactionSelectionDialogViewModel by viewModels()

    private val noteId: Note.Id by lazy {
        Note.Id(
            requireArguments().getLong("ACCOUNT_ID"),
            requireArguments().getString("NOTE_ID")!!
        )
    }

    private val flexBoxLayoutManager: FlexboxLayoutManager by lazy {
        val flexBoxLayoutManager = FlexboxLayoutManager(requireContext())
        flexBoxLayoutManager.alignItems = AlignItems.STRETCH
        flexBoxLayoutManager
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("ReactionSelectionDialog", "ReactionSelectionDialog#onCreateView")
        return inflater.inflate(R.layout.dialog_select_reaction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = DialogSelectReactionBinding.bind(view)
        binding.reactionSelectionViewModel = viewModel
        binding.lifecycleOwner = this


        val searchedReactionAdapter = EmojiChoicesAdapter {
            notesViewModel.toggleReaction(noteId, it.toTextReaction())
            dismiss()
        }

        binding.searchSuggestionsView.adapter = searchedReactionAdapter
        binding.searchSuggestionsView.layoutManager = flexBoxLayoutManager

        mNoteViewModel = notesViewModel
        binding.reactionChoicesTab.setupWithViewPager(binding.reactionChoicesViewPager)

        val adapter = ReactionChoicesPagerAdapter(childFragmentManager, requireContext())
        binding.reactionChoicesViewPager.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.categories.collect { categories ->
                    adapter.setList(categories.toList())
                }
            }

        }


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.filteredEmojis.collect { list ->
                    searchedReactionAdapter.submitList(list)
                }
            }
        }

        binding.searchReactionEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                notesViewModel.toggleReaction(noteId, viewModel.searchWord.value)
                dismiss()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

    }

    override fun selectReaction(reaction: String) {
        mNoteViewModel?.toggleReaction(noteId, reaction)
        dismiss()
    }

    class ReactionChoicesPagerAdapter(fragmentManager: FragmentManager, val context: Context) :
        FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private var categoryList: List<TabType> = emptyList()
        override fun getCount(): Int {
            return categoryList.size
        }

        override fun getPageTitle(position: Int): CharSequence {
            return when(val type = categoryList[position]) {
                TabType.All -> context.getString(R.string.all)
                is TabType.Category -> type.name
                TabType.OftenUse -> context.getString(R.string.often_use)
                TabType.UserCustom -> context.getString(R.string.user)
            }
        }

        override fun getItem(position: Int): Fragment {
            return when(val type = categoryList[position]) {
                TabType.All -> ReactionChoicesFragment.newInstance(ReactionChoicesFragment.Type.DEFAULT)
                is TabType.Category -> ReactionChoicesFragment.newInstance(
                    ReactionChoicesFragment.Type.CATEGORY,
                    type.name,
                )
                TabType.OftenUse -> ReactionChoicesFragment.newInstance(ReactionChoicesFragment.Type.FREQUENCY)
                TabType.UserCustom -> ReactionChoicesFragment.newInstance(ReactionChoicesFragment.Type.USER)
            }

        }

        fun setList(list: List<TabType>) {
            categoryList = list
            notifyDataSetChanged()
        }

    }

}

