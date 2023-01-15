
package net.pantasystem.milktea.note.reaction
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ahmadhamwi.tabsync.TabbedListMediator
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.launch
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.reaction.ReactionSelection
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.DialogSelectReactionBinding
import net.pantasystem.milktea.note.reaction.choices.EmojiChoicesAdapter
import net.pantasystem.milktea.note.reaction.choices.EmojiChoicesListAdapter
import net.pantasystem.milktea.note.reaction.viewmodel.ReactionChoicesViewModel
import net.pantasystem.milktea.note.toTextReaction
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

    @Inject
    lateinit var accountStore: AccountStore

    val notesViewModel by activityViewModels<NotesViewModel>()

//    val viewModel: ReactionSelectionDialogViewModel by viewModels()

    private val reactionChoicesViewModel: ReactionChoicesViewModel by viewModels()

    private val noteId: Note.Id by lazy {
        Note.Id(
            requireArguments().getLong("ACCOUNT_ID"),
            requireArguments().getString("NOTE_ID")!!
        )
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
        binding.reactionSelectionViewModel = reactionChoicesViewModel
        binding.lifecycleOwner = this

        val binder = ReactionSelectionDialogBinder(
            context = requireContext(),
            scope = lifecycleScope,
            fragmentManager = childFragmentManager,
            lifecycleOwner = viewLifecycleOwner,
            searchSuggestionListView = binding.searchSuggestionsView,
            tabLayout = binding.reactionChoicesTab,
            recyclerView = binding.reactionChoicesViewPager,
            searchWordTextField = binding.searchReactionEditText,
            onReactionSelected = {
                notesViewModel.toggleReaction(noteId, it)
                dismiss()
            },
            onSearchEmojiTextFieldEntered = {
                notesViewModel.toggleReaction(noteId, it)
                dismiss()
            },
            reactionChoicesViewModel = reactionChoicesViewModel,
        )
        binder.bind()

    }

    override fun selectReaction(reaction: String) {
        notesViewModel.toggleReaction(noteId, reaction)
        dismiss()
    }



}

class ReactionSelectionDialogBinder(
    val context: Context,
    val scope: CoroutineScope,
    val fragmentManager: FragmentManager,
    val lifecycleOwner: LifecycleOwner,
    val searchSuggestionListView: RecyclerView,
    val tabLayout: TabLayout,
    val recyclerView: RecyclerView,
    val searchWordTextField: EditText,
    val reactionChoicesViewModel: ReactionChoicesViewModel,
    val onReactionSelected: (String) -> Unit,
    val onSearchEmojiTextFieldEntered: (String) -> Unit,
) {

    private val flexBoxLayoutManager: FlexboxLayoutManager by lazy {
        val flexBoxLayoutManager = FlexboxLayoutManager(context)
        flexBoxLayoutManager.alignItems = AlignItems.STRETCH
        flexBoxLayoutManager
    }

    fun bind() {
        val searchedReactionAdapter = EmojiChoicesAdapter {
            onReactionSelected(it.toTextReaction())
        }
        searchSuggestionListView.adapter = searchedReactionAdapter
        searchSuggestionListView.layoutManager = flexBoxLayoutManager

        val layoutManager = LinearLayoutManager(context)

        val choicesAdapter = EmojiChoicesListAdapter {
            onReactionSelected(it.toTextReaction())
        }
        recyclerView.adapter = choicesAdapter

        recyclerView.layoutManager = layoutManager


        scope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                reactionChoicesViewModel.uiState.collect {
                    choicesAdapter.submitList(it.segments)
                    searchedReactionAdapter.submitList(it.searchFilteredEmojis)
                }
            }
        }

        var tabbedListMediator: TabbedListMediator? = null
        scope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                reactionChoicesViewModel.tabLabels.filterNot {
                    it.isEmpty()
                }.collect {
                    tabLayout.removeAllTabs()
                    it.map {
                        val tab = tabLayout.newTab().apply {
                            text = it.getString(context)
                        }
                        tabLayout.addTab(tab)
                    }
                    tabbedListMediator?.detach()
                    tabbedListMediator = TabbedListMediator(recyclerView, tabLayout, it.indices.toList())
                    tabbedListMediator?.attach()
                }
            }
        }


        searchWordTextField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                onSearchEmojiTextFieldEntered(reactionChoicesViewModel.searchWord.value)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

    }
}