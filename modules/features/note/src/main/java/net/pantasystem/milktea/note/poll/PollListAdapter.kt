package net.pantasystem.milktea.note.poll

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.pantasystem.milktea.model.notes.Note
import net.pantasystem.milktea.model.notes.poll.Poll
import net.pantasystem.milktea.note.R
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
    class ChoiceHolder(val binding: ItemChoiceBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onBindViewHolder(holder: ChoiceHolder, position: Int) {
        holder.binding.poll = poll
        holder.binding.choice = poll.choices[position]
        holder.binding.radioChoice.setOnClickListener {
            onVoteSelected(OnVoted(noteId, poll.choices[position], poll))
        }
        holder.binding.noteId = noteId
        holder.binding.executePendingBindings()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChoiceHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ItemChoiceBinding>(inflater, R.layout.item_choice, parent, false)
        return ChoiceHolder(binding)
    }
}

data class OnVoted(
    val noteId: Note.Id,
    val choice: Poll.Choice,
    val poll: Poll,
)