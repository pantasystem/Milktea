package jp.panta.misskeyandroidclient.view.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemListUserBinding
import jp.panta.misskeyandroidclient.viewmodel.list.ListUserViewData
import jp.panta.misskeyandroidclient.viewmodel.list.UserListDetailViewModel
import jp.panta.misskeyandroidclient.viewmodel.list.UserListOperateViewModel

class ListUserListAdapter(
    private val userListDetailViewModel: UserListDetailViewModel,
    private val userListOperateViewModel: UserListOperateViewModel,
    val lifecycleOwner: LifecycleOwner
) : ListAdapter<ListUserViewData, ListUserListAdapter.VH>(ItemCallback()){
    class VH(val binding: ItemListUserBinding) : RecyclerView.ViewHolder(binding.root)
    class ItemCallback : DiffUtil.ItemCallback<ListUserViewData>(){
        override fun areContentsTheSame(
            oldItem: ListUserViewData,
            newItem: ListUserViewData
        ): Boolean {
            return oldItem.userId == newItem.userId
                    && oldItem.user.value == newItem.user.value
        }

        override fun areItemsTheSame(
            oldItem: ListUserViewData,
            newItem: ListUserViewData
        ): Boolean {
            return oldItem.userId == newItem.userId
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.binding.listUserViewData = getItem(position)
        holder.binding.userListDetailViewModel = userListDetailViewModel
        holder.binding.lifecycleOwner = lifecycleOwner
        holder.binding.userListOperateViewModel = userListOperateViewModel
        holder.binding.executePendingBindings()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ItemListUserBinding>(inflater, R.layout.item_list_user, parent, false)
        return VH(binding)
    }
}