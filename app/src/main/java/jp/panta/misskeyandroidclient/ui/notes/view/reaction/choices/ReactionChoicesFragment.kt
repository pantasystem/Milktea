package jp.panta.misskeyandroidclient.ui.notes.view.reaction.choices

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.FragmentReactionChoicesBinding
import jp.panta.misskeyandroidclient.ui.reaction.ReactionChoicesAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.pantasystem.milktea.model.account.AccountStore
import net.pantasystem.milktea.model.instance.MetaRepository
import net.pantasystem.milktea.model.notes.reaction.LegacyReaction
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reaction_choices, container, false)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
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

        when (Type.values()[typeOrdinal]) {
            Type.DEFAULT -> showDefault(adapter)
            Type.FREQUENCY -> showFrequency(adapter)
            Type.CATEGORY -> {
                val category = arguments?.getString(EXTRA_CATEGORY) ?: return
                showCategoryBy(category, adapter)
            }
            Type.USER -> {
                showUserSettings(adapter)
            }
        }

    }


    @OptIn(ExperimentalCoroutinesApi::class)
    private fun showDefault(adapter: ReactionChoicesAdapter) {
        val defaultReaction = LegacyReaction.defaultReaction
        val emojiFlow = accountStore.observeCurrentAccount.filterNotNull().flatMapLatest {
            metaRepository.observe(it.instanceDomain)
        }.mapNotNull {
            it?.emojis
        }.map { emojis ->
            ArrayList<String>(defaultReaction).apply {
                addAll(emojis.map {
                    ":${it.name}:"
                })
            }
        }
        lifecycleScope.launchWhenResumed {
            emojiFlow.flowOn(Dispatchers.IO).collect { list ->
                adapter.submitList(list)
            }
        }
    }

    private fun showFrequency(adapter: ReactionChoicesAdapter) {

        val ac = accountStore.currentAccount?: return
        lifecycleScope.launch(Dispatchers.IO) {
            val meta = metaRepository.get(ac.instanceDomain)
            val list = reactionHistoryDao.sumReactions(ac.instanceDomain).map {
                it.reaction
            }.map { reaction ->
                if (reaction.codePointCount(0, reaction.length) == 1) {
                    return@map reaction
                }
                if (reaction.startsWith(":") && reaction.endsWith(":") && reaction.contains("@")) {
                    return@map (reaction.replace(":", "").split("@")[0]).let {
                        ":$it:"
                    }
                }
                reaction
            }.filter { reaction ->
                reaction.codePointCount(0, reaction.length) == 1
                        || meta?.emojis?.any {
                    it.name == reaction.replace(":", "")

                } ?: false
            }.distinct()
            withContext(Dispatchers.Main) {
                adapter.submitList(list)
            }

        }
    }

    @ExperimentalCoroutinesApi
    private fun showCategoryBy(category: String, adapter: ReactionChoicesAdapter) {

        val emojiFlow = accountStore.observeCurrentAccount.filterNotNull()
            .flatMapLatest {
                metaRepository.observe(it.instanceDomain)
            }.mapNotNull {
                it?.emojis
            }.map { emojis ->
                emojis.filter {
                    it.category == category

                }.map {
                    ":${it.name}:"
                }
            }
        lifecycleScope.launchWhenResumed {
            emojiFlow
                .flowOn(Dispatchers.IO)
                .collect { emojis ->
                    adapter.submitList(emojis)
                }
        }

    }

    private fun showUserSettings(adapter: ReactionChoicesAdapter) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val instance = accountStore.currentAccount?.instanceDomain
                var reactions =
                    reactionUserSettingDao.findByInstanceDomain(instance!!)?.map {
                        it.reaction
                    } ?: LegacyReaction.defaultReaction
                if (reactions.isEmpty()) {
                    reactions = LegacyReaction.defaultReaction
                }
                Handler(Looper.getMainLooper()).post {
                    adapter.submitList(reactions)
                }
            } catch (e: Exception) {
                Log.e("ReactionChoicesFragment", "load error", e)
            }
        }
    }

}