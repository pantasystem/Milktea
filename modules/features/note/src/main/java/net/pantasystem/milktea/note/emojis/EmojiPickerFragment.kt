package net.pantasystem.milktea.note.emojis

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.tabs.TabLayout
import com.wada811.databinding.dataBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common_android_ui.tab.TabbedFlexboxListMediator
import net.pantasystem.milktea.model.notes.reaction.LegacyReaction
import net.pantasystem.milktea.model.notes.reaction.Reaction
import net.pantasystem.milktea.model.notes.reaction.ReactionSelection
import net.pantasystem.milktea.note.EmojiListItemType
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.FragmentEmojiPickerBinding
import net.pantasystem.milktea.note.emojis.viewmodel.EmojiPickerViewModel
import net.pantasystem.milktea.note.reaction.choices.EmojiListItemsAdapter
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
            scope = viewLifecycleOwner.lifecycleScope,
            fragmentManager = childFragmentManager,
            lifecycleOwner = viewLifecycleOwner,
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
    val tabLayout: TabLayout,
    val recyclerView: RecyclerView,
    val searchWordTextField: EditText,
    val emojiPickerViewModel: EmojiPickerViewModel,
    val onReactionSelected: (String) -> Unit,
    val onSearchEmojiTextFieldEntered: (String) -> Unit,
) {

    fun bind() {


        val adapter = EmojiListItemsAdapter(
            isApplyImageAspectRatio = true,
            onEmojiLongClicked = { emojiType ->
                val exists = emojiPickerViewModel.uiState.value.isExistsConfig(emojiType)
                if (!exists) {
                    AddEmojiToUserConfigDialog.newInstance(emojiType)
                        .show(fragmentManager, "AddEmojiToUserConfigDialog")
                    true
                } else {
                    false
                }
            },
            onEmojiSelected = {
                onReactionSelected(it.toTextReaction())
            }
        )


        val layoutManager by lazy {
            val flexBoxLayoutManager = FlexboxLayoutManager(context)
            flexBoxLayoutManager.alignItems = AlignItems.STRETCH
            flexBoxLayoutManager
        }

        recyclerView.layoutManager = layoutManager

        recyclerView.adapter = adapter


        scope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                emojiPickerViewModel.uiState.collect {
                    adapter.submitList(it.emojiListItems)
                }
            }
        }

        var tabbedListMediator: TabbedFlexboxListMediator? = null
        emojiPickerViewModel.uiState.filterNot {
            it.tabHeaderLabels.isEmpty()
        }.distinctUntilChangedBy {
            it.emojiListItems
        }.onEach {
            tabLayout.removeAllTabs()
            val labels = it.tabHeaderLabels
            labels.forEach {
                val tab = tabLayout.newTab().apply {
                    text = it.getString(context)
                }
                tabLayout.addTab(tab, false)
            }
            tabbedListMediator?.detach()
            tabbedListMediator = TabbedFlexboxListMediator(
                recyclerView,
                tabLayout,
                it.emojiListItems.mapIndexedNotNull { index, emojiListItemType ->
                    when(emojiListItemType) {
                        is EmojiListItemType.EmojiItem -> null
                        is EmojiListItemType.Header -> index
                    }
                }
            )
            tabbedListMediator?.attach()
        }.flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.RESUMED).launchIn(scope)


        searchWordTextField.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                onSearchEmojiTextFieldEntered(emojiPickerViewModel.searchWord.value)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

    }
}