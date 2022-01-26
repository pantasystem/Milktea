package jp.panta.misskeyandroidclient.ui.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemListUserBinding
import jp.panta.misskeyandroidclient.ui.list.viewmodel.UserListDetailViewModel
import jp.panta.misskeyandroidclient.ui.users.viewmodel.UserViewData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class ListUserListAdapter(
    private val userListDetailViewModel: UserListDetailViewModel,
    val lifecycleOwner: LifecycleOwner
) : ListAdapter<UserViewData, ListUserListAdapter.VH>(ItemCallback()){
    class VH(val binding: ItemListUserBinding) : RecyclerView.ViewHolder(binding.root)
    @FlowPreview
    class ItemCallback : DiffUtil.ItemCallback<UserViewData>(){

        @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
        override fun areContentsTheSame(
            oldItem: UserViewData,
            newItem: UserViewData
        ): Boolean {
            return oldItem.userId == newItem.userId
                    && oldItem.user.value == newItem.user.value
        }

        @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
        override fun areItemsTheSame(
            oldItem: UserViewData,
            newItem: UserViewData
        ): Boolean {
            return oldItem.userId == newItem.userId
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.binding.userViewData = getItem(position)
        holder.binding.userListDetailViewModel = userListDetailViewModel
        holder.binding.lifecycleOwner = lifecycleOwner
        holder.binding.executePendingBindings()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ItemListUserBinding>(inflater, R.layout.item_list_user, parent, false)
        return VH(binding)
    }
}