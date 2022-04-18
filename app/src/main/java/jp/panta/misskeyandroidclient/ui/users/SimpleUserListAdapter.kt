package jp.panta.misskeyandroidclient.ui.users

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemSimpleUserBinding
import net.pantasystem.milktea.model.user.User

class SimpleUserListAdapter(
    val lifecycleOwner: LifecycleOwner
) : ListAdapter<net.pantasystem.milktea.model.user.User, SimpleUserListAdapter.ViewHolder>(Diff()) {

    class Diff : DiffUtil.ItemCallback<net.pantasystem.milktea.model.user.User>() {
        override fun areContentsTheSame(oldItem: net.pantasystem.milktea.model.user.User, newItem: net.pantasystem.milktea.model.user.User): Boolean {
            return oldItem == newItem
        }

        override fun areItemsTheSame(oldItem: net.pantasystem.milktea.model.user.User, newItem: net.pantasystem.milktea.model.user.User): Boolean {
            return oldItem.id == newItem.id
        }
    }
    inner class ViewHolder(val binding: ItemSimpleUserBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(user: net.pantasystem.milktea.model.user.User) {
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