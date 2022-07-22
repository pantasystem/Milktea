@file:Suppress("DEPRECATION")
package jp.panta.misskeyandroidclient.ui.notes.view.reaction

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.DialogSelectReactionBinding
import jp.panta.misskeyandroidclient.ui.notes.view.reaction.choices.ReactionChoicesFragment
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.NotesViewModel
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.reaction.ReactionSelectionDialogViewModel
import jp.panta.misskeyandroidclient.ui.reaction.ReactionChoicesAdapter
import net.pantasystem.milktea.model.account.AccountStore
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.model.notes.reaction.ReactionSelection
import javax.inject.Inject

@AndroidEntryPoint
class ReactionSelectionDialog : BottomSheetDialogFragment(),
    ReactionSelection {

    private var mNoteViewModel: NotesViewModel? = null
    val notesViewModel by activityViewModels<NotesViewModel>()

    @Inject
    lateinit var metaRepository: MetaRepository

    @Inject
    lateinit var accountStore: AccountStore

    val viewModel: ReactionSelectionDialogViewModel by viewModels()

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

        val searchedReactionAdapter = ReactionChoicesAdapter {
            notesViewModel.postReaction(it)
        }
        binding.searchSuggestionsView.adapter = searchedReactionAdapter
        binding.searchSuggestionsView.layoutManager = flexBoxLayoutManager

        mNoteViewModel = notesViewModel
        binding.reactionChoicesTab.setupWithViewPager(binding.reactionChoicesViewPager)

        lifecycleScope.launchWhenResumed {
            viewModel.categories.collect { categories ->
                binding.reactionChoicesViewPager.adapter = ReactionChoicesPagerAdapter(categories.toSet())
            }
        }


        lifecycleScope.launchWhenResumed {
            viewModel.filteredEmojis.collect { list ->
                searchedReactionAdapter.submitList(list)
            }
        }

        binding.searchReactionEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                notesViewModel.postReaction(viewModel.searchWord.value)
                dismiss()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

    }

    override fun selectReaction(reaction: String) {
        mNoteViewModel?.postReaction(reaction)
        dismiss()
    }

    inner class ReactionChoicesPagerAdapter(
        category: Set<String>
    ) : FragmentPagerAdapter(childFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT){

        private val categoryList = category.toList()
        override fun getCount(): Int {
            return 3 + categoryList.size
        }

        override fun getPageTitle(position: Int): CharSequence {
            return when(position){
                0 ->{
                    getString(R.string.user)
                }
                1 ->{
                    getString(R.string.often_use)
                }
                2 ->{
                    getString(R.string.all)
                }

                else ->{
                    categoryList[position - 3]
                }
            }
        }
        override fun getItem(position: Int): Fragment {
            return when(position){
                0 ->{
                    ReactionChoicesFragment.newInstance(ReactionChoicesFragment.Type.USER)
                }
                1 ->{
                    ReactionChoicesFragment.newInstance(ReactionChoicesFragment.Type.FREQUENCY)
                }
                2 ->{
                    ReactionChoicesFragment.newInstance(ReactionChoicesFragment.Type.DEFAULT)
                }
                else ->{
                    val categoryName = categoryList[position - 3]
                    ReactionChoicesFragment.newInstance(ReactionChoicesFragment.Type.CATEGORY, categoryName)
                }
            }
        }
    }

}