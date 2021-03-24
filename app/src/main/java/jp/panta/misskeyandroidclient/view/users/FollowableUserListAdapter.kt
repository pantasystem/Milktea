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
import jp.panta.misskeyandroidclient.viewmodel.users.ShowUserDetails
import jp.panta.misskeyandroidclient.viewmodel.users.ToggleFollowViewModel
import jp.panta.misskeyandroidclient.viewmodel.users.UserViewData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class FollowableUserListAdapter(
    private val viewLifecycleOwner: LifecycleOwner,
    private val showUserDetails: ShowUserDetails,
    private val toggleFollowViewModel: ToggleFollowViewModel
) : ListAdapter<UserViewData, FollowableUserListAdapter.ViewHolder>(
    DiffUtilItemCallback()
){
    @FlowPreview
    @ExperimentalCoroutinesApi
    class DiffUtilItemCallback : DiffUtil.ItemCallback<UserViewData>(){
        override fun areContentsTheSame(oldItem: UserViewData, newItem: UserViewData): Boolean {
            return oldItem.user.value == newItem.user.value
        }

        override fun areItemsTheSame(oldItem: UserViewData, newItem: UserViewData): Boolean {
            return oldItem.userId == newItem.userId
        }
    }
    class ViewHolder(val binding: ItemFollowingFollowerBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        getItem(position).user.observe(viewLifecycleOwner) {
            holder.binding.user = it
        }
        holder.binding.lifecycleOwner = viewLifecycleOwner
        holder.binding.showUserDetails = showUserDetails
        holder.binding.toggleFollowViewModel = toggleFollowViewModel
        holder.binding.executePendingBindings()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ItemFollowingFollowerBinding>(LayoutInflater.from(parent.context), R.layout.item_following_follower, parent, false)
        return ViewHolder(binding)
    }
}