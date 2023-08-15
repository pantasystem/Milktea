package net.pantasystem.milktea.note.poll

import android.view.LayoutInflater
import android.widget.LinearLayout
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.poll.Poll
import net.pantasystem.milktea.note.databinding.ItemChoiceBinding
import net.pantasystem.milktea.note.view.NoteCardActionListenerAdapter

object PollListLinearLayoutBinder {

    fun bindPollChoices(
        layout: LinearLayout,
        noteId: Note.Id,
        poll: Poll,
        noteCardActionListenerAdapter: NoteCardActionListenerAdapter
    ) {
        while(layout.childCount > poll.choices.size) {
            layout.removeViewAt(layout.childCount - 1)
        }
        poll.choices.forEachIndexed { index, choice ->
            val existingBinding = if (index < layout.childCount) {
                ItemChoiceBinding.bind(layout.getChildAt(index))
            } else {
                null
            }

            val binding = existingBinding ?: ItemChoiceBinding.inflate(LayoutInflater.from(layout.context), layout, false)

            bindItem(binding, choice, noteId, poll) {
                noteCardActionListenerAdapter.onPollChoiceClicked(it.noteId, poll, it.choice)
            }

            if (existingBinding == null) {
                layout.addView(binding.root)
            }
        }
    }

    private fun bindItem(binding: ItemChoiceBinding, choice: Poll.Choice, noteId: Note.Id, poll: Poll, onVoteSelected: (OnVoted) -> Unit) {
        binding.radioChoice.setOnClickListener {
            onVoteSelected(OnVoted(noteId, choice, poll))
        }

        binding.radioChoice.text = choice.text
        binding.radioChoice.isEnabled = poll.canVote
        binding.radioChoice.isChecked = choice.isVoted

        binding.progressBar.max = poll.totalVoteCount
        binding.progressBar.progress = choice.votes
    }
}