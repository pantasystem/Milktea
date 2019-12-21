package jp.panta.misskeyandroidclient.view.users

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import jp.panta.misskeyandroidclient.R
import jp.panta.misskeyandroidclient.databinding.ItemFollowingFollowerBinding
import jp.panta.misskeyandroidclient.viewmodel.users.FollowFollowerViewData
import jp.panta.misskeyandroidclient.viewmodel.users.FollowFollowerViewModel

class FollowFollowerListAdapter(
    private val viewLifecycleOwner: LifecycleOwner,
    private val followFollowerViewModel: FollowFollowerViewModel
) : ListAdapter<FollowFollowerViewData, FollowFollowerListAdapter.ViewHolder>(
    DiffUtilItemCallback()
){
    class DiffUtilItemCallback : DiffUtil.ItemCallback<FollowFollowerViewData>(){
        override fun areContentsTheSame(
            oldItem: FollowFollowerViewData,
            newItem: FollowFollowerViewData
        ): Boolean {
            return oldItem.user == newItem.user
        }

        override fun areItemsTheSame(
            oldItem: FollowFollowerViewData,
            newItem: FollowFollowerViewData
        ): Boolean {
            return oldItem.id == newItem.id
        }
    }
    class ViewHolder(val binding: ItemFollowingFollowerBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.followFollowerUser = getItem(position)
        holder.binding.lifecycleOwner = viewLifecycleOwner
        holder.binding.followFollowerViewModel = followFollowerViewModel
        holder.binding.executePendingBindings()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ItemFollowingFollowerBinding>(LayoutInflater.from(parent.context), R.layout.item_following_follower, parent, false)
        return ViewHolder(binding)
    }
}