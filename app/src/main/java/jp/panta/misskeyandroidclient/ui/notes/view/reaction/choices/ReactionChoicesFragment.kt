package jp.panta.misskeyandroidclient.ui.notes.view.reaction.choices

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentReactionChoicesBinding
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.reaction.ReactionChoicesViewModel
import jp.panta.misskeyandroidclient.ui.reaction.ReactionChoicesAdapter
import kotlinx.coroutines.launch
import net.pantasystem.milktea.model.account.AccountStore
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.model.notes.reaction.ReactionSelection
import net.pantasystem.milktea.model.notes.reaction.history.ReactionHistoryDao
import net.pantasystem.milktea.model.notes.reaction.usercustom.ReactionUserSettingDao
import javax.inject.Inject

@AndroidEntryPoint
class ReactionChoicesFragment : Fragment() {

    companion object {
        private const val EXTRA_TYPE =
            "jp.panta.misskeyandroidclient.ui.notes.ReactionChoicesFragment.EXTRA_TYPE"
        private const val EXTRA_CATEGORY =
            "jp.panta.misskeyandroidclient.vaie.notes.reaction.choices.ReactionChoicesFragment.EXTRA_CATEGORY"

        fun newInstance(type: Type, category: String? = null): ReactionChoicesFragment {
            val fragment = ReactionChoicesFragment()
            val bundle = Bundle().apply {
                putInt(EXTRA_TYPE, type.ordinal)
                putString(EXTRA_CATEGORY, category)
            }
            fragment.arguments = bundle
            return fragment

        }

    }

    enum class Type {
        DEFAULT,
        FREQUENCY,
        CATEGORY,
        USER
    }

    @Inject lateinit var reactionHistoryDao: ReactionHistoryDao
    @Inject lateinit var reactionUserSettingDao: ReactionUserSettingDao

    @Inject
    lateinit var metaRepository: MetaRepository

    @Inject
    lateinit var accountStore: AccountStore

    val viewModel: ReactionChoicesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reaction_choices, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentReactionChoicesBinding.bind(view)


        val columns = view.context.resources.getInteger(R.integer.reaction_choices_columns)

        val adapter =
            ReactionChoicesAdapter {
                val selection = (parentFragment as? ReactionSelection)
                    ?: (requireActivity() as? ReactionSelection)
                Log.w("ReactionChoicesFragment", "ReactionSelectionの実装が行われていません")
                selection?.selectReaction(it)
            }
        val layoutManager = GridLayoutManager(view.context, columns)

        binding.reactionsView.layoutManager = layoutManager
        binding.reactionsView.adapter = adapter


        val typeOrdinal = arguments?.getInt(EXTRA_TYPE) ?: 0

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.uiState.collect { uiState ->
                    when(Type.values()[typeOrdinal]) {
                        Type.DEFAULT -> {
                            adapter.submitList(uiState.all)
                        }
                        Type.FREQUENCY -> {
                            adapter.submitList(uiState.frequencyUsedReactions)
                        }
                        Type.CATEGORY -> {
                            val category = arguments?.getString(EXTRA_CATEGORY) ?: return@collect
                            adapter.submitList(uiState.getCategoryBy(category))
                        }
                        Type.USER -> {
                            adapter.submitList(uiState.userSettingTextReactions)
                        }
                    }
                }
            }
        }

    }

}