package jp.panta.misskeyandroidclient.view.users

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemClickableSimpleUserBinding
import jp.panta.misskeyandroidclient.view.users.selectable.SelectableUsersAdapter
import jp.panta.misskeyandroidclient.viewmodel.users.UserViewData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class ClickableUserListAdapter(val lifecycleOwner: LifecycleOwner) : ListAdapter<UserViewData, ClickableUserListAdapter.VH>(SelectableUsersAdapter.ItemCallback()){
    class VH(val binding: ItemClickableSimpleUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.binding.user = getItem(position)
        holder.binding.lifecycleOwner = lifecycleOwner
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return VH(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_clickable_simple_user,
                parent,
                false
            )
        )
    }

}