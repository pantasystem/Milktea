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
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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
import net.pantasystem.milktea.model.note.reaction.LegacyReaction
import net.pantasystem.milktea.model.note.reaction.Reaction
import net.pantasystem.milktea.model.note.reaction.ReactionSelection
import net.pantasystem.milktea.model.setting.DefaultConfig
import net.pantasystem.milktea.model.setting.LocalConfigRepository
import net.pantasystem.milktea.note.EmojiListItemType
import net.pantasystem.milktea.note.EmojiPickerUiStateService
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.FragmentEmojiPickerBinding
import net.pantasystem.milktea.note.emojis.viewmodel.EmojiPickerViewModel
import net.pantasystem.milktea.note.reaction.choices.EmojiListItemsAdapter
import net.pantasystem.milktea.note.toTextReaction
import javax.inject.Inject

@AndroidEntryPoint
class EmojiPickerFragment : Fragment(R.layout.fragment_emoji_picker), ReactionSelection {

    companion object {
        fun newInstance(accountId: Long?): EmojiPickerFragment {
            return EmojiPickerFragment().also { fragment ->
                fragment.arguments = Bundle().apply {
                    putLong(EmojiPickerUiStateService.EXTRA_ACCOUNT_ID, accountId ?: -1L)
                }
            }
        }
    }
    interface OnEmojiSelectedListener {
        fun onSelect(emoji: String)
    }

    private val binding: FragmentEmojiPickerBinding by dataBinding()

    private val emojiPickerViewModel: EmojiPickerViewModel by viewModels()

    @Inject
    internal lateinit var configRepository: LocalConfigRepository

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
            emojiPickerEmojiSize = configRepository.get().getOrElse { DefaultConfig.config }.emojiPickerEmojiDisplaySize
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
    val emojiPickerEmojiSize: Int,
) {

    fun bind() {


        val adapter = EmojiListItemsAdapter(
            isApplyImageAspectRatio = true,
            onEmojiLongClicked = { emojiType ->
                val exists = emojiPickerViewModel.uiState.value.isExistsConfig(emojiType)
                if (!exists) {
                    AddEmojiToUserConfigDialog.newInstance(emojiType)
                        .show(fragmentManager, AddEmojiToUserConfigDialog.FRAGMENT_TAG)
                    true
                } else {
                    false
                }
            },
            onEmojiSelected = {
                onReactionSelected(it.toTextReaction())
            },
            baseItemSizeDp = emojiPickerEmojiSize,
        )


        val layoutManager by lazy {
            val flexBoxLayoutManager = FlexboxLayoutManager(context)
            flexBoxLayoutManager.alignItems = AlignItems.STRETCH
            flexBoxLayoutManager
        }

        recyclerView.layoutManager = layoutManager

        recyclerView.adapter = adapter
        recyclerView.itemAnimator = null


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