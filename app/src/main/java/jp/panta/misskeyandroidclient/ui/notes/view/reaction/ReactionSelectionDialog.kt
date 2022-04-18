@file:Suppress("DEPRECATION")
package jp.panta.misskeyandroidclient.ui.notes.view.reaction

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.MiApplication
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.DialogSelectReactionBinding
import net.pantasystem.milktea.model.notes.reaction.ReactionSelection
import jp.panta.misskeyandroidclient.ui.notes.view.reaction.choices.ReactionChoicesFragment
import jp.panta.misskeyandroidclient.ui.notes.view.reaction.choices.ReactionInputDialog
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.NotesViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

@AndroidEntryPoint
class ReactionSelectionDialog : BottomSheetDialogFragment(),
    net.pantasystem.milktea.model.notes.reaction.ReactionSelection {

    private var mNoteViewModel: NotesViewModel? = null
    val notesViewModel by activityViewModels<NotesViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("ReactionSelectionDialog", "ReactionSelectionDialog#onCreateView")
        return inflater.inflate(R.layout.dialog_select_reaction, container, false)
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = DialogSelectReactionBinding.bind(view)

        val activity = activity
        val miApplication = context?.applicationContext as MiApplication
        val ar  = miApplication.getAccountStore().currentAccount

        activity?: return
        ar?: return

        mNoteViewModel = notesViewModel


        miApplication.getAccountStore().observeCurrentAccount.filterNotNull().flatMapLatest {
            miApplication.getMetaRepository().observe(it.instanceDomain)
        }.mapNotNull {
            it?.emojis
        }.map { emojis ->
            emojis.filter {
                it.category != null
            }.groupBy {
                it.category?: ""
            }.keys
        }.onEach { category ->
            val pagerAdapter = ReactionChoicesPagerAdapter(category)
            binding.reactionChoicesViewPager.adapter = pagerAdapter
            binding.reactionChoicesTab.setupWithViewPager(binding.reactionChoicesViewPager)
        }.launchIn(lifecycleScope)


        binding.reactionInputKeyboard.setOnClickListener {
            ReactionInputDialog().show(parentFragmentManager, "")
            dismiss()
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
        @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
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