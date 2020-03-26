package jp.panta.misskeyandroidclient.view.users.selectable

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemSelectableSimpleUserBinding
import jp.panta.misskeyandroidclient.viewmodel.users.selectable.SearchAndSelectUserViewModel
import jp.panta.misskeyandroidclient.viewmodel.users.selectable.SelectableUserViewData

class SelectableUsersAdapter(
    val selectableUserViewModel: SearchAndSelectUserViewModel,
    val lifecycleOwner: LifecycleOwner
) : ListAdapter<SelectableUserViewData, SelectableUsersAdapter.VH>(ItemCallback()){

    class VH(val binding: ItemSelectableSimpleUserBinding) : RecyclerView.ViewHolder(binding.root)
    class ItemCallback : DiffUtil.ItemCallback<SelectableUserViewData>(){
        override fun areContentsTheSame(
            oldItem: SelectableUserViewData,
            newItem: SelectableUserViewData
        ): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(
            oldItem: SelectableUserViewData,
            newItem: SelectableUserViewData
        ): Boolean {
            return oldItem.user.userId == newItem.user.userId
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val user = getItem(position)
        holder.binding.selectUserViewModel = selectableUserViewModel
        holder.binding.selectableUser = user
        holder.binding.lifecycleOwner = lifecycleOwner
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            DataBindingUtil.inflate<ItemSelectableSimpleUserBinding>(
                LayoutInflater.from(parent.context),
                R.layout.item_selectable_simple_user,
                parent,
                false
            )
        )
    }


}