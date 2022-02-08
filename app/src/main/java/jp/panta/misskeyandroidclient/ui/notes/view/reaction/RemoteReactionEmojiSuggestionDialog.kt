package jp.panta.misskeyandroidclient.ui.notes.view.reaction

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexboxLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.DialogRemoteReactionEmojiSuggestionBinding
import jp.panta.misskeyandroidclient.ui.notes.viewmodel.reaction.RemoteReactionEmojiSuggestionViewModel
import jp.panta.misskeyandroidclient.ui.reaction.ReactionChoicesAdapter
import jp.panta.misskeyandroidclient.util.State
import jp.panta.misskeyandroidclient.util.StateContent

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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        val reaction = requireArguments().getString(EXTRA_REACTION)!!
        val noteId = requireArguments().getString(EXTRA_NOTE_ID)!!
        val accountId = requireArguments().getLong(EXTRA_ACCOUNT_ID)

        val view = View.inflate(context, R.layout.dialog_remote_reaction_emoji_suggestion, null)
        val binding = DialogRemoteReactionEmojiSuggestionBinding.bind(view)
        dialog.setContentView(view)

        viewModel.setReaction(accountId, reaction = reaction, noteId = noteId)

        val adapter = ReactionChoicesAdapter {
            viewModel.send()
            dismiss()
        }
        binding.suggestedEmojis.adapter = adapter
        val flexBoxLayoutManager = FlexboxLayoutManager(binding.suggestedEmojis.context)
        flexBoxLayoutManager.alignItems = AlignItems.STRETCH
        binding.suggestedEmojis.layoutManager = flexBoxLayoutManager
        lifecycleScope.launchWhenResumed {
            viewModel.filteredEmojis.collect { state ->
                when (state) {
                    is State.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.suggestedEmojis.visibility = View.GONE
                        binding.errorMessage.visibility = View.GONE
                    }
                    is State.Fixed -> {
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
                    is State.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.suggestedEmojis.visibility = View.GONE
                        binding.errorMessage.visibility = View.VISIBLE
                        binding.errorMessage.text = state.throwable.stackTraceToString()
                    }
                }
            }
        }
        return dialog
    }
}