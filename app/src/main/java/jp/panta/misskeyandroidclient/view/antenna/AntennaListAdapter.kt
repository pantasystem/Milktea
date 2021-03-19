package jp.panta.misskeyandroidclient.view.antenna

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemAntennaBinding
import jp.panta.misskeyandroidclient.api.v12.antenna.AntennaDTO
import jp.panta.misskeyandroidclient.viewmodel.antenna.AntennaListViewModel

class AntennaListAdapter(
    private val antennaListViewModel: AntennaListViewModel,
    val lifecycleOwner: LifecycleOwner
) : ListAdapter<AntennaDTO, AntennaListAdapter.ViewHolder>(ItemCallback()){

    class ItemCallback : DiffUtil.ItemCallback<AntennaDTO>(){
        override fun areContentsTheSame(oldItem: AntennaDTO, newItem: AntennaDTO): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areItemsTheSame(oldItem: AntennaDTO, newItem: AntennaDTO): Boolean {
            return oldItem == newItem
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