
package net.pantasystem.milktea.note.reaction
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import net.pantasystem.milktea.app_store.account.AccountStore
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.reaction.ReactionSelection
import net.pantasystem.milktea.note.R
import net.pantasystem.milktea.note.databinding.DialogSelectReactionBinding
import net.pantasystem.milktea.note.emojis.EmojiPickerFragment
import net.pantasystem.milktea.note.viewmodel.NotesViewModel
import javax.inject.Inject

@AndroidEntryPoint
class ReactionSelectionDialog : BottomSheetDialogFragment(),
    ReactionSelection, EmojiPickerFragment.OnEmojiSelectedListener {

    companion object {
        fun newInstance(noteId: Note.Id): ReactionSelectionDialog {
            return ReactionSelectionDialog().apply {
                arguments = Bundle().apply {
                    putLong("ACCOUNT_ID", noteId.accountId)
                    putString("NOTE_ID", noteId.noteId)
                }
            }
        }
    }

    @Inject
    lateinit var accountStore: AccountStore

    val notesViewModel by activityViewModels<NotesViewModel>()



    private val noteId: Note.Id by lazy {
        Note.Id(
            requireArguments().getLong("ACCOUNT_ID"),
            requireArguments().getString("NOTE_ID")!!
        )
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
        binding.lifecycleOwner = this

        if (savedInstanceState == null) {
            val fragment = EmojiPickerFragment()
            childFragmentManager.beginTransaction().also { ft ->
                ft.add(R.id.fragmentBaseContainer, fragment)
            }.commit()
        }

    }

    override fun selectReaction(reaction: String) {
        notesViewModel.toggleReaction(noteId, reaction)
        dismiss()
    }

    override fun onSelect(emoji: String) {
        notesViewModel.toggleReaction(noteId, emoji)
        dismiss()
    }



}
