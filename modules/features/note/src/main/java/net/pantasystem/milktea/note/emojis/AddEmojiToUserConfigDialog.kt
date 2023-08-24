package net.pantasystem.milktea.note.emojis

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.note.EmojiType
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.emojis.viewmodel.AddEmojiToUserConfigViewModel
import net.pantasystem.milktea.note.emojis.viewmodel.AddEmojiToUserConfigViewModel.Companion.EXTRA_TEXT_EMOJI
import net.pantasystem.milktea.note.toTextReaction

@AndroidEntryPoint
class AddEmojiToUserConfigDialog : AppCompatDialogFragment() {

    companion object {
        const val FRAGMENT_TAG = "AddEmojiToUserConfigDialog"


        fun newInstance(emojiType: EmojiType): DialogFragment {
            return AddEmojiToUserConfigDialog().apply {
                arguments = Bundle().apply {
                    putString(EXTRA_TEXT_EMOJI, emojiType.toTextReaction())
                }
            }
        }
    }

    private val viewModel by activityViewModels<AddEmojiToUserConfigViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val textEmoji = requireArguments().getString(EXTRA_TEXT_EMOJI) ?: ""
        return MaterialAlertDialogBuilder(requireContext())
            .setMessage(getString(R.string.dialog_add_emoji_to_user_settings_message, textEmoji))
            .setTitle(R.string.dialog_add_emoji_to_user_settings_title)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                viewModel.save(textEmoji)
            }
            .setNegativeButton(android.R.string.cancel) { _ ,_ ->
                dismiss()
            }.create()
    }
}