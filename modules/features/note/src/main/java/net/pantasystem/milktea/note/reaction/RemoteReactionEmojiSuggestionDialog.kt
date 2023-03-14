package net.pantasystem.milktea.note.reaction

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexboxLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.pantasystem.milktea.common.ResultState
import net.pantasystem.milktea.common.StateContent
import net.pantasystem.milktea.common_android_ui.reaction.ReactionChoicesAdapter
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.DialogRemoteReactionEmojiSuggestionBinding
import net.pantasystem.milktea.note.reaction.viewmodel.RemoteReactionEmojiSuggestionViewModel

private const val EXTRA_REACTION = "EXTRA_REACTION"
private const val EXTRA_ACCOUNT_ID = "EXTRA_ACCOUNT_ID"
private const val EXTRA_NOTE_ID = "EXTRA_NOTE_ID"

@AndroidEntryPoint
class RemoteReactionEmojiSuggestionDialog : AppCompatDialogFragment() {

    companion object {
        fun newInstance(accountId: Long, noteId: String, reaction: String): RemoteReactionEmojiSuggestionDialog {
            return RemoteReactionEmojiSuggestionDialog().also { fragment ->
                fragment.arguments = Bundle().also { bundle ->
                    bundle.putString(EXTRA_REACTION, reaction)
                    bundle.putLong(EXTRA_ACCOUNT_ID, accountId)
                    bundle.putString(EXTRA_NOTE_ID, noteId)
                }
            }
        }
    }


    val viewModel: RemoteReactionEmojiSuggestionViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_remote_reaction_emoji_suggestion, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = DialogRemoteReactionEmojiSuggestionBinding.bind(view)

        val reaction = requireArguments().getString(EXTRA_REACTION)!!
        val noteId = requireArguments().getString(EXTRA_NOTE_ID)!!
        val accountId = requireArguments().getLong(EXTRA_ACCOUNT_ID)
        viewModel.setReaction(accountId, reaction = reaction, noteId = noteId)

        val adapter = ReactionChoicesAdapter {
            viewModel.send()
            dismiss()
        }
        binding.suggestedEmojis.adapter = adapter
        val flexBoxLayoutManager = FlexboxLayoutManager(binding.suggestedEmojis.context)
        flexBoxLayoutManager.alignItems = AlignItems.STRETCH
        binding.suggestedEmojis.layoutManager = flexBoxLayoutManager
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.filteredEmojis.collect { state ->
                    when (state) {
                        is ResultState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.suggestedEmojis.visibility = View.GONE
                            binding.errorMessage.visibility = View.GONE
                        }
                        is ResultState.Fixed -> {
                            binding.progressBar.visibility = View.GONE
                            binding.suggestedEmojis.visibility = View.VISIBLE
                            val emojis = (state.content as? StateContent.Exist)?.rawContent?: emptyList()
                            if (emojis.isEmpty()) {
                                binding.errorMessage.visibility = View.VISIBLE
                                binding.errorMessage.text = getString(R.string.the_remote_emoji_does_not_exist_in_this_instance)
                            } else {
                                binding.errorMessage.visibility = View.GONE
                            }
                            adapter.submitList(emojis.map {
                                ":${it.name}:"
                            })
                        }
                        is ResultState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.suggestedEmojis.visibility = View.GONE
                            binding.errorMessage.visibility = View.VISIBLE
                            binding.errorMessage.text = state.throwable.stackTraceToString()
                        }
                    }
                }
            }
        }
    }
}