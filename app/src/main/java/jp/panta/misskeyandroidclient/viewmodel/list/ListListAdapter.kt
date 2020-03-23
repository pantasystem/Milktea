package jp.panta.misskeyandroidclient.viewmodel.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemListBinding
import jp.panta.misskeyandroidclient.model.list.UserList

class ListListAdapter(private val listListViewModel: ListListViewModel, val lifecycleOwner: LifecycleOwner) : ListAdapter<UserList, ListListAdapter.VH>(ItemCallback()){
    class VH(val binding: ItemListBinding) : RecyclerView.ViewHolder(binding.root)

    class ItemCallback : DiffUtil.ItemCallback<UserList>(){
        override fun areContentsTheSame(oldItem: UserList, newItem: UserList): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: UserList, newItem: UserList): Boolean {
            return oldItem.id == newItem.id
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.binding.list = getItem(position)
        holder.binding.listListViewModel = listListViewModel
        holder.binding.lifecycleOwner = lifecycleOwner
        holder.binding.executePendingBindings()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            DataBindingUtil.inflate<ItemListBinding>(
                LayoutInflater.from(parent.context),
                R.layout.item_list,
                parent,
                false
            )
        )
    }
}