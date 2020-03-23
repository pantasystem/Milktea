package jp.panta.misskeyandroidclient.view.list

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
import jp.panta.misskeyandroidclient.viewmodel.list.ListListViewModel
import jp.panta.misskeyandroidclient.viewmodel.list.UserListOperateViewModel

class ListListAdapter(
    private val listListViewModel: ListListViewModel,
    val lifecycleOwner: LifecycleOwner,
    private val userListOperateViewModel: UserListOperateViewModel
) : ListAdapter<UserList, ListListAdapter.VH>(
    ItemCallback()
){
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
        holder.binding.userListOperateViewModel = userListOperateViewModel
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