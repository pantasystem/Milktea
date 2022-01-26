package jp.panta.misskeyandroidclient.ui.users.selectable

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemSelectableSimpleUserBinding
import jp.panta.misskeyandroidclient.ui.users.viewmodel.UserViewData
import jp.panta.misskeyandroidclient.ui.users.viewmodel.selectable.SelectedUserViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@ExperimentalCoroutinesApi
@FlowPreview
class SelectableUsersAdapter(
    val selectedUserViewModel: SelectedUserViewModel,
    val lifecycleOwner: LifecycleOwner
) : ListAdapter<UserViewData, SelectableUsersAdapter.VH>(ItemCallback()){

    class VH(val binding: ItemSelectableSimpleUserBinding) : RecyclerView.ViewHolder(binding.root)
    @ExperimentalCoroutinesApi
    @FlowPreview
    class ItemCallback : DiffUtil.ItemCallback<UserViewData>(){
        override fun areContentsTheSame(oldItem: UserViewData, newItem: UserViewData): Boolean {
            return oldItem.user.value == newItem.user.value
        }

        override fun areItemsTheSame(oldItem: UserViewData, newItem: UserViewData): Boolean {
            return oldItem.userId == newItem.userId
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val user = getItem(position)
        holder.binding.selectedUserViewModel = selectedUserViewModel
        holder.binding.user = user
        holder.binding.lifecycleOwner = lifecycleOwner
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_selectable_simple_user,
                parent,
                false
            )
        )
    }


}