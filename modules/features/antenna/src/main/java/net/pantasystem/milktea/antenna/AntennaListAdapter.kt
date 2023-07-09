package net.pantasystem.milktea.antenna

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.pantasystem.milktea.antenna.databinding.ItemAntennaBinding
import net.pantasystem.milktea.antenna.viewmodel.AntennaListItem
import net.pantasystem.milktea.antenna.viewmodel.AntennaListViewModel

class AntennaListAdapter(
    private val antennaListViewModel: AntennaListViewModel,
    val lifecycleOwner: LifecycleOwner
) : ListAdapter<AntennaListItem, AntennaListAdapter.ViewHolder>(ItemCallback()){

    class ItemCallback : DiffUtil.ItemCallback<AntennaListItem>(){
        override fun areContentsTheSame(oldItem: AntennaListItem, newItem: AntennaListItem): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: AntennaListItem, newItem: AntennaListItem): Boolean {
            return oldItem.antenna.id == newItem.antenna.id
        }
    }

    class ViewHolder(val binding: ItemAntennaBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ItemAntennaBinding>(inflater, R.layout.item_antenna, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        binding.antenna = getItem(position)
        binding.antennaListViewModel = antennaListViewModel
        binding.lifecycleOwner = lifecycleOwner

    }

}