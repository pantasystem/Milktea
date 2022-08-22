package net.pantasystem.milktea.common_android_ui.user

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.pantasystem.milktea.common_android_ui.R
import net.pantasystem.milktea.common_android_ui.databinding.ItemUserChipBinding
import net.pantasystem.milktea.common_viewmodel.UserViewData

class UserChipListAdapter(val lifecycleOwner: LifecycleOwner) :
    ListAdapter<UserViewData, UserChipListAdapter.VH>(Diff()) {
    class VH(val binding: ItemUserChipBinding) : RecyclerView.ViewHolder(binding.root)

    class Diff : DiffUtil.ItemCallback<UserViewData>() {
        override fun areContentsTheSame(oldItem: UserViewData, newItem: UserViewData): Boolean {
            return oldItem.userId == newItem.userId && oldItem.user.value == newItem.user.value
        }

        override fun areItemsTheSame(oldItem: UserViewData, newItem: UserViewData): Boolean {
            return oldItem.userId == newItem.userId
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.binding.userViewData = getItem(position)
        holder.binding.lifecycleOwner = lifecycleOwner
        holder.binding.executePendingBindings()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {

        return VH(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_user_chip,
                parent,
                false
            )
        )
    }
}