package net.pantasystem.milktea.common_android_ui.reaction

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.pantasystem.milktea.common_android_ui.R
import net.pantasystem.milktea.common_android_ui.databinding.ItemReactionChoiceBinding
import net.pantasystem.milktea.model.notes.reaction.ReactionSelection

class ReactionChoicesAdapter(
    val reactionSelection: ReactionSelection
) : ListAdapter<String, ReactionChoicesAdapter.Holder>(
    DiffUtilItemCallback()
){
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
        val binding =
            DataBindingUtil.inflate<ItemReactionChoiceBinding>(
                LayoutInflater.from(parent.context),
                R.layout.item_reaction_choice,
                parent,
                false
            )
        return Holder(
            binding
        )
    }
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.binding.reaction = getItem(position)
        holder.binding.reactionSelection = reactionSelection
        holder.binding.executePendingBindings()
    }
}