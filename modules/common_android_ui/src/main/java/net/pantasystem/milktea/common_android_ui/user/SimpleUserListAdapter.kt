package net.pantasystem.milktea.common_android_ui.user

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import net.pantasystem.milktea.common_android_ui.R
import net.pantasystem.milktea.common_android_ui.databinding.ItemSimpleUserBinding
import net.pantasystem.milktea.model.user.User

class SimpleUserListAdapter(
    val lifecycleOwner: LifecycleOwner
) : ListAdapter<User, SimpleUserListAdapter.ViewHolder>(Diff()) {

    class Diff : DiffUtil.ItemCallback<User>() {
        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }
    }
    inner class ViewHolder(val binding: ItemSimpleUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.user = user
            binding.lifecycleOwner = lifecycleOwner
            binding.executePendingBindings()
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ItemSimpleUserBinding>(LayoutInflater.from(parent.context), R.layout.item_simple_user, parent, false)
        return ViewHolder(binding)
    }

}