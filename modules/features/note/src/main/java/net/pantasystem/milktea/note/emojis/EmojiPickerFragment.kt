package net.pantasystem.milktea.note.emojis

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
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
import com.google.android.material.tabs.TabLayout
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.launch
import net.pantasystem.milktea.model.notes.reaction.LegacyReaction
import net.pantasystem.milktea.model.notes.reaction.Reaction
import net.pantasystem.milktea.model.notes.reaction.ReactionSelection
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.FragmentEmojiPickerBinding
import net.pantasystem.milktea.note.emojis.viewmodel.EmojiPickerViewModel
import net.pantasystem.milktea.note.reaction.choices.EmojiChoicesAdapter
import net.pantasystem.milktea.note.reaction.choices.EmojiChoicesListAdapter
import net.pantasystem.milktea.note.toTextReaction

@AndroidEntryPoint
class EmojiPickerFragment : Fragment(R.layout.fragment_emoji_picker), ReactionSelection {

    interface OnEmojiSelectedListener {
        fun onSelect(emoji: String)
    }

    private val binding: FragmentEmojiPickerBinding by dataBinding()

    private val emojiPickerViewModel: EmojiPickerViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.emojiPickerViewModel = emojiPickerViewModel
        val binder = EmojiSelectionBinder(
            context = requireContext(),
            scope = lifecycleScope,
            fragmentManager = childFragmentManager,
            lifecycleOwner = viewLifecycleOwner,
            searchSuggestionListView = binding.searchSuggestionsView,
            tabLayout = binding.reactionChoicesTab,
            recyclerView = binding.reactionChoicesViewPager,
            searchWordTextField = binding.searchReactionEditText,
            onReactionSelected = {
                val selected = if (Reaction(it).isCustomEmojiFormat()) {
                    it
                } else {
                    LegacyReaction.reactionMap[it] ?: it
                }
                onSelect(selected)
            },
            onSearchEmojiTextFieldEntered = {
                onSelect(it)
            },
            emojiPickerViewModel = emojiPickerViewModel,
        )
        binder.bind()

    }

    override fun selectReaction(reaction: String) {
        onSelect(reaction)
    }

    fun onSelect(emoji: String) {
        val activity = requireActivity()
        val parent = parentFragment
        if (parent is OnEmojiSelectedListener) {
            parent.onSelect(emoji)
        } else if (activity is OnEmojiSelectedListener) {
            activity.onSelect(emoji)
        }
    }
}


class EmojiSelectionBinder(
    val context: Context,
    val scope: CoroutineScope,
    val fragmentManager: FragmentManager,
    val lifecycleOwner: LifecycleOwner,
    val searchSuggestionListView: RecyclerView,
    val tabLayout: TabLayout,
    val recyclerView: RecyclerView,
    val searchWordTextField: EditText,
    val emojiPickerViewModel: EmojiPickerViewModel,
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
                emojiPickerViewModel.uiState.collect {
                    choicesAdapter.submitList(it.segments)
                    searchedReactionAdapter.submitList(it.searchFilteredEmojis)
                }
            }
        }

        var tabbedListMediator: TabbedListMediator? = null
        scope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                emojiPickerViewModel.tabLabels.filterNot {
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
                    tabbedListMediator =
                        TabbedListMediator(recyclerView, tabLayout, it.indices.toList())
                    tabbedListMediator?.attach()
                }
            }
        }


        searchWordTextField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                onSearchEmojiTextFieldEntered(emojiPickerViewModel.searchWord.value)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

    }
}