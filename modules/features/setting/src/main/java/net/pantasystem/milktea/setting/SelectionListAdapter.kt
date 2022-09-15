package net.pantasystem.milktea.setting

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.pantasystem.milktea.setting.databinding.ItemSharedSelectionRadioBinding
import net.pantasystem.milktea.setting.viewmodel.SelectionSharedItem

class SelectionListAdapter(
    val viewLifecycleOwner: LifecycleOwner,
    private val selection: SelectionSharedItem
) : ListAdapter<SelectionSharedItem.Choice, SelectionListAdapter.SelectionViewHolder>(
    DiffUtilItemCallback()
){
    class DiffUtilItemCallback : DiffUtil.ItemCallback<SelectionSharedItem.Choice>(){
        override fun areContentsTheSame(
            oldItem: SelectionSharedItem.Choice,
            newItem: SelectionSharedItem.Choice
        ): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(
            oldItem: SelectionSharedItem.Choice,
            newItem: SelectionSharedItem.Choice
        ): Boolean {
            return oldItem.id == newItem.id
        }
    }
    class SelectionViewHolder(val binding: ItemSharedSelectionRadioBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onBindViewHolder(holder: SelectionViewHolder, position: Int) {
        holder.binding.choice = getItem(position)
        holder.binding.selection = selection
        holder.binding.executePendingBindings()
        holder.binding.lifecycleOwner = viewLifecycleOwner
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectionViewHolder {
        val binding = DataBindingUtil.inflate<ItemSharedSelectionRadioBinding>(LayoutInflater.from(parent.context), R.layout.item_shared_selection_radio, parent, false)
        return SelectionViewHolder(binding)
    }
}