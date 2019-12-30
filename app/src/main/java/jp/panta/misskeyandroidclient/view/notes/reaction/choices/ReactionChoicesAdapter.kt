package jp.panta.misskeyandroidclient.view.notes.reaction.choices

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemReactionChoiceBinding
import jp.panta.misskeyandroidclient.viewmodel.notes.NotesViewModel

class ReactionChoicesAdapter(
    val notesViewModel: NotesViewModel
) : ListAdapter<String, ReactionChoicesAdapter.Holder>(DiffUtilItemCallback()){
    class DiffUtilItemCallback : DiffUtil.ItemCallback<String>(){
        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }
    class Holder(val binding : ItemReactionChoiceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val binding = DataBindingUtil.inflate<ItemReactionChoiceBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_reaction_choice,
            parent,
            false
        )
        return Holder(binding)
    }
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.binding.reaction = getItem(position)
        holder.binding.notesViewModel = notesViewModel
        holder.binding.executePendingBindings()
    }
}