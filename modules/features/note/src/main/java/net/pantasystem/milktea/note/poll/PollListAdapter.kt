package net.pantasystem.milktea.note.poll

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.poll.Poll
import net.pantasystem.milktea.note.databinding.ItemChoiceBinding

class PollListAdapter(
    val noteId: Note.Id,
    val poll: Poll,
    val onVoteSelected: (OnVoted) -> Unit
) : ListAdapter<Poll.Choice, PollListAdapter.ChoiceHolder>(object : DiffUtil.ItemCallback<Poll.Choice>() {
    override fun areContentsTheSame(oldItem: Poll.Choice, newItem: Poll.Choice): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: Poll.Choice, newItem: Poll.Choice): Boolean {
        return oldItem == newItem
    }
}){
    class ChoiceHolder(val binding: ItemChoiceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun onBind(choice: Poll.Choice, noteId: Note.Id, poll: Poll, onVoteSelected: (OnVoted) -> Unit) {
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


    override fun onBindViewHolder(holder: ChoiceHolder, position: Int) {
        holder.onBind(getItem(position), noteId, poll, onVoteSelected)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoiceHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemChoiceBinding.inflate(inflater, parent, false)
        return ChoiceHolder(binding)
    }
}

data class OnVoted(
    val noteId: Note.Id,
    val choice: Poll.Choice,
    val poll: Poll,
)